package tw.idempiere.base.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.compiere.model.MConversionRate;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

public class ImportRealTimeRate extends SvrProcess {

    private static final String API_URL = "https://rate.bot.com.tw/xrt/flcsv/0/day";

    @Override
    protected void prepare() {
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null)
                ;
            else
                log.log(Level.SEVERE, "Unknown Parameter: " + name);
        }
    }

    @Override
    protected String doIt() throws Exception {
        // 1. Fetch CSV Data
        List<String[]> csvData = fetchCSVData();
        if (csvData == null || csvData.isEmpty()) {
            throw new Exception("No data fetched from BOT API");
        }

        // 2. Parse CSV into Map<CurrencyCode, Rate>
        // Key: Currency Code (e.g. USD), Value: Rate (Average of Spot Buy/Sell)
        Map<String, BigDecimal> rateMap = parseCSV(csvData);

        // 3. Get Active Pairs
        String sql = "SELECT p.TW_ExchangeRatePair_ID, p.C_Currency_ID, p.C_Currency_ID_To, " +
                "p.C_ConversionType_ID, p.API_Key, p.IsInverse, p.AD_Org_ID, p.DateLastRun, " +
                "cf.ISO_Code as ISO_From, ct.ISO_Code as ISO_To " +
                "FROM TW_ExchangeRatePair p " +
                "INNER JOIN C_Currency cf ON p.C_Currency_ID = cf.C_Currency_ID " +
                "INNER JOIN C_Currency ct ON p.C_Currency_ID_To = ct.C_Currency_ID " +
                "WHERE p.IsActive='Y' AND p.AD_Client_ID=?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try {
            pstmt = DB.prepareStatement(sql, get_TrxName());
            pstmt.setInt(1, getAD_Client_ID());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int tw_ExchangeRatePair_ID = rs.getInt("TW_ExchangeRatePair_ID");
                int c_Currency_ID_From = rs.getInt("C_Currency_ID");
                int c_Currency_ID_To = rs.getInt("C_Currency_ID_To");
                int c_ConversionType_ID = rs.getInt("C_ConversionType_ID");
                // boolean isInverse = "Y".equals(rs.getString("IsInverse")); // Logic inferred
                // from TWD position
                int ad_Org_ID = rs.getInt("AD_Org_ID");
                Timestamp dateLastRun = rs.getTimestamp("DateLastRun");
                String isoFrom = rs.getString("ISO_From");
                String isoTo = rs.getString("ISO_To");

                String lookupCode = null;
                boolean invert = false;

                if ("TWD".equals(isoTo)) {
                    lookupCode = isoFrom; // Foreign -> TWD
                    invert = false;
                } else if ("TWD".equals(isoFrom)) {
                    lookupCode = isoTo; // TWD -> Foreign
                    invert = true;
                } else {
                    log.warning("Skipping pair " + isoFrom + "->" + isoTo + ": One side must be TWD for BOT API.");
                    continue;
                }

                // Get Real Time Rate for this currency
                BigDecimal rate = rateMap.get(lookupCode);

                if (rate == null) {
                    log.warning("Rate not found for currency: " + lookupCode);
                    continue;
                }

                if (invert) {
                    rate = BigDecimal.ONE.divide(rate, 12, RoundingMode.HALF_UP);
                }

                // Gap Filling Logic
                Timestamp startDate;
                if (dateLastRun == null) {
                    // Default to yesterday if first run, or just process today.
                    // Request says: "fill gap between DateLastRun and Now". If null, let's just do
                    // today or yesterday?
                    // Let's assume start from Yesterday to ensure continuity if it's new.
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);
                    startDate = new Timestamp(cal.getTimeInMillis());
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateLastRun);
                    cal.add(Calendar.DATE, 1); // Start from Next Day
                    startDate = new Timestamp(cal.getTimeInMillis());
                }

                // Ensure we don't go into the future, but we must process up to TODAY (Now)
                // Normalize dates to midnight for comparison loop
                Calendar current = Calendar.getInstance();
                current.setTime(startDate);
                cleanTime(current);

                Calendar end = Calendar.getInstance();
                end.setTime(now);
                cleanTime(end);

                while (!current.after(end)) {
                    Timestamp validFrom = new Timestamp(current.getTimeInMillis());

                    saveRate(c_Currency_ID_From, c_Currency_ID_To, c_ConversionType_ID, validFrom, rate, ad_Org_ID);
                    count++;

                    current.add(Calendar.DATE, 1);
                }

                // Update DateLastRun to NOW (as per request)
                DB.executeUpdate("UPDATE TW_ExchangeRatePair SET DateLastRun=? WHERE TW_ExchangeRatePair_ID=?",
                        new Object[] { now, tw_ExchangeRatePair_ID }, true, get_TrxName());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, sql, e);
            throw e;
        } finally {
            DB.close(rs, pstmt);
        }

        return "Processed " + count + " rates updates.";
    }

    private void cleanTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void saveRate(int c_Currency_ID_From, int c_Currency_ID_To, int c_ConversionType_ID, Timestamp validFrom,
            BigDecimal rate, int ad_Org_ID) {
        MConversionRate conversionRate = new Query(getCtx(), MConversionRate.Table_Name,
                "C_Currency_ID=? AND C_Currency_ID_To=? AND C_ConversionType_ID=? AND ValidFrom=? AND AD_Client_ID=? AND AD_Org_ID=?",
                get_TrxName())
                .setParameters(c_Currency_ID_From, c_Currency_ID_To, c_ConversionType_ID, validFrom,
                        getAD_Client_ID(), ad_Org_ID)
                .first();

        if (conversionRate == null) {
            conversionRate = new MConversionRate(getCtx(), 0, get_TrxName());
            conversionRate.setAD_Org_ID(ad_Org_ID);
            conversionRate.setC_Currency_ID(c_Currency_ID_From);
            conversionRate.setC_Currency_ID_To(c_Currency_ID_To);
            conversionRate.setC_ConversionType_ID(c_ConversionType_ID);
            conversionRate.setValidFrom(validFrom);
            conversionRate.setValidTo(validFrom);
        }

        conversionRate.setMultiplyRate(rate);
        conversionRate.setDivideRate(BigDecimal.ONE.divide(rate, 12, RoundingMode.HALF_UP));
        conversionRate.saveEx();
    }

    private Map<String, BigDecimal> parseCSV(List<String[]> csvData) {
        Map<String, BigDecimal> map = new HashMap<>();
        // CSV Structure:
        // Col 0: Currency (e.g. USD)
        // Col 3: Spot Buy (即期本行買入)
        // Col 13: Spot Sell (即期本行賣出) - Based on 0-based index from file check
        // File Check:
        // 0: USD, ...
        // 3: 31.57500 (Spot Buy)
        // 13: 31.72500 (Spot Sell)
        // Note: The downloaded file has duplicate headers, but data lines are
        // consistant.
        // Row 0 usually headers, data starts from Row 1.

        for (String[] row : csvData) {
            if (row.length < 14)
                continue;

            String currency = row[0];
            // Skip non-currency rows or headers if they don't match pattern
            if (currency == null || currency.trim().length() != 3)
                continue;

            String spotBuyStr = row[3];
            String spotSellStr = row[13];
            String cashBuyStr = row[2];
            String cashSellStr = row[12];

            try {
                BigDecimal buy = new BigDecimal(spotBuyStr);
                BigDecimal sell = new BigDecimal(spotSellStr);

                // If Spot Rate is zero, try Cash Rate
                if (buy.compareTo(BigDecimal.ZERO) == 0 || sell.compareTo(BigDecimal.ZERO) == 0) {
                    try {
                        buy = new BigDecimal(cashBuyStr);
                        sell = new BigDecimal(cashSellStr);
                    } catch (Exception e) {
                        // Ignore, stick to zero or partial
                    }
                }

                // Average
                BigDecimal avg = buy.add(sell).divide(new BigDecimal(2), 12, RoundingMode.HALF_UP);

                if (avg.compareTo(BigDecimal.ZERO) > 0) {
                    map.put(currency.trim(), avg);
                }
            } catch (Exception e) {
                // Ignore parse errors (maybe header or empty data)
            }
        }
        return map;
    }

    private List<String[]> fetchCSVData() throws Exception {
        List<String[]> list = new ArrayList<>();

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {
            // Simple comma split, might need regex if fields contain commas, but standard
            // finance CSV usually strict.
            // Using logic to handle simple quotes if any? BOT CSV seems simple.
            String[] values = line.split(",");
            list.add(values);
        }
        conn.disconnect();
        return list;
    }
}
