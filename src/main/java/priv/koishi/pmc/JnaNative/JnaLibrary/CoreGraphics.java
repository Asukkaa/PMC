package priv.koishi.pmc.JnaNative.JnaLibrary;

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

    int kCGWindowListOptionAll = 0;

    int kCGWindowListOptionOnScreenOnly = 1;

    int kCGWindowListOptionOnScreenAboveWindow = 2;

    int kCGWindowListOptionOnScreenBelowWindow = 4;

    int kCGWindowListExcludeDesktopElements = 0x10;

    /**
     * macOS 10.15+ 检测屏幕录制权限专用API
     */
    byte CGPreflightScreenCaptureAccess();

    Pointer CGWindowListCopyWindowInfo(int option, int relativeToWindow);

    int CFArrayGetCount(Pointer array);

    Pointer CFArrayGetValueAtIndex(Pointer array, int index);

    void CFRelease(Pointer ref);

    /**
     * 获取窗口属性值
     */
    Pointer CFDictionaryGetValue(Pointer dict, Pointer key);

    String CFStringGetCStringPtr(Pointer cfStr, int encoding);

    boolean CFNumberGetValue(Pointer number, int type, double[] value);

    boolean CFNumberGetValue(Pointer number, int type, int[] value);

}
