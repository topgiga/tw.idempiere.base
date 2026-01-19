package tw.idempiere.base.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.compiere.model.MConversionRate;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

public class ImportCustomsRate extends SvrProcess {

    // URL to fetch JSON data
    private static final String API_URL = "https://portal.sw.nat.gov.tw/APGQ/GC331!downLoad?formBean.downLoadFile=CURRENT_JSON";

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
        // 1. Fetch JSON Data
        String jsonContent = fetchJSONData();
        if (jsonContent == null || jsonContent.isEmpty()) {
            throw new Exception("No data fetched from Customs API");
        }

        // 2. Parse JSON
        CustomsRateData rateData = parseJSON(jsonContent);
        if (rateData == null || rateData.items.isEmpty()) {
            throw new Exception("Failed to parse JSON or no items found");
        }

        log.info("Fetched Period: " + rateData.start + " to " + rateData.end);

        // 3. Process Rates
        return updateRates(rateData);
    }

    private String updateRates(CustomsRateData rateData) throws Exception {
        // Query Active Pairs that need updates
        String sql = "SELECT p.TW_ExchangeRatePair_ID, p.C_Currency_ID, p.C_Currency_ID_To, " +
                "p.C_ConversionType_ID, p.AD_Org_ID, " +
                "cf.ISO_Code as ISO_From, ct.ISO_Code as ISO_To " +
                "FROM TW_ExchangeRatePair p " +
                "INNER JOIN C_Currency cf ON p.C_Currency_ID = cf.C_Currency_ID " +
                "INNER JOIN C_Currency ct ON p.C_Currency_ID_To = ct.C_Currency_ID " +
                "WHERE p.IsActive='Y' AND p.AD_Client_ID=?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        int pairCount = 0;

        // Parse Start and End Dates
        // Format from JSON is likely "yyyyMMdd" based on example "20260121"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date startDate = sdf.parse(rateData.start);
        Date endDate = sdf.parse(rateData.end);

        Timestamp startTs = new Timestamp(startDate.getTime());
        Timestamp endTs = new Timestamp(endDate.getTime());
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try {
            pstmt = DB.prepareStatement(sql, get_TrxName());
            pstmt.setInt(1, getAD_Client_ID());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                pairCount++;
                int tw_ExchangeRatePair_ID = rs.getInt("TW_ExchangeRatePair_ID");
                int c_Currency_ID_From = rs.getInt("C_Currency_ID");
                int c_Currency_ID_To = rs.getInt("C_Currency_ID_To");
                int c_ConversionType_ID = rs.getInt("C_ConversionType_ID");
                int ad_Org_ID = rs.getInt("AD_Org_ID");
                String isoFrom = rs.getString("ISO_From");
                String isoTo = rs.getString("ISO_To");

                String lookupCode = null;
                boolean invert = false;

                // Determine Direction
                if ("TWD".equals(isoTo)) {
                    // Foreign -> TWD (Import/Buy logic?)
                    // Requirement: Foreign -> TWD uses "Buy Value"
                    lookupCode = isoFrom;
                    invert = false;
                } else if ("TWD".equals(isoFrom)) {
                    // TWD -> Foreign (Export/Sell logic?)
                    // Requirement: TWD -> Foreign uses "Sell Value" (inverted later)
                    lookupCode = isoTo;
                    invert = true;
                } else {
                    log.warning("Skipping pair " + isoFrom + "->" + isoTo + ": One side must be TWD.");
                    continue;
                }

                // Get Rate for this currency
                CustomsItem item = rateData.items.get(lookupCode);
                if (item == null) {
                    log.warning("Rate not found for currency: " + lookupCode);
                    continue;
                }

                BigDecimal rateVal;
                if (!invert) {
                    // Foreign -> TWD: Use Buy Value
                    rateVal = item.buyValue;
                } else {
                    // TWD -> Foreign: Use Sell Value (and invert it because rate is TWD per
                    // Foreign)
                    // The table gives Rate = TWD / Foreign (e.g. USD = 32 TWD)
                    // We want TWD -> Foreign, so we need Foreign / TWD = 1 / SellValue
                    // Wait, let's verify logic.
                    // To convert TWD to USD, we divide by the Sell Rate (Bank Sells USD to us).
                    // Rate in DB for TWD->USD is typically 0.03125 (1/32).
                    // So yes, we use Sell Value and Invert.
                    rateVal = item.sellValue;
                }

                if (rateVal == null || rateVal.compareTo(BigDecimal.ZERO) == 0) {
                    log.warning("Zero/Null rate for " + lookupCode);
                    continue;
                }

                // Invert if TWD is source
                if (invert) {
                    rateVal = BigDecimal.ONE.divide(rateVal, 12, RoundingMode.HALF_UP);
                }

                // Expand Date Range
                Calendar current = Calendar.getInstance();
                current.setTime(startTs);
                cleanTime(current);

                Calendar end = Calendar.getInstance();
                end.setTime(endTs);
                cleanTime(end);

                while (!current.after(end)) {
                    Timestamp validFrom = new Timestamp(current.getTimeInMillis());
                    saveRate(c_Currency_ID_From, c_Currency_ID_To, c_ConversionType_ID, validFrom, rateVal, ad_Org_ID);
                    count++;
                    current.add(Calendar.DATE, 1);
                }

                // Update DateLastRun to NOW
                DB.executeUpdate("UPDATE TW_ExchangeRatePair SET DateLastRun=? WHERE TW_ExchangeRatePair_ID=?",
                        new Object[] { now, tw_ExchangeRatePair_ID }, true, get_TrxName());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, sql, e);
            throw e;
        } finally {
            DB.close(rs, pstmt);
        }

        return "Processed " + pairCount + " pairs, " + count + " rates updates. Period: " + rateData.start + "-"
                + rateData.end;
    }

    private void cleanTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void saveRate(int c_Currency_ID_From, int c_Currency_ID_To, int c_ConversionType_ID, Timestamp validFrom,
            BigDecimal rate, int ad_Org_ID) {

        // Check if rate exists
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

        // Always update the rate
        conversionRate.setMultiplyRate(rate);
        conversionRate.setDivideRate(BigDecimal.ONE.divide(rate, 12, RoundingMode.HALF_UP));
        conversionRate.saveEx();
    }

    private String fetchJSONData() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("HTTP Error: " + response.statusCode());
        }

        return response.body();
    }

    // --- Simple Internal Data Structures & Parser ---

    private static class CustomsRateData {
        String start;
        String end;
        Map<String, CustomsItem> items = new HashMap<>();
    }

    private static class CustomsItem {
        String code;
        BigDecimal buyValue;
        BigDecimal sellValue;
    }

    private CustomsRateData parseJSON(String json) {
        CustomsRateData data = new CustomsRateData();

        // Regex for start and end
        // "start":"20260121"
        Pattern pStart = Pattern.compile("\"start\"\\s*:\\s*\"(\\d+)\"");
        Matcher mStart = pStart.matcher(json);
        if (mStart.find()) {
            data.start = mStart.group(1);
        }

        Pattern pEnd = Pattern.compile("\"end\"\\s*:\\s*\"(\\d+)\"");
        Matcher mEnd = pEnd.matcher(json);
        if (mEnd.find()) {
            data.end = mEnd.group(1);
        }

        // Items extraction
        // Assuming items are in "items":[ { ... }, { ... } ]
        // We can just iterate over object patterns that contain "code"

        // This regex looks for { "code":"XXX", ... } blocks roughly
        // It's safer to find the items array content first?
        // Given the simple structure, finding all objects with code/buy/sell might be
        // enough.

        // Pattern to find individual item objects:
        // \{[^}]*"code"\s*:\s*"(\w+)"[^}]*"buyValue"\s*:\s*"([\d\.]+)"[^}]*"sellValue"\s*:\s*"([\d\.]+)"[^}]*\}
        // Note: The order of keys is not guaranteed in JSON, but usually consistent in
        // API.
        // However, a more robust way is to finding all {...} and parsing inside.

        Pattern pObj = Pattern.compile("\\{[^}]+\\}");
        Matcher mObj = pObj.matcher(json);

        while (mObj.find()) {
            String objStr = mObj.group();

            // Extract fields from this object chunk
            String code = extractValue(objStr, "code");
            String buy = extractValue(objStr, "buyValue");
            String sell = extractValue(objStr, "sellValue");

            if (code != null && buy != null && sell != null) {
                try {
                    CustomsItem item = new CustomsItem();
                    item.code = code;
                    item.buyValue = new BigDecimal(buy);
                    item.sellValue = new BigDecimal(sell);
                    data.items.put(code, item);
                } catch (Exception e) {
                    // Ignore parse error for bad number
                }
            }
        }

        return data;
    }

    private String extractValue(String jsonFragment, String key) {
        // Looks for "key":"value"
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(jsonFragment);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
