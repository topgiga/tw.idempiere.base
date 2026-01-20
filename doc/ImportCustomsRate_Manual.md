# Taiwan Customs Exchange Rate Import - Technical & User Manual

## 1. Introduction

This document describes the `ImportCustomsRate` process, which automates the retrieval of "Customs Declaration Exchange Rates" (每旬報關適用外幣匯率) from the Taiwan Customs Portal and updates the iDempiere ERP system.

---

## 2. User Manual (使用手冊)

### 2.1 Overview
The system automatically updates the exchange rates for defined currency pairs based on the data published by the Taiwan Customs Administration. The rates are typically published every 10 days (Decade).

### 2.2 Configuration

#### 2.2.1 Exchange Rate Pair Setup (TW_ExchangeRatePair)
To enable automatic updates for a currency pair, you must configure a record in the `TW_ExchangeRatePair` table.

**Fields:**
- **Organization**: The organization this rate applies to.
- **Currency**: The source currency (e.g., `USD`, `EUR`, `JPY`). *Note: Replaces `C_Currency_ID_From`.*
- **To Currency**: The target currency (Must be `TWD` or the currency `TWD` converts to).
- **Conversion Type**: The rate type to update (e.g., `Spot`, `Corporate`).
- **Is Active**: Set to `Y` to enable updates for this pair.
- **Date Last Run**: Automatically updated by the process.

**Logic:**
- One side of the pair **MUST** be `TWD`.
- **Import (Foreign -> TWD)**: Uses the **Buy Rate** (即期買入).
    - Configure: Currency=`USD`, To Currency=`TWD`.
- **Export (TWD -> Foreign)**: Uses the **Sell Rate** (即期賣出).
    - Configure: Currency=`TWD`, To Currency=`USD`.
    - *Note: The system creates an inverted rate record (1 / Sell Rate) because iDempiere stores Multiplier/Divisor.*

### 2.3 Running the Process
The process `ImportCustomsRate` can be:
1.  **Scheduled**: set to run daily or periodically (recommended: once per day to catch the new decadal release).
2.  **Manually Run**: via the Process window.

**Result**:
- The process fetches the JSON data.
- It identifies the valid period (e.g., `2026/01/21` - `2026/01/31`).
- It creates or updates a **single record** in `C_Conversion_Rate` for that period:
    - **Valid From**: Start Date (e.g., `2026-01-21`)
    - **Valid To**: End Date (e.g., `2026-01-31`)
    - **Rate**: The referenced Customs rate.

---

## 3. Technical Reference (技術文件)

### 3.1 Class Information
- **Class**: `tw.idempiere.base.process.ImportCustomsRate`
- **Parent**: `SvrProcess`
- **Package**: `tw.idempiere.base.process`

### 3.2 Data Source
- **URL**: `https://portal.sw.nat.gov.tw/APGQ/GC331!downLoad?formBean.downLoadFile=CURRENT_JSON`
- **Format**: JSON
- **Content**: Contains `start` (date), `end` (date), and a list of `items` with `code` (Currency), `buyValue`, and `sellValue`.

### 3.3 Process Logic (`doIt`)
1.  **Fetch**: Uses `java.net.http.HttpClient` to GET the JSON. User-Agent is set to simulate a browser.
2.  **Parse**: Regex-based parsing extracts the period and rate items.
3.  **Update Loop**:
    - Queries active `TW_ExchangeRatePair`.
    - Matches the Currency Code.
    - **Determine Rate**:
        - Checks `IsSOTrx` column in `TW_ExchangeRatePair`.
        - If `'Y'` (Sales/Receipt): Uses `buyValue`.
        - If `'N'` (Purchase/Payment): Uses `sellValue`.
    - **Determine Direction**:
        - **Foreign->TWD**: Normal rate.
        - **TWD->Foreign**: Inverted rate (`1 / rate`).
    - **Upsert Rate**:
        - Searches for existing `C_Conversion_Rate` matching the keys + `ValidFrom`.
        - Updates `ValidTo` to the end of the period.
        - Updates `MultiplyRate`.
    - **Update Timestamp**: Updates `DateLastRun` on `TW_ExchangeRatePair`.

### 3.4 Schema Dependencies
- **Table**: `TW_ExchangeRatePair`
- **Columns Used**:
    - `TW_ExchangeRatePair_ID` (PK)
    - `C_Currency_ID` (Foreign Currency)
    - `C_Currency_ID_To` (Target Currency)
    - `C_ConversionType_ID`
    - `AD_Org_ID`
    - `IsActive`

### 3.5 Database Changes
- **Updated Logic**: The process now saves the rate with a Date Range (`ValidTo` = End Date) instead of creating daily entries for every day in the range.

### 3.6 Error Handling
- **Network**: Throws Exception if non-200.
- **Parsing**: Logs warning if rate not found or zero.
- **Skipping**: Skips pairs where neither currency is `TWD`.

---
*Document Generated: 2026-01-20*
