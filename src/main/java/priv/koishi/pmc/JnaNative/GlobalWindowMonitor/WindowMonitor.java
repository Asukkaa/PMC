package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Controller.AutoClickController;
import priv.koishi.pmc.JnaNative.NativeInterface.CoreGraphics;
import priv.koishi.pmc.JnaNative.NativeInterface.Foundation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static priv.koishi.pmc.Controller.AutoClickController.massageFloating;
import static priv.koishi.pmc.Controller.AutoClickController.showFloatingWindow;
import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Controller.SettingController.windowInfoFloating;
import static priv.koishi.pmc.Controller.SettingController.windowRelativeInfoFloating;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.Enum.WindowListOptionEnum.EXCLUDE_DESKTOP_ELEMENTS;
import static priv.koishi.pmc.Finals.Enum.WindowListOptionEnum.ON_SCREEN_ONLY;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Service.ImageRecognitionService.dpiScale;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.hideFloatingWindow;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.updateMassageLabel;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.cancelKey;
import static priv.koishi.pmc.Utils.ListenerUtils.addNativeListener;
import static priv.koishi.pmc.Utils.ListenerUtils.removeNativeListener;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.UiUtils.showStage;

/**
 * 窗口信息获取
 *
 * @author KOISHI
 * Date:2025-09-08
 * Time:18:03
 */
public class WindowMonitor {

    /**
     * 全局鼠标监听器
     */
    public ClickWindowMouseListener clickWindowMouseListener;

    /**
     * 全局键盘监听器
     */
    private NativeKeyListener nativeKeyListener;

    /**
     * 正在寻找窗口（true 寻找中）
     */
    @Getter
    private boolean findingWindow;

    /**
     * 寻找窗口计时器
     */
    private Timeline findingWindowTimeline;

    /**
     * 窗口信息处理器
     */
    @Setter
    private WindowInfoHandler windowInfoHandler;

    /**
     * 要防重复点击的组件
     */
    private final List<? extends Node> disableNodes;

    /**
     * 窗口信息
     */
    @Getter
    @Setter
    private WindowInfo windowInfo;

    /**
     * 需要弹出的窗口
     */
    private final Stage stage;

    /**
     * 构造函数
     *
     * @param disableNodes 要防重复点击的组件
     * @param stage        需要弹出的窗口
     */
    public WindowMonitor(List<? extends Node> disableNodes, Stage stage) {
        this.disableNodes = disableNodes;
        this.stage = stage;
    }

    /**
     * 创建默认窗口信息处理器
     *
     * @param infoLabel 窗口信息展示栏
     * @return 窗口信息处理器
     */
    public static WindowInfoHandler creatDefaultWindowInfoHandler(Label infoLabel) {
        return new WindowInfoHandler() {

            /**
             * 显示窗口信息接口
             *
             * @param windowInfo 窗口信息
             */
            @Override
            public void showInfo(WindowInfo windowInfo) {
                Platform.runLater(() -> windowInfoShow(windowInfo, infoLabel));
            }

            /**
             * 删除窗口信息接口
             */
            @Override
            public void removeInfo() {
                infoLabel.setText("");
            }

        };
    }

    /**
     * 计算绝对坐标
     *
     * @param windowInfo 窗口信息
     * @param relativeX  相对横坐标（保留两位小数的百分比）
     * @param relativeY  相对纵坐标（保留两位小数的百分比）
     * @return 计算后的绝对坐标
     */
    public static Map<String, Integer> calculateAbsolutePosition(WindowInfo windowInfo, double relativeX, double relativeY) {
        Map<String, Integer> absolutePosition = new HashMap<>();
        relativeX = relativeX / 100;
        relativeY = relativeY / 100;
        int wX = windowInfo.getX();
        int wY = windowInfo.getY();
        int wW = windowInfo.getWidth();
        int wH = windowInfo.getHeight();
        int x = (int) Math.round(wX + wW * relativeX);
        int y = (int) Math.round(wY + wH * relativeY);
        absolutePosition.put(AbsoluteX, x);
        absolutePosition.put(AbsoluteY, y);
        return absolutePosition;
    }

