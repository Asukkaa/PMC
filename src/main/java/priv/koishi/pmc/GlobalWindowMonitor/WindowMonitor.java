package priv.koishi.pmc.GlobalWindowMonitor;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 窗口信息获取
 *
 * @author KOISHI
 * Date:2025-09-08
 * Time:18:03
 */
public class WindowMonitor {

    public static void main(String[] args) {
        List<WindowInfo> windows = getWinWindowInfo();
        // 打印窗口信息
        for (WindowInfo info : windows) {
            System.out.println(info);
        }
    }

    /**
     * 获取win窗口信息
     *
     * @return 窗口信息列表
     */
    public static List<WindowInfo> getWinWindowInfo() {
        User32 user32 = User32.INSTANCE;
        List<WindowInfo> windowList = new ArrayList<>();
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
                        WindowInfo windowInfo = new WindowInfo()
                                .setHeight(height)
                                .setWidth(width)
                                .setTitle(title)
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

}
