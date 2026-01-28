package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

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
        if (isWin) {
            return moveWindowWin(windowInfo, x, y, width, height);
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
     * Windows平台窗口移动实现
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

}