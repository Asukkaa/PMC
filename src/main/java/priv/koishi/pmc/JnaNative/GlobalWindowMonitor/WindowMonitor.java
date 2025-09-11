package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.JnaNative.JnaLibrary.CoreGraphics;
import priv.koishi.pmc.JnaNative.JnaLibrary.Foundation;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.Enum.WindowListOptionEnum.EXCLUDE_DESKTOP_ELEMENTS;
import static priv.koishi.pmc.Finals.Enum.WindowListOptionEnum.ON_SCREEN_ONLY;

/**
 * 窗口信息获取
 *
 * @author KOISHI
 * Date:2025-09-08
 * Time:18:03
 */
public class WindowMonitor {

    public static void main(String[] args) throws Exception {
        if (isMac) {
            System.out.println(getMacFocusWindowInfo());
            System.out.println(getMainWindowInfoByAppBundlePath("/System/Applications/iPhone Mirroring.app"));
        }
    }

    /**
     * 获取win窗口信息
     *
     * @return 窗口信息列表
     */
    public static List<WindowInfo> getWinWindowInfo() {
        List<WindowInfo> windowList = new ArrayList<>();
        User32 user32 = User32.INSTANCE;
        // 使用回调枚举所有顶级窗口
        user32.EnumWindows((hwnd, data) -> {
            // 跳过不可见窗口
            if (!user32.IsWindowVisible(hwnd)) {
                return true;
            }
            char[] titleChars = new char[512];
            user32.GetWindowText(hwnd, titleChars, titleChars.length);
            String title = Native.toString(titleChars);
            if (StringUtils.isNotBlank(title)) {
                // 获取窗口位置和大小
                WinDef.RECT rect = new WinDef.RECT();
                if (user32.GetWindowRect(hwnd, rect)) {
                    int x = rect.left;
                    int y = rect.top;
                    int width = rect.right - x;
                    int height = rect.bottom - y;
                    if (width > 0 && height > 0) {
                        // 获取进程ID
                        WinDef.DWORDByReference pidRef = new WinDef.DWORDByReference();
                        int pid = pidRef.getValue().intValue();
                        // 获取窗口句柄的数值表示
                        long windowId = Pointer.nativeValue(hwnd.getPointer());
                        WindowInfo windowInfo = new WindowInfo()
                                .setHeight(height)
                                .setWidth(width)
                                .setTitle(title)
                                .setId(windowId)
                                .setPid(pid)
                                .setX(x)
                                .setY(y);
                        windowList.add(windowInfo);
                    }
                }
            }
            // 继续枚举
            return true;
        }, null);
        return windowList;
    }