    /**
     * 计算相对对坐标
     *
     * @param windowInfo 窗口信息
     * @param absoluteX  绝对横坐标
     * @param absoluteY  绝对纵坐标
     * @return 计算后的相对坐标
     */
    public static Map<String, String> calculateRelativePosition(WindowInfo windowInfo, int absoluteX, int absoluteY) {
        Map<String, String> relativePosition = new HashMap<>();
        int wX = windowInfo.getX();
        int wY = windowInfo.getY();
        int wW = windowInfo.getWidth();
        int wH = windowInfo.getHeight();
        if (absoluteX < wX || absoluteX > wX + wW || absoluteY < wY || absoluteY > wY + wH || wH == 0 || wW == 0) {
            relativePosition.put(RelativeX, "");
            relativePosition.put(RelativeY, "");
        } else {
            double rX = ((double) (absoluteX - wX) / wW) * 100;
            double rY = ((double) (absoluteY - wY) / wH) * 100;
            String formattedX = String.format("%.2f", rX);
            String formattedY = String.format("%.2f", rY);
            relativePosition.put(RelativeX, formattedX);
            relativePosition.put(RelativeY, formattedY);
        }
        return relativePosition;
    }

    /**
     * 显示窗口信息
     *
     * @param windowInfo 窗口信息
     * @param infoLabel  窗口信息展示栏
     */
    public static void windowInfoShow(WindowInfo windowInfo, Label infoLabel) {
        if (windowInfo != null) {
            String processPath = windowInfo.getProcessPath();
            if (windowInfo.getPid() != -1) {
                infoLabel.setTextFill(Color.CORNFLOWERBLUE);
                infoLabel.setText(windowInfo.getProcessName());
                String info = findImgSet_PName() + windowInfo.getProcessName() + "\n" +
                        findImgSet_windowPath() + processPath + "\n" +
                        findImgSet_windowTitle() + windowInfo.getTitle() + "\n" +
                        findImgSet_windowLocation() + " X: " + windowInfo.getX() + " Y: " + windowInfo.getY() + "\n" +
                        findImgSet_windowSize() + " W: " + windowInfo.getWidth() + " H: " + windowInfo.getHeight();
                addToolTip(info, infoLabel);
            } else if (StringUtils.isNotBlank(processPath)) {
                infoLabel.setTextFill(Color.RED);
                String name = new File(processPath).getName();
                infoLabel.setText(name);
                String info = text_noWindowInfo() + "\n" +
                        findImgSet_PName() + name + "\n" +
                        findImgSet_windowPath() + processPath + "\n";
                addToolTip(info, infoLabel);
            }
        } else {
            String text = infoLabel.getText();
            if (StringUtils.isNotEmpty(text)) {
                infoLabel.setTextFill(Color.RED);
                addToolTip(text_noWindowInfo(), infoLabel);
            }
        }
    }

    /**
     * 显示窗口信息
     */
    public void showWindowInfo() {
        if (windowInfoHandler != null) {
            windowInfoHandler.showInfo(windowInfo);
        }
    }

    /**
     * 更新窗口信息
     */
    public void updateWindowInfo() {
        if (windowInfo != null) {
            updateWindowInfo(windowInfo.getProcessPath());
        }
    }

    /**
     * 更新窗口信息
     *
     * @param processPath 窗口进程路径
     */
    public void updateWindowInfo(String processPath) {
        if (StringUtils.isNotBlank(processPath)) {
            windowInfo = getMainWindowInfo(processPath);
            showWindowInfo();
        }
    }

    /**
     * 删除窗口信息
     */
    public void removeWindowInfo() {
        windowInfo = null;
        windowInfoHandler.removeInfo();
    }

