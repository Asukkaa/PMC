package priv.koishi.pmc.Native;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

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

    Pointer CGWindowListCreate(int option, int relativeToWindow);

    void CFRelease(Pointer ptr);

    int CFArrayGetCount(Pointer array);

    Pointer CFArrayGetValueAtIndex(Pointer array, int index);

    Pointer CGWindowListCreateImage(Pointer array, int imageOption);

}
