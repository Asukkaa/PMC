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

/**
 * 窗口信息获取
 *
 * @author KOISHI
 * Date:2025-09-08
 * Time:18:03
 */
public class WindowMonitor {

    public static void main(String[] args) throws Exception {
        List<WindowInfo> windows = new ArrayList<>();
        if (isWin) {
            windows = getWinWindowInfo();
        } else if (isMac) {
            windows = getMacWindowInfo();
        }
        for (WindowInfo info : windows) {
            System.out.println(info);
        }
        System.out.println("-----------------");
//        Thread.sleep(5000);
        if (isMac) {
            System.out.println(getMacFocusWindowInfo());
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
     * 获取Mac窗口信息
     *
     * @return 窗口信息列表
     */
    public static List<WindowInfo> getMacWindowInfo() {
        List<WindowInfo> windowList = new ArrayList<>();
        try {
            CoreGraphics cg = CoreGraphics.INSTANCE;
            // 获取屏幕上所有窗口
            Pointer windowArray = cg.CGWindowListCopyWindowInfo(
                    CoreGraphics.kCGWindowListOptionOnScreenOnly | CoreGraphics.kCGWindowListExcludeDesktopElements,
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
                    Pointer kCGWindowAlpha = createCFString("kCGWindowAlpha");
                    Pointer kCGWindowNumber = createCFString("kCGWindowNumber");
                    // 定义Core Foundation类型常量
                    final int kCFNumberIntType = 9;
                    final int kCFNumberDoubleType = 13;
                    for (int i = 0; i < count; i++) {
                        Pointer windowInfo = cg.CFArrayGetValueAtIndex(windowArray, i);
                        Pointer windowIdValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowNumber);
                        long windowId = 0;
                        if (windowInfo != null) {
                            if (windowIdValue != null) {
                                int[] idArr = new int[1];
                                if (cg.CFNumberGetValue(windowIdValue, kCFNumberIntType, idArr)) {
                                    windowId = idArr[0];
                                }
                            }
                            // 检查窗口是否可见（Alpha > 0）
                            Pointer alphaValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowAlpha);
                            if (alphaValue != null) {
                                double[] alpha = new double[1];
                                if (cg.CFNumberGetValue(alphaValue, kCFNumberDoubleType, alpha) && alpha[0] == 0) {
                                    continue; // 跳过完全透明的窗口
                                }
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
                            // 获取窗口边界
                            Pointer boundsValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowBounds);
                            if (boundsValue != null) {
                                // 创建临时的CFString键
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
                                        // 获取进程ID
                                        Pointer pidValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowOwnerPID);
                                        int pid = 0;
                                        if (pidValue != null) {
                                            int[] pidArr = new int[1];
                                            if (cg.CFNumberGetValue(pidValue, kCFNumberIntType, pidArr)) {
                                                pid = pidArr[0];
                                            }
                                        }
                                        // 获取应用程序名称
                                        String process;
                                        String processName = null;
                                        String processPath = null;
                                        // 获取窗口进程路径和名称
                                        if (pid != 0) {
                                            process = getProcessPathByPid(pid);
                                            if (process != null && process.contains(app)) {
                                                processPath = process.substring(0, process.lastIndexOf(app) + app.length());
                                            }
                                            Pointer ownerValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowOwnerName);
                                            if (ownerValue != null) {
                                                processName = cg.CFStringGetCStringPtr(ownerValue, 0x08000100);
                                            }
                                            if (process != null && StringUtils.isBlank(processName)) {
                                                processName = process.substring(process.lastIndexOf(File.separator) + 1);
                                            }
                                        }
                                        // 获取窗口标题
                                        Pointer titleValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowName);
                                        String title = null;
                                        if (titleValue != null) {
                                            title = cg.CFStringGetCStringPtr(titleValue, 0x08000100);
                                        }
                                        // 过滤条件：有效大小、非系统窗口、非桌面元素
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
                                            windowList.add(windowInfoObj);
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
                    cg.CFRelease(kCGWindowAlpha);
                    cg.CFRelease(kCGWindowNumber);
                } finally {
                    cg.CFRelease(windowArray);
                }
            }
        } catch (Exception e) {
            System.err.println("获取Mac窗口信息时出错: " + e.getMessage());
        }
        return windowList;
    }

    /**
     * 通过进程ID获取进程地址
     */
    private static String getProcessPathByPid(int pid) {
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
            System.err.println("通过PID获取进程名失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 创建CFString对象
     */
    private static Pointer createCFString(String str) {
        Foundation foundation = Foundation.INSTANCE;
        return foundation.CFStringCreateWithCString(Pointer.NULL, str, 0x08000100);
    }

    public static WindowInfo getMacFocusWindowInfo() throws Exception {
        // 使用 AppleScript 获取当前焦点应用的进程ID和名称
        String script = """
                tell application "System Events"
                set frontApp to first application process whose frontmost is true
                set appName to name of frontApp
                set appPID to unix id of frontApp
                return appName & "||" & appPID
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
        process.waitFor();
        // 获取进程路径
        String processPath = getProcessPathByPid(frontAppPid);
        List<WindowInfo> allWindows = getMacWindowInfo();
        if (allWindows.isEmpty()) {
            return null;
        }
        // 查找属于该应用且最可能是焦点窗口的窗口
        WindowInfo focusWindow = null;
        for (WindowInfo window : allWindows) {
            if (frontAppPid == window.getPid() && frontAppName.equals(window.getProcessName())) {
                // 优先选择有标题的窗口
                if (focusWindow == null ||
                        (window.getTitle() != null && !window.getTitle().isEmpty())) {
                    focusWindow = window;
                    // 设置进程路径
                    focusWindow.setProcessPath(processPath);
                }
            }
        }
        // 如果没有找到匹配的窗口，创建一个基本的信息对象
        if (focusWindow == null) {
            focusWindow = new WindowInfo()
                    .setPid(frontAppPid)
                    .setProcessName(frontAppName)
                    .setProcessPath(processPath);
        }
        return focusWindow;
    }

}