    /**
     * 通过进程 ID 获取 macOS 进程地址
     *
     * @param pid 进程ID
     * @return 进程地址
     */
    private static String getMacProcessPathByPid(int pid) {
        try {
            // 使用ps命令通过PID获取进程名称
            Process process = Runtime.getRuntime().exec(new String[]{"ps", "-p", String.valueOf(pid), "-o", "comm="});
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String name = reader.readLine();
            if (name != null && !name.trim().isEmpty()) {
                return name.trim();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 创建CFString对象
     *
     * @param str 要创建的CFString键
     * @return CFString对象
     */
    private static Pointer createCFString(String str) {
        Foundation foundation = Foundation.INSTANCE;
        return foundation.CFStringCreateWithCString(Pointer.NULL, str, 0x08000100);
    }

    /**
     * 获取Mac聚焦窗口信息
     *
     * @return 窗口信息
     * @throws Exception 无法获取当前焦点应用信息
     */
    public static WindowInfo getMacFocusWindowInfo() throws Exception {
        // 使用 AppleScript 获取当前焦点窗口的详细信息
        String script = """
                tell application "System Events"
                    set frontApp to first application process whose frontmost is true
                    set appName to name of frontApp
                    set appPID to unix id of frontApp
                    -- 尝试获取焦点窗口的信息
                    try
                        set frontWindow to front window of frontApp
                        set windowId to id of frontWindow
                        set windowName to name of frontWindow
                        set windowPosition to position of frontWindow
                        set windowSize to size of frontWindow
                        return appName & "||" & appPID & "||" & windowId & "||" & windowName & "||" & ¬
                            (item 1 of windowPosition) & "||" & (item 2 of windowPosition) & "||" & ¬
                            (item 1 of windowSize) & "||" & (item 2 of windowSize)
                    on error
                        -- 如果无法获取窗口详细信息，只返回应用信息
                        return appName & "||" & appPID & "||||||||"
                    end try
                end tell""";
        Process process = Runtime.getRuntime().exec(new String[]{"osascript", "-e", script});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String readLine = reader.readLine();
        if (readLine == null) {
            throw new RuntimeException("无法获取当前焦点应用信息");
        }
        String[] appInfo = readLine.split("\\|\\|");
        if (appInfo.length < 2) {
            throw new RuntimeException("无法解析焦点应用信息");
        }
        String frontAppName = appInfo[0].trim();
        int frontAppPid = Integer.parseInt(appInfo[1].trim());
        // 获取进程路径
        String processPath = getMacProcessPathByPid(frontAppPid);
        if (processPath != null && StringUtils.isBlank(frontAppName)) {
            frontAppName = processPath.substring(processPath.lastIndexOf(File.separator) + 1);
        }
        if (processPath != null && processPath.contains(app)) {
            processPath = processPath.substring(0, processPath.lastIndexOf(app) + app.length());
        }
        // 创建窗口信息对象
        WindowInfo focusWindow = new WindowInfo()
                .setProcessName(frontAppName)
                .setProcessPath(processPath)
                .setPid(frontAppPid);
        // 如果成功获取了窗口详细信息，填充窗口属性
        if (appInfo.length >= 9 && !appInfo[2].isEmpty()) {
            try {
                long windowId = Long.parseLong(appInfo[2].trim());
                String windowTitle = appInfo[3].trim();
                int x = Integer.parseInt(appInfo[4].trim());
                int y = Integer.parseInt(appInfo[5].trim());
                int width = Integer.parseInt(appInfo[6].trim());
                int height = Integer.parseInt(appInfo[7].trim());
                focusWindow.setTitle(windowTitle)
                        .setHeight(height)
                        .setWidth(width)
                        .setId(windowId)
                        .setX(x)
                        .setY(y);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法解析窗口信息");
            }
        }
        process.waitFor();
        return focusWindow;
    }

    /**
     * 根据进程路径获取主窗口信息
     *
     * @param appBundlePath 进程路径
     * @return 窗口信息，如果没有找到则返回null
     */
    public static WindowInfo getMainWindowInfoByAppBundlePath(String appBundlePath) {
        List<WindowInfo> windows = getWindowInfoByAppBundlePathUsingCG(appBundlePath);
        return windows.isEmpty() ? null : windows.getFirst();
    }

    /**
     * 使用 Core Graphics API 根据应用程序Bundle路径获取窗口信息
     *
     * @param appBundlePath 应用程序Bundle路径（.app结尾）
     * @return 窗口信息列表
     */
    public static List<WindowInfo> getWindowInfoByAppBundlePathUsingCG(String appBundlePath) {
        List<WindowInfo> result = new ArrayList<>();
        try {
            CoreGraphics cg = CoreGraphics.INSTANCE;
            Pointer windowArray = cg.CGWindowListCopyWindowInfo(
                    ON_SCREEN_ONLY.getValue() | EXCLUDE_DESKTOP_ELEMENTS.getValue(),
                    0);
            if (windowArray != null) {
                try {
                    int count = cg.CFArrayGetCount(windowArray);
                    // 创建必要的CFString键
                    Pointer kCGWindowName = createCFString("kCGWindowName");
                    Pointer kCGWindowOwnerName = createCFString("kCGWindowOwnerName");
                    Pointer kCGWindowOwnerPID = createCFString("kCGWindowOwnerPID");
                    Pointer kCGWindowBounds = createCFString("kCGWindowBounds");
                    Pointer kCGWindowLayer = createCFString("kCGWindowLayer");
                    Pointer kCGWindowNumber = createCFString("kCGWindowNumber");
                    // 定义Core Foundation类型常量
                    final int kCFNumberIntType = 9;
                    final int kCFNumberDoubleType = 13;
                    for (int i = 0; i < count; i++) {
                        Pointer windowInfo = cg.CFArrayGetValueAtIndex(windowArray, i);
                        // 获取进程ID
                        Pointer pidValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowOwnerPID);
                        int pid = 0;
                        if (pidValue != null) {
                            int[] pidArr = new int[1];
                            if (cg.CFNumberGetValue(pidValue, kCFNumberIntType, pidArr)) {
                                pid = pidArr[0];
                            }
                        }
                        // 根据PID获取进程路径
                        String process = getMacProcessPathByPid(pid);
                        // 检查进程路径是否匹配
                        if (process != null && process.startsWith(appBundlePath)) {
                            // 获取窗口ID
                            Pointer windowIdValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowNumber);
                            long windowId = 0;
                            if (windowIdValue != null) {
                                int[] idArr = new int[1];
                                if (cg.CFNumberGetValue(windowIdValue, kCFNumberIntType, idArr)) {
                                    windowId = idArr[0];
                                }
                            }
                            // 获取窗口边界
                            Pointer boundsValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowBounds);
                            if (boundsValue != null) {
                                Pointer xKey = createCFString("X");
                                Pointer yKey = createCFString("Y");
                                Pointer widthKey = createCFString("Width");
                                Pointer heightKey = createCFString("Height");
                                Pointer xValue = cg.CFDictionaryGetValue(boundsValue, xKey);
                                Pointer yValue = cg.CFDictionaryGetValue(boundsValue, yKey);
                                Pointer widthValue = cg.CFDictionaryGetValue(boundsValue, widthKey);
                                Pointer heightValue = cg.CFDictionaryGetValue(boundsValue, heightKey);
                                // 立即释放临时键
                                cg.CFRelease(xKey);
                                cg.CFRelease(yKey);
                                cg.CFRelease(widthKey);
                                cg.CFRelease(heightKey);
                                if (xValue != null && yValue != null && widthValue != null && heightValue != null) {
                                    double[] xArr = new double[1];
                                    double[] yArr = new double[1];
                                    double[] widthArr = new double[1];
                                    double[] heightArr = new double[1];
                                    if (cg.CFNumberGetValue(xValue, kCFNumberDoubleType, xArr) &&
                                            cg.CFNumberGetValue(yValue, kCFNumberDoubleType, yArr) &&
                                            cg.CFNumberGetValue(widthValue, kCFNumberDoubleType, widthArr) &&
                                            cg.CFNumberGetValue(heightValue, kCFNumberDoubleType, heightArr)) {
                                        int x = (int) Math.round(xArr[0]);
                                        int y = (int) Math.round(yArr[0]);
                                        int width = (int) Math.round(widthArr[0]);
                                        int height = (int) Math.round(heightArr[0]);
                                        // 获取窗口标题
                                        Pointer titleValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowName);
                                        String title = null;
                                        if (titleValue != null) {
                                            title = cg.CFStringGetCStringPtr(titleValue, 0x08000100);
                                        }
                                        // 获取应用程序名称
                                        Pointer ownerValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowOwnerName);
                                        String processName = null;
                                        String processPath = null;
                                        if (ownerValue != null) {
                                            processName = cg.CFStringGetCStringPtr(ownerValue, 0x08000100);
                                        }
                                        if (StringUtils.isBlank(processName)) {
                                            processName = process.substring(process.lastIndexOf(File.separator) + 1);
                                        }
                                        if (process.contains(app)) {
                                            processPath = process.substring(0, process.lastIndexOf(app) + app.length());
                                        }
                                        // 获取窗口层级
                                        Pointer layerValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowLayer);
                                        int layer = -1;
                                        if (layerValue != null) {
                                            int[] layerArr = new int[1];
                                            if (cg.CFNumberGetValue(layerValue, kCFNumberIntType, layerArr)) {
                                                layer = layerArr[0];
                                            }
                                        }
                                        if (width > 0 && height > 0 && layer >= 0 && layer <= 100 &&
                                                !macSysNoWindowPath.contains(processPath) &&
                                                StringUtils.isNotBlank(processPath)) {
                                            WindowInfo windowInfoObj = new WindowInfo()
                                                    .setProcessName(processName)
                                                    .setProcessPath(processPath)
                                                    .setHeight(height)
                                                    .setWidth(width)
                                                    .setTitle(title)
                                                    .setId(windowId)
                                                    .setPid(pid)
                                                    .setX(x)
                                                    .setY(y);
                                            result.add(windowInfoObj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 释放CFString对象
                    cg.CFRelease(kCGWindowName);
                    cg.CFRelease(kCGWindowOwnerName);
                    cg.CFRelease(kCGWindowOwnerPID);
                    cg.CFRelease(kCGWindowBounds);
                    cg.CFRelease(kCGWindowLayer);
                    cg.CFRelease(kCGWindowNumber);
                } finally {
                    cg.CFRelease(windowArray);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
