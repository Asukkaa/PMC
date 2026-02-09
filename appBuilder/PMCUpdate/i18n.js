module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNewItems: `
        修复设置相对窗口识别范围后运行自动操作会重置所有范围设置的问题
        优化窗口移动后的窗口信息更新逻辑
        优化信息浮窗显示内容，现在能显示正在执行的操作更详细的进度和步骤名称
        修复自动任务参数校验不通过时无法正确取消任务的问题
        修复窗口相对位置可能显示不正确的问题
        修复详情页显示窗口范围浮窗时，如果需要最小化的页面有多个时，macOS 会弹出最后隐藏的窗口的问题
        修复【批量执行 PMC 文件】页面查看 pmc 文件详情在操作列表有数据时，选择取消查看也加载要查看的数据的问题
        修复【批量执行 PMC 文件】跳转逻辑检查有误的问题`,
        invalidJson: "无效的JSON请求体",
        invalidOS: "无效的操作系统参数"
    },
    "zh-TW": {
        versionPrefix: "版本",
        whatsNewItems: `
        修復設置相對視窗識別範圍後，執行自動操作會重置所有範圍設定的問題
        優化視窗移動後的視窗資訊更新邏輯
        優化資訊浮動視窗顯示內容，現在能顯示正在執行的操作更詳細的進度和步驟名稱
        修復自動任務參數校驗不通過時無法正確取消任務的問題
        修復視窗相對位置可能顯示不正確的問題
        修復詳情頁顯示視窗範圍浮動視窗時，如果需要最小化的頁面有多個時，macOS 會彈出最後隱藏的視窗的問題
        修復【批量執行 PMC 文件】頁面查看 pmc 檔案詳情時，在操作列表有資料時，選擇取消查看也會載入要查看的資料的問題
        修復【批量執行 PMC 文件】跳轉邏輯檢查有誤的問題`,
        invalidJson: "無效的JSON請求體",
        invalidOS: "無效的作業系統參數"
    },
    "en": {
        versionPrefix: "Version",
        whatsNewItems: `
        Fixed the issue where setting a relative window recognition range would reset all range settings when running automatic operations
        Optimized the window information update logic after window movement
        Improved the information floating window display, which now shows more detailed progress and step names of the ongoing operation
        Fix the issue where tasks cannot be correctly canceled when automatic task parameter validation fails
        Fix the issue where the relative position of windows may display incorrectly
        Fix the issue where, when displaying a floating window within the window range on the details page, if there are multiple pages that need to be minimized, macOS will pop up the last hidden window
        Fix the issue where, on the【Batch PMC Execution】 page, when viewing PMC file details, if there is data in the operation list, selecting to cancel the view still loads the data to be viewed
        Fix the issue of incorrect jump logic check for 【Batch PMC Execution】`,
        invalidJson: "Invalid JSON request body",
        invalidOS: "Invalid OS parameter"
    }
};