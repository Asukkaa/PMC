package priv.koishi.pmc.JnaNative.NativeInterface;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * 调用 macOS CoreGraphics 框架原生接口的 JNA 接口定义
 * <p>提供访问窗口管理、屏幕捕获权限检测等相关功能
 *
 * @author koishi
 * Date 2025/04/21
 * Time 14:30
 */
public interface CoreGraphics extends Library {

    /**
     * 调用本地 CoreGraphics api
     */
    CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);

    /**
     * 检测当前应用是否具有屏幕录制权限（macOS 10.15+）
     *
     * @return 非零表示有权限，0 表示无权限
     */
    byte CGPreflightScreenCaptureAccess();

}
