package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.PointerByReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.JnaNative.JnaLibrary.Accessibility;
import priv.koishi.pmc.JnaNative.JnaLibrary.CoreGraphics;
import priv.koishi.pmc.JnaNative.JnaStructure.CGPoint;
import priv.koishi.pmc.JnaNative.JnaStructure.CGSize;

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

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(WindowMonitor.class);

    public static void main(String[] args) throws InterruptedException {
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
            System.out.println(getFocusWindowInfoUsingAccessibility());
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
        return Accessibility.createCFString(str);
    }

    /**
     * 获取Mac焦点窗口
     */
    public static String getFocusWindowInfo1() {
        try {
            String script = "tell application \"System Events\"\n" +
                    "set frontApp to first application process whose frontmost is true\n" +
                    "set appName to name of frontApp\n" +
                    "try\n" +
                    "set windowName to name of front window of frontApp\n" +
                    "set windowPosition to position of front window of frontApp\n" +
                    "set windowSize to size of front window of frontApp\n" +
                    "return appName & \"||\" & windowName & \"||\" & windowPosition & \"||\" & windowSize\n" +
                    "on error errMsg\n" +
                    "return appName & \"||\" & \"\" & \"||\" & \"\" & \"||\" & \"\"\n" +
                    "end try\n" +
                    "end tell";
            Process process = Runtime.getRuntime().exec(new String[]{"osascript", "-e", script});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            process.waitFor();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static WindowInfo getFocusWindowInfo2() {
        List<WindowInfo> allWindows = getMacWindowInfo();
        if (allWindows.isEmpty()) {
            return null;
        }
        try {
            // 使用 AppleScript 获取当前焦点应用的名称
            Process process = Runtime.getRuntime().exec(new String[]{"osascript", "-e",
                    "tell application \"System Events\" to get name of first application process whose frontmost is true"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String frontAppName = reader.readLine().trim();
            process.waitFor();
            // 查找属于该应用且最可能是焦点窗口的窗口, 通常焦点窗口是应用的最上层窗口
            WindowInfo focusWindow = null;
            for (WindowInfo window : allWindows) {
                if (frontAppName.equals(window.getProcessName())) {
                    // 这里可以根据需要添加更多判断条件
                    if (focusWindow == null || window.getTitle() != null) {
                        focusWindow = window;
                    }
                }
            }
            return focusWindow;
        } catch (Exception e) {
            System.err.println("获取焦点窗口信息时出错: " + e.getMessage());
        }
        return null;
    }

    public static WindowInfo getFocusWindowInfoUsingAccessibility() {
        try {
            // 1. 获取前台应用的PID
            Process process = Runtime.getRuntime().exec(new String[]{"osascript", "-e",
                    "tell application \"System Events\" to get unix id of first application process whose frontmost is true"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String pidStr = reader.readLine();
            process.waitFor();
            if (pidStr == null || pidStr.trim().isEmpty()) {
                return null;
            }
            int frontAppPid = Integer.parseInt(pidStr.trim());
            // 2. 为这个PID创建AXUIElement应用对象
            Pointer axApp = Accessibility.INSTANCE.AXUIElementCreateApplication(frontAppPid);
            if (axApp == null) {
                logger.error("无法为PID创建AXUIElement: {}", frontAppPid);
                throw new RuntimeException("无法为PID创建AXUIElement: " + frontAppPid);
            }
            try {
                // 3. 查询该应用的焦点窗口属性
                PointerByReference focusedWindowRef = new PointerByReference();
                int errCode = Accessibility.INSTANCE.AXUIElementCopyAttributeValue(
                        axApp, Accessibility.kAXFocusedWindowAttribute, focusedWindowRef);
                if (errCode != 0 || focusedWindowRef.getValue() == null) {
                    logger.error("无法获取焦点窗口 (错误码: {})", errCode);
                    throw new RuntimeException("无法获取焦点窗口 (错误码: " + errCode + ")");
                }
                Pointer axFocusedWindow = focusedWindowRef.getValue();
                try {
                    // 4. 从焦点窗口元素中提取我们需要的信息
                    String title = getStringAttribute(axFocusedWindow, Accessibility.kAXTitleAttribute);
                    // 使用新的getPositionAndSize方法获取位置和大小
                    int[] x = new int[1], y = new int[1];
                    int[] width = new int[1], height = new int[1];
                    if (!getPositionAndSize(axFocusedWindow, x, y, width, height)) {
                        logger.error("从AXUIElement中获取位置和大小信息失败");
                        throw new RuntimeException("从AXUIElement中获取位置和大小信息失败");
                    }
                    // 5. 构建并返回WindowInfo对象
                    String processPath = getProcessPathByPid(frontAppPid);
                    String processName = getProcessNameByPid(frontAppPid);
                    return new WindowInfo()
                            .setId(Pointer.nativeValue(axFocusedWindow))
                            .setPid(frontAppPid)
                            .setProcessName(processName)
                            .setProcessPath(processPath)
                            .setTitle(title)
                            .setX(x[0])
                            .setY(y[0])
                            .setWidth(width[0])
                            .setHeight(height[0]);
                } finally {
                    if (axFocusedWindow != null) {
                        Accessibility.INSTANCE.CFRelease(axFocusedWindow);
                    }
                }
            } finally {
                Accessibility.INSTANCE.CFRelease(axApp);
            }
        } catch (Exception e) {
            logger.error("Accessibility API 调用失败: {}", e.getMessage());
            throw new RuntimeException("Accessibility API 调用失败: " + e.getMessage());
        }
    }

    // 辅助函数：从AXUIElement获取字符串属性
    private static String getStringAttribute(Pointer element, Pointer attribute) {
        PointerByReference valueRef = new PointerByReference();
        int err = Accessibility.INSTANCE.AXUIElementCopyAttributeValue(element, attribute, valueRef);
        if (err == 0 && valueRef.getValue() != null) {
            try {
                // 简化处理，实际需要将CFStringRef转换为Java String
                byte[] buffer = new byte[255];
                Accessibility.INSTANCE.CFStringGetCString(valueRef.getValue(), buffer, buffer.length, 0x08000100);
                return Native.toString(buffer, "UTF-8");
            } finally {
                Accessibility.INSTANCE.CFRelease(valueRef.getValue());
            }
        }
        return null;
    }

    /**
     * 从AXUIElement中获取位置和大小信息
     */
    private static boolean getPositionAndSize(Pointer axElement, int[] x, int[] y, int[] width, int[] height) {
        try {
            Accessibility ax = Accessibility.INSTANCE;
            // 获取位置
            PointerByReference positionValueRef = new PointerByReference();
            int errPos = ax.AXUIElementCopyAttributeValue(axElement, Accessibility.kAXPositionAttribute, positionValueRef);

            if (errPos == 0 && positionValueRef.getValue() != null) {
                CGPoint point = new CGPoint();
                boolean success = ax.AXValueGetValue(positionValueRef.getValue(), Accessibility.kAXValueTypeCGPoint, point);
                if (success) {
                    x[0] = (int) Math.round(point.x);
                    y[0] = (int) Math.round(point.y);
                }
                ax.CFRelease(positionValueRef.getValue());
            } else {
                logger.error("获取位置属性失败: {}", errPos);
                throw new RuntimeException("获取位置属性失败，错误码: " + errPos);
            }
            // 获取大小
            PointerByReference sizeValueRef = new PointerByReference();
            int errSize = ax.AXUIElementCopyAttributeValue(axElement, Accessibility.kAXSizeAttribute, sizeValueRef);
            if (errSize == 0 && sizeValueRef.getValue() != null) {
                CGSize size = new CGSize();
                boolean success = ax.AXValueGetValue(sizeValueRef.getValue(), Accessibility.kAXValueTypeCGSize, size);
                if (success) {
                    width[0] = (int) Math.round(size.width);
                    height[0] = (int) Math.round(size.height);
                    return true;
                }
                ax.CFRelease(sizeValueRef.getValue());
            } else {
                logger.error("获取大小属性失败: {}", errSize);
                throw new RuntimeException("获取大小属性失败，错误码: " + errSize);
            }
            return false;
        } catch (Exception e) {
            logger.error("获取位置和大小信息时出错: {}", e.getMessage());
            throw new RuntimeException("获取位置和大小信息时出错: " + e.getMessage());
        }
    }

    /**
     * 通过进程ID获取进程名称
     */
    private static String getProcessNameByPid(int pid) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"ps", "-p", String.valueOf(pid), "-o", "comm="});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String name = reader.readLine();
            process.waitFor();
            if (name != null && !name.trim().isEmpty()) {
                return name.trim();
            }
        } catch (Exception e) {
            logger.error("通过PID获取进程名失败: {}", e.getMessage());
            throw new RuntimeException("通过PID获取进程名失败: " + e.getMessage());
        }
        return null;
    }

}
