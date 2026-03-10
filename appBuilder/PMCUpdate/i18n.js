module.exports = {
    "zh-CN": {
        versionPrefix: "版本",
        whatsNewItems: `
        新增图像不存在时重复执行当前设置相关逻辑，可在步骤详情页的重试逻辑中进行设置，当图像识别未匹配时会按照当前操作重复执行，直到图像出现
        新增忽略图像识别坐标相关逻辑，可在详情页勾选设置，勾选后即使识别到目标图像也会按照步骤设置中的坐标进行操作
        为 macOS 新增了一个查看自动启动任务文件的按钮，可在自动启动任务详情页点击，之后会在访达显示自动启动任务文件所在位置
        优化图像识别匹配逻辑与重试逻辑的说明描述，鼠标指针悬浮可看到每个选项的详细逻辑描述
        优化版本更新弹窗界面布局
        修复步骤详情页无法正常显示键盘自动化设置按键的问题
        修复 macOS 删除定时启动任务后到点仍会执行的问题`,
        invalidJson: "无效的 JSON 请求体",
        invalidOS: "无效的操作系统参数"
    },
    "zh-TW": {
        versionPrefix: "版本",
        whatsNewItems: `
        新增圖像不存在時重複執行目前設定相關邏輯，可在步驟詳情頁的重試邏輯中進行設定，當圖像識別未匹配時會按照目前操作重複執行，直到圖像出現
        新增忽略圖像識別座標相關邏輯，可在詳情頁勾選設定，勾選後即使識別到目標圖像也會按照步驟設定中的座標進行操作
        為 macOS 新增了一個查看自動啟動任務檔案的按鈕，可在自動啟動任務詳情頁點擊，之後會在訪達顯示自動啟動任務檔案所在位置
        優化圖像識別匹配邏輯與重試邏輯的說明描述，滑鼠指標懸浮可看到每個選項的詳細邏輯描述
        優化版本更新彈窗介面佈局
        修復步驟詳情頁無法正常顯示鍵盤自動化設定按鍵的問題
        修復 macOS 刪除定時啟動任務後到點仍會執行的問題`,
        invalidJson: "無效的 JSON 請求體",
        invalidOS: "無效的作業系統參數"
    },
    "en": {
        versionPrefix: "Version",
        whatsNewItems: `
        Added logic to repeat current settings when image is not found. It can be configured in the retry logic of the step details page. When image recognition fails to match, the current operation will be repeated until the image appears
        Added logic to ignore image recognition coordinates. It can be enabled via a checkbox on the details page. When checked, even if the target image is recognized, the operation will use the coordinates set in the step instead
        Added a button for macOS to view auto-start task files. Click it on the auto-start task details page, and Finder will reveal the location of the auto-start task file
        Optimized the descriptions of image recognition matching logic and retry logic. Hovering the mouse pointer over each option now displays its detailed logic description
        Optimized the layout of the version update dialog
        Fixed an issue where keyboard automation settings were not displayed correctly on the step details page
        Fixed an issue on macOS where scheduled auto-start tasks would still execute after being deleted`,
        invalidJson: "Invalid JSON request body",
        invalidOS: "Invalid OS parameter"
    }
};