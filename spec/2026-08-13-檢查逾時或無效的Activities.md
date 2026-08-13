# 工作原則
你現在是我的資深軟體工程師。在開始撰寫或修改任何程式碼之前，你必須嚴格遵守以下三個步驟，禁止直接盲猜或盲寫：                                               
                                                                                                                                                             
  1. 【檢索確認】：                                                                                                                                          
     - 先使用專案搜尋工具（如 grep、find 或 view），找出與此任務相關的現有 API、底層 Function 或 Model 定義。                                                
     - 確保你預計要呼叫的任何 Function 在專案中是確實存在的。如果不存在，必須自行實作或使用正確的替代方案。                                                  
                                                                                                                                                             
  2. 【列出計畫 (Plan)】：                                                                                                                                   
     - 在終端機列出你的實作計畫。                                                                                                                            
     - 清楚標明你找到了哪些現有的 Function 可以複用，以及需要新增哪些邏輯。                                                                                  
     - 等待我的確認，或在計畫合理後再推進。                                                                                                                  
                                                                                                                                                             
  3. 【精準編碼】：                                                                                                                                          
     - 嚴格依照剛才確認的計畫與現有 API 規範進行編碼，確保型別與參數完全正確。
     
# DB Connection
ERP DB Postgresql, localhost ,db=dev , user=dev, password=dev

# 任務

1. 製作一個 View ，查詢  AD_WF_Activity  Suspended 時間超過 30天或對應單據 （Record_ID ，AD_Table_ID）文件狀態非 IP （In Progress）
2. 這個 View 要做為 InfoWindow 使用需要有 PK， TableName + "_ID"
3. View 提供 和應該文件的資訊 包括 DocType DocDate DocStatus , 及 AD_WF_Activity User, Created 日期
4. view sql 存在 /tw.idempiere.base/sql
5. 製作一個 Process 可支多 InfoWindow 多選後 進行 一鍵 Abort ，存在 tw.idempiere.base.process

# 完成狀態

## 產出

## View 欄位

## 技術說明

### 資料來源對應

### 計算與點擊聯動邏輯變更

### iDempiere 設定步驟（UI）


