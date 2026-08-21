module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNew: {
            win: `
            优化窗口识别区域浮窗的实现，添加了文本区域背景避免看不清，并解决文本无法完整显示的问题 (2026.08.20)
            重构了后台线程任务的处理机制 (2026.08.20)
            升级依赖版本 (2026.08.20)
            升级 Java 版本由 26.0.2 至 26.0.2.1 (2026.08.20)
            重构了整个自动操作流程 (2026.08.21)
            修复了一些可能的 BUG (2026.08.21)`,
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
            優化視窗識別區域浮窗的實作，加入文字區域背景以避免辨識不清，並解決文字無法完整顯示的問題 (2026.08.20)
            重構後台執行緒任務的處理機制 (2026.08.20)
            升級依賴版本 (2026.08.20)
            升級 Java 版本由 26.0.2 至 26.0.2.1 (2026.08.20)
            重構了整個自動操作流程 (2026.08.21)
            修復了一些可能的 BUG (2026.08.21)`,
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
            Optimized the implementation of the floating window for window recognition areas; added a background to the text area to prevent illegibility and fixed the issue where text could not be fully displayed (2026.08.20)
            Refactored the handling mechanism for background thread tasks (2026.08.20)
            Upgraded dependency versions (2026.08.20)
            Upgraded Java version from 26.0.2 to 26.0.2.1 (2026.08.20)
            The entire automated operation process has been reconstructed (2026.08.21)
            Fixed some potential bugs (2026.08.21)`,
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
