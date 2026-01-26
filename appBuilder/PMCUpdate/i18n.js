module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNewItems: `
        为窗口绑定功能新增了一个识别范围的设置框，绑定窗口后窗口信息栏的右键菜单新增「设置图像识别窗口范围区域」选项，使用这个选项可进入识别范围编辑界面，通过拖拽可设置相对识别范围，即窗口范围内按照百分比记录识别区域的大小与坐标，默认识别整个窗口
        新增「批量运行 PMC 文件」页面，这个页面可以加载多个 PMC 文件，然后按照列表设置的顺序与操作次数执行 PMC 文件，列表中的 PMC 文件具有右键菜单，可以进行查看每个文件具体流程
        新增 PMCS 格式文件导入导出支持，这个文件为 PMC 文件的集合，用来记录多个 PMC 文件的执行逻辑，在「批量运行 PMC 文件」页面可进行这种文件的导入与导出，需要与 PMC 文件配合使用，不会单独记录操作信息
        为「批量运行 PMC 文件」功能新增了一个独立的日志界面，批量运行 PMC 文件后可以查看每个文件的运行记录，与之前的自动流程运行记录相互独立
        优化了录制与运行自动流程时的信息浮窗的信息，将会展示更详细的操作信息
        修复若干 bug
        优化界面布局`,
        invalidJson: "无效的JSON请求体",
        invalidOS: "无效的操作系统参数"
    },
    "zh-TW": {
        versionPrefix: "版本",
        whatsNewItems: `
        為視窗綁定功能新增了一個識別範圍的設定框，綁定視窗後視窗資訊欄的右鍵選單新增「設定圖像識別視窗範圍區域」選項，使用這個選項可進入識別範圍編輯介面，通過拖拽可設定相對識別範圍，即視窗範圍內按照百分比記錄識別區域的大小與座標，預設識別整個視窗
        新增「批量執行 PMC 檔案」頁面，這個頁面可以載入多個 PMC 檔案，然後按照清單設定的順序與操作次數執行 PMC 檔案，清單中的 PMC 檔案具有右鍵選單，可以查看每個檔案的具體流程
        新增 PMCS 格式檔案匯入匯出支援，這個檔案為 PMC 檔案的集合，用來記錄多個 PMC 檔案的執行邏輯，在「批量執行 PMC 檔案」頁面可進行這種檔案的匯入與匯出，需要與 PMC 檔案配合使用，不會單獨記錄操作資訊
        為「批量執行 PMC 檔案」功能新增了一個獨立的日誌介面，批量執行 PMC 檔案後可以查看每個檔案的執行記錄，與之前的自動流程執行記錄相互獨立
        最佳化了錄製與執行自動流程時的資訊浮窗的資訊，將會展示更詳細的操作資訊
        修復若干 bug
        優化介面佈局`,
        invalidJson: "無效的JSON請求體",
        invalidOS: "無效的作業系統參數"
    },
    "en": {
        versionPrefix: "Version",
        whatsNewItems: `
        Added a recognition range setting box to the window binding feature. After binding a window, a new "Adjust Recognition Area" option is added to the right-click menu of the window information bar. Using this option, you can enter the recognition range editing interface. By dragging, you can set a relative recognition range, which records the size and coordinates of the recognition area as a percentage within the window bounds. By default, the entire window is recognized
        Added a "Batch PMC Execution" page. This page can load multiple PMC files and execute them in the order and number of operations set in the list. PMC files in the list have a right-click menu for viewing the specific process of each file
        Added import and export support for PMCS format files. This file is a collection of PMC files used to record the execution logic of multiple PMC files. Import and export of such files can be performed on the "Batch PMC Execution" page. It needs to be used in conjunction with PMC files and does not separately record operation information
        Added an independent log interface for the "Batch PMC Execution" feature. After batch executing PMC files, you can view the execution records of each file, which are independent of the previous automatic process execution records
        Optimized the information displayed in the floating window during recording and executing automatic processes, showing more detailed operation information
        Fixed several bugs
        Optimize interface layout`,
        invalidJson: "Invalid JSON request body",
        invalidOS: "Invalid OS parameter"
    }
};