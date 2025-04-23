package priv.koishi.pmc.MacScreenPermissionChecker;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * 调用macOS图形相关接口
 *
 * @author koishi
 * Date 2025/04/21
 * Time 14:30
 */
public interface CoreGraphics extends Library {

    /**
     * 调用本地 api
     */
    CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);

    /**
     * macOS 10.15+ 检测屏幕录制权限专用API
     */
    byte CGPreflightScreenCaptureAccess();

}
