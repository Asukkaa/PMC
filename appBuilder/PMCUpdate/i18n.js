module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNew: {
            win: `
            升级 Java 与 JavaFX 版本由 26.0.2 至 27 (2026.09.16)
            修复应用重启时可能会出现孤儿进程导致内存泄漏的问题 (2026.09.16)`,
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
            升級 Java 與 JavaFX 版本，由 26.0.2 至 27 (2026.09.16)
            修復應用程式重新啟動時可能出現孤兒行程，導致記憶體洩漏的問題 (2026.09.16)`,
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
            Upgrade Java and JavaFX versions from 26.0.2 to 27 (2026.09.16)
            Fix an issue where orphan processes may appear when the application restarts, causing memory leaks (2026.09.16)`,
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
