package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static priv.koishi.pmc.Finals.CommonFinals.isMac;
import static priv.koishi.pmc.Finals.CommonFinals.isWin;

/**
 * 跨平台窗口移动工具类
 *
 * @author KOISHI
 * Date:2026-01-28
 * Time:15:50
 */
public class WindowMove {

    /**
     * 移动窗口到指定位置
     *
     * @param windowInfo 窗口信息
     * @param x          目标X坐标（屏幕坐标）
     * @param y          目标Y坐标（屏幕坐标）
     * @param width      目标宽度（为 0 时保持原大小）
     * @param height     目标高度（为 0 时保持原大小）
     * @return 是否移动成功
     */
    public static boolean moveWindow(WindowInfo windowInfo, int x, int y, int width, int height) {
        if (windowInfo == null) {
            return false;
        }
        int wX = windowInfo.getX();
        int wY = windowInfo.getY();
        int w = windowInfo.getWidth();
        int h = windowInfo.getHeight();
        if (wX < 0 && wY < 0 && Math.abs(wX) > w && Math.abs(wY) > h) {
            return false;
        }
        if (isWin) {
            return moveWindowWin(windowInfo, x, y, width, height);
        } else if (isMac) {
            return moveWindowMac(windowInfo, x, y, width, height);
        }
        return false;
    }

    /**
     * 仅移动窗口位置（保持原大小）
     *
     * @param windowInfo 窗口信息
     * @param x          目标X坐标
     * @param y          目标Y坐标
     * @return 是否移动成功
     */
    public static boolean moveWindow(WindowInfo windowInfo, int x, int y) {
        return moveWindow(windowInfo, x, y, 0, 0);
    }

    /**
     * Windows 平台窗口移动实现
     *
     * @param windowInfo 窗口信息
     * @param x          目标 X 坐标
     * @param y          目标 Y 坐标
     * @param width      目标宽度（0 表示保持原大小）
     * @param height     目标高度（0 表示保持原大小）
     * @return 是否移动成功
     */
    private static boolean moveWindowWin(WindowInfo windowInfo, int x, int y, int width, int height) {
        long windowId = windowInfo.getId();
        try {
            User32 user32 = User32.INSTANCE;
            WinDef.HWND hwnd = new WinDef.HWND(Pointer.createConstant(windowId));
            // 检查窗口句柄是否有效
            if (!user32.IsWindow(hwnd)) {
                return false;
            }
            // 获取窗口当前大小（如果需要保持原大小）
            if (width == 0 || height == 0) {
                WinDef.RECT rect = new WinDef.RECT();
                if (user32.GetWindowRect(hwnd, rect)) {
                    if (width == 0) {
                        width = rect.right - rect.left;
                    }
                    if (height == 0) {
                        height = rect.bottom - rect.top;
                    }
                } else {
                    return false;
                }
            }
            int flags = WinUser.SWP_NOACTIVATE | WinUser.SWP_NOZORDER;
            if (width == 0 || height == 0) {
                flags |= WinUser.SWP_NOSIZE;
            }
            return user32.SetWindowPos(hwnd, null, x, y, width, height, flags);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * macOS 平台窗口移动实现
     *
     * @param windowInfo 窗口信息
     * @param x          目标X坐标
     * @param y          目标Y坐标
     * @param width      目标宽度（0 表示保持原大小）
     * @param height     目标高度（0 表示保持原大小）
     * @return 是否移动成功
     */
    private static boolean moveWindowMac(WindowInfo windowInfo, int x, int y, int width, int height) {
        try {
            String appName = windowInfo.getProcessName();
            width = width > 0 ? width : windowInfo.getWidth();
            height = height > 0 ? height : windowInfo.getHeight();
            return moveWindowMethod(appName, x, y, width, height);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 移动窗口
     *
     * @param appName 要移动的应用名称
     * @param x       移动后窗口 X 坐标
     * @param y       移动后窗口 Y 坐标
     * @param width   移动后窗口宽度
     * @param height  移动后窗口高度
     */
    private static boolean moveWindowMethod(String appName, int x, int y, int width, int height) {
        try {
            String script = "tell application \"" + appName + "\"\n" +
                    "  activate\n" +
                    "  try\n" +
                    "    set win to window 1\n" +
                    "    set bounds of win to {" +
                    x + ", " +
                    y + ", " +
                    (x + width) + ", " +
                    (y + height) + "}\n" +
                    "    return true\n" +
                    "  on error\n" +
                    "    return false\n" +
                    "  end try\n" +
                    "end tell\n";
            String result = executeAppleScript(script);
            return "true".equals(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行 AppleScript 移动窗口
     *
     * @param script AppleScript 脚本
     * @return 执行结果
     */
    private static String executeAppleScript(String script) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"osascript", "-e", script});
            // 读取错误输出
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                error.append(line);
            }
            // 读取标准输出
            BufferedReader outputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            while ((line = outputReader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();
            if (!error.isEmpty()) {
                throw new RuntimeException(String.valueOf(error));
            }
            return output.toString().trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}