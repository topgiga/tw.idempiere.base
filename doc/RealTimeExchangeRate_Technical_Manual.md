# Real-Time Exchange Rate Integration - Technical Manual

## Overview
This document outlines the technical implementation of the Real-Time Exchange Rate integration for iDempiere. It covers the configuration table `TW_ExchangeRatePair` and the process `ImportRealTimeRate` which integrates with the Bank of Taiwan (BOT) API.

## Database Schema: TW_ExchangeRatePair

The `TW_ExchangeRatePair` table holds configuration for currency pairs to be synchronized.

| Column Name | Type | Description |
| :--- | :--- | :--- |
| **TW_ExchangeRatePair_ID** | numeric | Primary Key. |
| **AD_Client_ID** | numeric | Client Identifier. |
| **AD_Org_ID** | numeric | Organization Identifier. |
| **IsActive** | char(1) | 'Y' to enable processing, 'N' to disable. |
| **C_Currency_ID_From** | numeric | Source Currency (e.g., USD). |
| **C_Currency_ID_To** | numeric | Target Currency (e.g., TWD). |
| **C_ConversionType_ID** | numeric | The Conversion Type (e.g., Spot, Corporate) to populate. |
| **API_Key** | varchar | Identifier used for API mapping (e.g., Column Index for CBC API). |
| **IsInverse** | char(1) | If 'Y', the rate is inverted (1/Rate) before saving. |
| **DateLastRun** | timestamp | Tracks the last date/time the process successfully ran and updated rates. |
| **DayOffset** | numeric | Number of days to shift the effective `ValidFrom` date. (Used in CBC Import, currently not applied in Real-Time Import). |

## Process: ImportRealTimeRate

### 1. Overview
The `ImportRealTimeRate` process fetches real-time exchange rates from the Bank of Taiwan (BOT) website via CSV API and updates the C_Conversion_Rate table.

*   **Class**: `tw.prohot.scm.process.ImportRealTimeRate`
*   **API URL**: `https://rate.bot.com.tw/xrt/flcsv/0/day`
*   **Frequency**: On demand or Scheduled.

### 2. Workflow Logic

#### A. Data Fetching
*   The process downloads a CSV file from the BOT API.
*   **Encoding**: UTF-8.

#### B. CSV Parsing & Rate Calculation
*   **Columns Used**:
    *   Currency Code (Column 0)
    *   Cash Buy (Column 2)
    *   Spot Buy (Column 3)
    *   Cash Sell (Column 12)
    *   Spot Sell (Column 13)
*   **Rate Logic**:
    *   Primary: Calculate the average of **Spot Buy** and **Spot Sell**.
    *   `Rate = (SpotBuy + SpotSell) / 2`
*   **Fallback Mechanism (e.g., for IDR)**:
    *   If **Spot Buy** or **Spot Sell** is zero (0), the process falls back to **Cash** rates.
    *   `Rate = (CashBuy + CashSell) / 2`

#### C. Currency Pair Matching (TWD Relative)
The BOT API provides rates relative to New Taiwan Dollar (TWD). The process automatically adjusts based on the configuration in `TW_ExchangeRatePair`:

1.  **Foreign -> TWD** (e.g., USD to TWD):
    *   Uses the parsed Rate directly.
2.  **TWD -> Foreign** (e.g., TWD to USD):
    *   Uses the inverted rate: `1 / Rate`.
3.  **Foreign -> Foreign** (e.g., USD to JPY):
    *   **Not Supported**: The process skips these pairs as the API only provides TWD-relative rates. A warning is logged.

#### D. Gap Filling & Incremental Update
*   **Start Date**:
    *   Reads `DateLastRun` from `TW_ExchangeRatePair`.
    *   If exists: Start Date = `DateLastRun` + 1 day.
    *   If null: Start Date = Yesterday (System Date - 1).
*   **End Date**: Current System Date (Now).
*   **Filling**:
    *   The process iterates from Start Date to End Date.
    *   For *every day* in this range, it creates/updates a `C_Conversion_Rate` record using the **Current Real-Time Rate** fetched in Step B.
    *   This ensures no gaps in exchange rates, filling missing history with the latest known real-time rate.

#### E. Update Status
*   Upon successful processing, `TW_ExchangeRatePair.DateLastRun` is updated to the current system timestamp.

## Usage example

To configure a new currency pair (e.g., IDR to TWD):
1.  Create a record in `TW_ExchangeRatePair`.
2.  Set **From Currency**: IDR.
3.  Set **To Currency**: TWD.
4.  Set **IsActive**: Y.
5.  Execute `Import Real-Time Rate (BOT)`.
6.  Result: The process will detect IDR (using fallback to Cash rates if Spot is unavailable) and populate rates from the last run date until today.
