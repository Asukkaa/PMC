package priv.koishi.pmc.MacScreenPermissionChecker;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * macOS屏幕录制权限检查接口
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
     * macOS 10.15+ 专用API
     */
    byte CGPreflightScreenCaptureAccess();

}