    /**
     * 开启全局键盘监听
     */
    public void startNativeKeyListener() {
        // 移除可能存在的旧监听器
        removeNativeListener(nativeKeyListener);
        // 键盘监听器
        nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                // 检测快捷键 esc
                if (e.getKeyCode() == cancelKey) {
                    // 检测是否正在查找窗口
                    if (findingWindow) {
                        // 停止记录鼠标点击监听器
                        stopClickWindowMouseListener();
                        // 停止录制计时
                        if (findingWindowTimeline != null) {
                            findingWindowTimeline.stop();
                            findingWindowTimeline = null;
                        }
                        // 停止寻找窗口标记
                        findingWindow = false;
                        AutoClickController.findingWindow = false;
                        if (massageFloating != null) {
                            // 关闭窗口信息浮窗
                            hideFloatingWindow(massageFloating);
                        }
                        showStage(stage);
                    } else if (windowInfoFloating != null) {
                        // 关闭目标窗口信息浮窗
                        hideFloatingWindow(windowInfoFloating);
                        if (windowRelativeInfoFloating != null) {
                            Stage relativeStage = windowRelativeInfoFloating.getStage();
                            windowRelativeInfoFloating.getConfig().setWindowInfo(windowInfo);
                            windowRelativeInfoFloating.updateRelativeHeight((int) relativeStage.getHeight())
                                    .updateRelativeWidth((int) relativeStage.getWidth())
                                    .updateRelativeX((int) relativeStage.getX())
                                    .updateRelativeY((int) relativeStage.getY());
                            hideFloatingWindow(windowRelativeInfoFloating);
                        }
                        showStage(stage);
                    }
                    // 移除键盘监听器
                    removeNativeListener(nativeKeyListener);
                    // 改变要防重复点击的组件状态
                    changeDisableNodes(disableNodes, false);
                }
            }
        };
        // 注册监听器
        addNativeListener(nativeKeyListener);
    }

    /**
     * 停止全局键盘监听
     */
    public void stopNativeKeyListener() {
        removeNativeListener(nativeKeyListener);
    }

    /**
     * 开启记录焦点窗口鼠标点击监听器
     *
     * @param preparation 准备时间
     * @throws IOException 配置文件读取异常
     */
    public void startClickWindowMouseListener(int preparation) throws IOException {
        if (autoClickController.isFree()) {
            // 移除可能存在的旧监听器
            removeNativeListener(clickWindowMouseListener);
            // 启动寻找窗口标记
            findingWindow = true;
            AutoClickController.findingWindow = true;
            // 设置浮窗文本显示准备时间
            AtomicReference<String> text = new AtomicReference<>(text_cancelTask()
                    + preparation + findImgSet_wait());
            updateMassageLabel(massageFloating, text.get());
            // 显示浮窗
            showFloatingWindow(false);
            // 启动键盘监听器
            startNativeKeyListener();
            findingWindowTimeline = new Timeline();
            if (preparation == 0) {
                // 创建新监听器
                clickWindowMouseListener = new ClickWindowMouseListener(massageFloating, this);
                // 注册监听器
                addNativeListener(clickWindowMouseListener);
                // 更新浮窗文本
                text.set(text_cancelTask() + findImgSet_recording());
                updateMassageLabel(massageFloating, text.get());
            } else {
                AtomicInteger preparationTime = new AtomicInteger(preparation);
                // 创建 Timeline 来实现倒计时
                Timeline finalTimeline = findingWindowTimeline;
                findingWindowTimeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
                    preparationTime.getAndDecrement();
                    if (preparationTime.get() > 0) {
                        text.set(text_cancelTask() + preparationTime + findImgSet_wait());
                    } else {
                        // 创建新监听器
                        clickWindowMouseListener = new ClickWindowMouseListener(massageFloating, this);
                        // 注册监听器
                        addNativeListener(clickWindowMouseListener);
                        // 停止 Timeline
                        finalTimeline.stop();
                        // 更新浮窗文本
                        text.set(text_cancelTask() + findImgSet_recording());
                    }
                    updateMassageLabel(massageFloating, text.get());
                }));
                // 设置 Timeline 的循环次数
                findingWindowTimeline.setCycleCount(preparation);
                // 启动 Timeline
                findingWindowTimeline.play();
            }
        }
    }

    /**
     * 停止记录焦点窗口鼠标点击监听器
     */
    public void stopClickWindowMouseListener() {
        if (findingWindow) {
            if (clickWindowMouseListener != null) {
                removeNativeListener(clickWindowMouseListener);
                clickWindowMouseListener = null;
            }
        }
    }

    /**
     * 获取焦点窗口信息
     *
     * @return 焦点窗口信息
     */
    public WindowInfo getFocusWindowInfo() {
        WindowInfo windowInfo = null;
        if (isWin) {
            windowInfo = getWinFocusWindowInfo();
        } else if (isMac) {
            windowInfo = getMacFocusWindowInfo();
        }
        return windowInfo;
    }

    /**
     * 根据进程路径获取主窗口信息
     *
     * @param processPath 进程路径
     * @return 窗口信息，如果没有找到则返回 null
     */
    public static WindowInfo getMainWindowInfo(String processPath) {
        WindowInfo windowInfo = null;
        if (isWin) {
            windowInfo = getMainWinWindowInfo(processPath);
        } else if (isMac) {
            windowInfo = getMainMacWindowInfo(processPath);
        }
        if (windowInfo == null) {
            windowInfo = new WindowInfo()
                    .setProcessPath(processPath);
        }
        return windowInfo;
    }

    /**
     * 获取 win 焦点窗口信息
     *
     * @return 焦点窗口信息
     */
    public WindowInfo getWinFocusWindowInfo() {
        User32 user32 = User32.INSTANCE;
        Psapi psapi = Psapi.INSTANCE;
        Kernel32 kernel32 = Kernel32.INSTANCE;
        WinDef.HWND hwnd = user32.GetForegroundWindow();
        if (hwnd == null) {
            return null;
        }
        // 获取窗口标题
        char[] titleChars = new char[512];
        user32.GetWindowText(hwnd, titleChars, titleChars.length);
        String title = Native.toString(titleChars);
        // 获取窗口位置和大小
        WinDef.RECT rect = new WinDef.RECT();
        if (!user32.GetWindowRect(hwnd, rect)) {
            return null;
        }
        int x = (int) (rect.left / dpiScale);
        int y = (int) (rect.top / dpiScale);
        int width = (int) ((rect.right - rect.left) / dpiScale);
        int height = (int) ((rect.bottom - rect.top) / dpiScale);
        // 获取进程ID - 使用IntByReference而不是DWORDByReference
        IntByReference pidRef = new IntByReference();
        user32.GetWindowThreadProcessId(hwnd, pidRef);
        int pid = pidRef.getValue();
        // 获取进程名称和路径
        String processName = "";
        String processPath = "";
        try {
            WinNT.HANDLE processHandle = kernel32.OpenProcess(
                    Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ,
                    false,
                    pid);
            if (processHandle != null) {
                try {
                    // 使用 char 数组作为缓冲区
                    char[] pathChars = new char[1024];
                    int result = psapi.GetModuleFileNameExW(processHandle, null, pathChars, pathChars.length);
                    if (result > 0) {
                        processPath = new String(pathChars, 0, result);
                        File file = new File(processPath);
                        processName = file.getName();
                    }
                } finally {
                    kernel32.CloseHandle(processHandle);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 获取窗口句柄的数值表示
        long windowId = Pointer.nativeValue(hwnd.getPointer());
        return new WindowInfo()
                .setProcessName(processName)
                .setProcessPath(processPath)
                .setHeight(height)
                .setWidth(width)
                .setId(windowId)
                .setTitle(title)
                .setPid(pid)
                .setX(x)
                .setY(y);
    }

    /**
     * 根据 win 进程路径获取主窗口信息
     *
     * @param processPath 进程路径
     * @return 窗口信息，如果没有找到则返回 null
     */
    public static WindowInfo getMainWinWindowInfo(String processPath) {
        List<WindowInfo> windows = getWinWindowInfos(processPath);
        return windows.isEmpty() ? null : windows.getFirst();
    }

    /**
     * 根据 win 进程路径获取窗口信息
     *
     * @param processPath 进程路径
     * @return 窗口信息列表
     */
    public static List<WindowInfo> getWinWindowInfos(String processPath) {
        List<WindowInfo> result = new ArrayList<>();
        User32 user32 = User32.INSTANCE;
        Psapi psapi = Psapi.INSTANCE;
        Kernel32 kernel32 = Kernel32.INSTANCE;
        // 使用回调枚举所有顶级窗口
        user32.EnumWindows((hwnd, _) -> {
            // 跳过不可见窗口
            if (!user32.IsWindowVisible(hwnd)) {
                return true;
            }
            // 获取进程ID - 使用IntByReference而不是DWORDByReference
            IntByReference pidRef = new IntByReference();
            user32.GetWindowThreadProcessId(hwnd, pidRef);
            int pid = pidRef.getValue();
            // 获取进程路径
            String windowProcessPath = getWinProcessPathByPid(pid, psapi, kernel32);
            // 检查进程路径是否匹配
            if (windowProcessPath != null && windowProcessPath.equalsIgnoreCase(processPath)) {
                // 获取窗口标题
                char[] titleChars = new char[512];
                user32.GetWindowText(hwnd, titleChars, titleChars.length);
                String title = Native.toString(titleChars);
                // 获取窗口位置和大小
                WinDef.RECT rect = new WinDef.RECT();
                if (user32.GetWindowRect(hwnd, rect)) {
                    int x = (int) (rect.left / dpiScale);
                    int y = (int) (rect.top / dpiScale);
                    int width = (int) ((rect.right - rect.left) / dpiScale);
                    int height = (int) ((rect.bottom - rect.top) / dpiScale);
                    if (width > 0 && height > 0) {
                        // 获取进程名称
                        File file = new File(windowProcessPath);
                        String processName = file.getName();
                        // 获取窗口句柄的数值表示
                        long windowId = Pointer.nativeValue(hwnd.getPointer());
                        WindowInfo windowInfo = new WindowInfo()
                                .setProcessName(processName)
                                .setProcessPath(processPath)
                                .setHeight(height)
                                .setWidth(width)
                                .setId(windowId)
                                .setTitle(title)
                                .setPid(pid)
                                .setX(x)
                                .setY(y);
                        result.add(windowInfo);
                    }
                }
            }
            // 继续枚举
            return true;
        }, null);
        return result;
    }

    /**
     * 通过进程 ID 获取 Win 进程路径
     *
     * @param pid      进程 ID，要查询路径的进程的唯一标识符
     * @param psapi    Psapi 库实例，用于调用 GetModuleFileNameExW 等进程信息查询函数
     * @param kernel32 Kernel32 库实例，用于调用 OpenProcess 和 CloseHandle 等进程操作函数
     * @return 进程的完整路径，如果无法获取则返回 null
     */
    private static String getWinProcessPathByPid(int pid, Psapi psapi, Kernel32 kernel32) {
        try {
            WinNT.HANDLE processHandle = kernel32.OpenProcess(
                    Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ,
                    false,
                    pid
            );
            if (processHandle != null) {
                try {
                    // 使用 char 数组作为缓冲区
                    char[] pathChars = new char[1024];
                    int result = psapi.GetModuleFileNameExW(processHandle, null, pathChars, pathChars.length);
                    if (result > 0) {
                        return new String(pathChars, 0, result);
                    }
                } finally {
                    kernel32.CloseHandle(processHandle);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 通过进程 ID 获取 macOS 进程地址
     *
     * @param pid 进程 ID
     * @return 进程地址
     */
    private static String getMacProcessPathByPid(int pid) {
        try {
            // 使用 ps 命令通过 PID 获取进程名称
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
     * 创建 CFString 对象
     *
     * @param str 要创建的 CFString 键
     * @return CFString 对象
     */
    private static Pointer createCFString(String str) {
        Foundation foundation = Foundation.INSTANCE;
        return foundation.CFStringCreateWithCString(Pointer.NULL, str, 0x08000100);
    }

    /**
     * 获取 Mac 聚焦窗口信息（使用 Core Graphics API）
     *
     * @return 窗口信息
     */
    public WindowInfo getMacFocusWindowInfo() {
        WindowInfo result;
        try {
            // 使用 AppleScript 获取当前焦点应用的 PID
            String script = """
                    tell application "System Events"
                        set frontApp to first application process whose frontmost is true
                        set appPID to unix id of frontApp
                        return appPID
                    end tell""";
            Process process = Runtime.getRuntime().exec(new String[]{"osascript", "-e", script});
            int frontAppPid = getMacFrontAppPid(process);
            process.waitFor();
            // 获取进程路径
            String processPath = getMacProcessPathByPid(frontAppPid);
            if (processPath == null) {
                throw new RuntimeException(text_geFocusPathErr());
            }
            if (processPath.contains(app)) {
                processPath = processPath.substring(0, processPath.lastIndexOf(app) + app.length());
            }
            result = getMainMacWindowInfo(processPath);
        } catch (Exception e) {
            throw new RuntimeException(text_getMacFocusErr(), e);
        }
        return result;
    }

    /**
     * 获取 Mac 焦点窗口进程 pid
     *
     * @param process pid 查询命令进程
     * @throws IOException 命令结果读取异常
     */
    private static int getMacFrontAppPid(Process process) throws IOException {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String error = errorReader.readLine();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String pidStr = reader.readLine();
        if (pidStr == null) {
            String err = text_getPidErr();
            if (error != null) {
                err = err + error;
            }
            throw new RuntimeException(err);
        }
        return Integer.parseInt(pidStr.trim());
    }

    /**
     * 根据 mac 进程路径获取主窗口信息
     *
     * @param appPath app 进程路径
     * @return 窗口信息，如果没有找到则返回 null
     */
    public static WindowInfo getMainMacWindowInfo(String appPath) {
        List<WindowInfo> windows = getMacWindowInfos(appPath);
        return windows.isEmpty() ? null : windows.getFirst();
    }

    /**
     * 使用 Core Graphics API 根据应用程序 Bundle 路径获取窗口信息
     *
     * @param appPath 应用程序 Bundle 路径（ .app 结尾）
     * @return 窗口信息列表
     */
    public static List<WindowInfo> getMacWindowInfos(String appPath) {
        List<WindowInfo> result = new ArrayList<>();
        try {
            CoreGraphics cg = CoreGraphics.INSTANCE;
            Pointer windowArray = cg.CGWindowListCopyWindowInfo(
                    ON_SCREEN_ONLY.getValue() | EXCLUDE_DESKTOP_ELEMENTS.getValue(),
                    0);
            if (windowArray != null) {
                try {
                    int count = cg.CFArrayGetCount(windowArray);
                    // 创建必要的 CFString 键
                    Pointer kCGWindowName = createCFString("kCGWindowName");
                    Pointer kCGWindowOwnerName = createCFString("kCGWindowOwnerName");
                    Pointer kCGWindowOwnerPID = createCFString("kCGWindowOwnerPID");
                    Pointer kCGWindowBounds = createCFString("kCGWindowBounds");
                    Pointer kCGWindowLayer = createCFString("kCGWindowLayer");
                    Pointer kCGWindowNumber = createCFString("kCGWindowNumber");
                    // 定义 Core Foundation 类型常量
                    final int kCFNumberIntType = 9;
                    final int kCFNumberDoubleType = 13;
                    for (int i = 0; i < count; i++) {
                        Pointer windowInfo = cg.CFArrayGetValueAtIndex(windowArray, i);
                        // 获取进程 ID
                        Pointer pidValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowOwnerPID);
                        int pid = 0;
                        if (pidValue != null) {
                            int[] pidArr = new int[1];
                            if (cg.CFNumberGetValue(pidValue, kCFNumberIntType, pidArr)) {
                                pid = pidArr[0];
                            }
                        }
                        // 根据 PID 获取进程路径
                        String process = getMacProcessPathByPid(pid);
                        // 检查进程路径是否匹配
                        if (process != null && process.startsWith(appPath)) {
                            // 获取窗口 ID
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
                                        String title = "";
                                        if (titleValue != null) {
                                            title = cg.CFStringGetCStringPtr(titleValue, 0x08000100);
                                        }
                                        if (title == null) {
                                            title = "";
                                        }
                                        // 获取应用程序名称
                                        Pointer ownerValue = cg.CFDictionaryGetValue(windowInfo, kCGWindowOwnerName);
                                        String processName = "";
                                        String processPath = "";
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
                                                    .setLayer(layer)
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
                    // 释放 CFString 对象
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
