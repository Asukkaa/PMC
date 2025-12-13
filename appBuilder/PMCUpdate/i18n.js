module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNewItems: `
        新增窗口绑定功能，可在识别区域设置全局窗口绑定和单个步骤的窗口绑定，绑定窗口后可限制图像识别访问
        新增图像识别范围设置功能，可在屏幕指定位置设置指定大小的图像识别矩形范围，可全局和单步骤设置
        新增相对坐标转换功能，在绑定窗口后可将目标坐标转换为相对坐标，支持相对坐标的导入导出，使用相对坐标后即使窗口位置移动也能点击到正确坐标
        新增鼠标滑轮自动操作功能，支持录制和手动设置
        新增打开文件、打开网址、运行脚本等自动操作，可在独立的操作步骤中设置相关自动操作
        应用界面 UI 全面升级，新增主题切换功能，可跟随操作系统切换明暗主题
        修复了一些 bug
        优化界面布局`,
        invalidJson: "无效的JSON请求体",
        invalidOS: "无效的操作系统参数"
    },
    "zh-TW": {
        versionPrefix: "版本",
        whatsNewItems: `
        新增視窗綁定功能，可在識別區域設定全域視窗綁定和單一步驟的視窗綁定，綁定視窗後可限制影像識別存取
        新增影像識別範圍設定功能，可在螢幕指定位置設定指定大小的影像識別矩形範圍，可全域和單步驟設定
        新增相對座標轉換功能，在綁定視窗後可將目標座標轉換為相對座標，支援相對座標的匯入匯出，使用相對座標後即使視窗位置移動也能點擊到正確座標
        新增滑鼠滾輪自動操作功能，支援錄製和手動設定
        新增開啟檔案、開啟網址、執行腳本等自動操作，可在獨立的操作步驟中設定相關自動操作
        應用介面 UI 全面升級，新增主題切換功能，可跟隨作業系統切換明暗主題
        修復了一些 bug
        優化介面佈局`,
        invalidJson: "無效的JSON請求體",
        invalidOS: "無效的作業系統參數"
    },
    "en": {
        versionPrefix: "Version",
        whatsNewItems: `
        Added window binding functionality, allowing global window binding and single-step window binding in recognition areas. After binding windows, image recognition access can be restricted
        Added image recognition range setting functionality, enabling the creation of specified-size rectangular image recognition areas at designated screen locations. This can be configured globally or per step
        Added relative coordinate conversion functionality. After binding windows, target coordinates can be converted to relative coordinates, supporting import and export of relative coordinates. Using relative coordinates ensures accurate clicking even if the window position moves
        Added mouse scroll wheel automation functionality, supporting both recording and manual configuration
        Added automation operations such as opening files, opening URLs, and running scripts, which can be configured in independent operation steps
        The program interface UI has undergone a comprehensive upgrade, with new theme switching functionality that can follow the operating system to switch between light and dark themes
        Fixed some bugs
        Optimize interface layout`,
        invalidJson: "Invalid JSON request body",
        invalidOS: "Invalid OS parameter"
    }
};