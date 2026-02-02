package priv.koishi.pmc.JnaNative.NativeInterface;

import com.sun.jna.Library;
import com.sun.jna.Native;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.MacNativeWindowInfo;

import static priv.koishi.pmc.Utils.FileUtils.getDylibPath;

/**
 * macOS Native 窗口管理接口类
 *
 * @author applesaucepenguin
 * Date 2026-01-30
 * time 16:18
 */
public interface MacWindowManager extends Library {

    /**
     * 调用 macOS Native 窗口管理接口
     */
    MacWindowManager INSTANCE = Native.load(getDylibPath() + "/libMacWindowManager.dylib", MacWindowManager.class);

    /**
     * 移动窗口
     *
     * @param pid 目标窗口的 PID
     * @param x   目标横坐标
     * @param y   目标纵坐标
     * @return 窗口移动结果（true 移动成功）
     */
    boolean moveWindow(int pid, int x, int y);

    /**
     * 设置窗口大小
     *
     * @param pid    目标窗口的 PID
     * @param height 目标高度
     * @param width  目标宽度
     * @return 窗口大小设置结果（true 设置成功）
     */
    boolean resizeWindow(int pid, int width, int height);

    /**
     * 获取焦点窗口信息
     *
     * @return 焦点窗口信息
     */
    MacNativeWindowInfo.ByValue getFocusedWindowInfo();

    /**
     * 根据进程路径获取窗口信息
     *
     * @param processPath 进程路径
     * @return 窗口信息
     */
    MacNativeWindowInfo.ByValue getMacWindowInfo(String processPath);

}
