module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNewItems: `
        新增移动窗口功能，绑定窗口进程后可设置进程窗口的左上角位置，需注意窗口移动并不是一定会成功的，可选择是否忽略移动失败事件
        使用 C 语言重写了 macOS 获取焦点窗口的功能，将不再依赖 AppleScript 获取焦点窗口，也就不再需要自动化权限
        定时启动任务新增对 PMCS 文件和带空格的文件名的支持
        修复若干 bug
        优化界面布局`,
        invalidJson: "无效的JSON请求体",
        invalidOS: "无效的操作系统参数"
    },
    "zh-TW": {
        versionPrefix: "版本",
        whatsNewItems: `
        新增移動視窗功能，綁定視窗進程後可設置進程視窗的左上角位置，需注意視窗移動並非一定會成功，可選擇是否忽略移動失敗事件
        使用 C 語言重寫了 macOS 獲取焦點視窗的功能，將不再依賴 AppleScript 獲取焦點視窗，也就不再需要自動化權限
        定時啟動任務新增對 PMCS 檔案和帶空格的文件名稱的支援
        修復若干 bug
        優化介面佈局`,
        invalidJson: "無效的JSON請求體",
        invalidOS: "無效的作業系統參數"
    },
    "en": {
        versionPrefix: "Version",
        whatsNewItems: `
        Added window movement functionality: after binding to a window process, the top-left position of the process window can be set. Note that window movement is not guaranteed to succeed, and there is a configurable option to ignore movement failure events
        Rewrote the macOS focused window acquisition feature in C, eliminating dependency on AppleScript for retrieving the focused window and thus no longer requiring automation permissions
        Scheduled task startup now supports PMCS files and file names containing spaces
        Fixed several bugs
        Optimize interface layout`,
        invalidJson: "Invalid JSON request body",
        invalidOS: "Invalid OS parameter"
    }
};