module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNew: {
            win: `
            新增 cpu 信息查询功能，可在设置也查看当前 CPU 信息 (2026.08.28)
            重构了应用自动更新功能 (2026.08.28)
            修复执行脚本功能中 Java 版本号查询异常的问题 (2026.08.28)
            修复了一些可能的 BUG (2026.08.28)`,
            mac: `
            新增颜色识别功能，可使用拾色器进行拾色后设置目标颜色 (2026.05.12)
            新增文字识别功能，可以使用自定义 traineddata 模型进行文字识别 (2026.05.12)
            新增拓展标题栏相关功能，开启后可使应用标题栏与应用主界面保持一致 (2026.05.12)
            重构了很多界面 UI 逻辑 (2026.05.12)
            修复了很多 BUG (2026.05.12)`
        },
        invalidJson: "无效的 JSON 请求体",
        invalidOS: "无效的操作系统参数"
    },
    "zh-TW": {
        versionPrefix: "版本",
        whatsNew: {
            win: `
            新增 CPU 資訊查詢功能，可在設定頁查看當前 CPU 資訊 (2026.08.28)
            重構了應用程式自動更新功能 (2026.08.28)
            修復執行腳本功能中 Java 版本號查詢異常的問題 (2026.08.28)
            修復了一些可能的 BUG (2026.08.28)`,
            mac: `
            新增顏色識別功能，可使用拾色器進行拾色後設定目標顏色 (2026.05.12)
            新增文字識別功能，可以使用自定義traineddata模型進行文字識別 (2026.05.12)
            新增拓展標題列相關功能，開啟後可使應用標題列與應用主介面保持一致 (2026.05.12)
            重構了很多介面 UI 邏輯 (2026.05.12)
            修復了很多 BUG (2026.05.12)`
        },
        invalidJson: "無效的 JSON 請求體",
        invalidOS: "無效的作業系統參數"
    },
    "en": {
        versionPrefix: "Version",
        whatsNew: {
            win: `
            Added CPU info query feature; current CPU information can now be viewed in the Settings page (2026.08.28)
            Refactored the app auto-update functionality (2026.08.28)
            Fixed an issue where Java version number query in the script execution feature returned incorrect results (2026.08.28)
            Fixed some potential bugs (2026.08.28)`,
            mac: `
            Added color recognition function, allowing users to use a color picker to pick up colors and set the target color (2026.05.12)
            Added text recognition function, allowing for the use of custom traineddata models for text recognition (2026.05.12)
            New features related to expanding the title bar have been added, which can make the application title bar consistent with the main interface of the application when enabled (2026.05.12)
            Refactored a lot of interface UI logic (2026.05.12)
            Fixed many bugs (2026.05.12)`
        },
        invalidJson: "Invalid JSON request body",
        invalidOS: "Invalid OS parameter"
    }
};
