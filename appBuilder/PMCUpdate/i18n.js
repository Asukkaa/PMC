module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNew: {
            win: `
                修复版本检测更新频率下拉框设置失效的问题
                优化缩略图显示速度
                升级 Java 与 JavaFx 版本有 26.0.1 至 26.0.2`,
            mac: `
                新增颜色识别功能，可使用拾色器进行拾色后设置目标颜色
                新增文字识别功能，可以使用自定义 traineddata 模型进行文字识别
                新增拓展标题栏相关功能，开启后可使应用标题栏与应用主界面保持一致
                重构了很多界面 UI 逻辑
                修复了很多 BUG`
        },
        invalidJson: "无效的 JSON 请求体",
        invalidOS: "无效的操作系统参数"
    },
    "zh-TW": {
        versionPrefix: "版本",
        whatsNew: {
            win: `
               修復版本檢測更新頻率下拉選單設定失效的問題
               優化縮圖顯示速度
               將 Java 與 JavaFX 版本由 26.0.1 升級至 26.0.2`,
            mac: `
               新增顏色識別功能，可使用拾色器進行拾色後設定目標顏色
                新增文字識別功能，可以使用自定義traineddata模型進行文字識別
                新增拓展標題列相關功能，開啟後可使應用標題列與應用主介面保持一致
                重構了很多介面 UI 邏輯
                修復了很多 BUG`
        },
        invalidJson: "無效的 JSON 請求體",
        invalidOS: "無效的作業系統參數"
    },
    "en": {
        versionPrefix: "Version",
        whatsNew: {
            win: `
                Fixed the issue where the drop-down menu for version update check frequency settings was not working
                Optimized thumbnail display speed
                Upgraded Java and JavaFX versions from 26.0.1 to 26.0.2`,
            mac: `
               Added color recognition function, allowing users to use a color picker to pick up colors and set the target color
               Added text recognition function, allowing for the use of custom traineddata models for text recognition
               New features related to expanding the title bar have been added, which can make the application title bar consistent with the main interface of the application when enabled
               Refactored a lot of interface UI logic
               Fixed many bugs`
        },
        invalidJson: "Invalid JSON request body",
        invalidOS: "Invalid OS parameter"
    }
};
