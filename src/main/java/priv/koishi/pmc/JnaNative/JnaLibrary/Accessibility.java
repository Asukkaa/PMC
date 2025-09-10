package priv.koishi.pmc.JnaNative.JnaLibrary;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

/**
 * @author KOISHI
 * Date:2025-09-10
 * Time:17:41
 */
public interface Accessibility extends Library {

    Accessibility INSTANCE = Native.load("ApplicationServices", Accessibility.class);

    // 常量定义
    Pointer kAXSystemWideElement = Pointer.createConstant(1);

    Pointer kAXFocusedWindowAttribute = createCFString("AXFocusedWindow");

    Pointer kAXTitleAttribute = createCFString("AXTitle");

    Pointer kAXWindowsAttribute = createCFString("AXWindows");

    Pointer kAXPositionAttribute = createCFString("AXPosition");

    Pointer kAXSizeAttribute = createCFString("AXSize");

    Pointer kAXMinimizedAttribute = createCFString("AXMinimized");

    // AXValue 相关常量
    Pointer kAXValueTypeCGPoint = createCFString("AXValueCGPointType");

    Pointer kAXValueTypeCGSize = createCFString("AXValueCGSizeType");

    // 核心函数
    Pointer AXUIElementCreateApplication(int pid);

    int AXUIElementCopyAttributeValue(Pointer element, Pointer attribute, PointerByReference value);

    int AXUIElementCopyAttributeNames(Pointer element, PointerByReference names);

    // AXValue 函数
    boolean AXValueGetValue(Pointer value, Pointer type, Structure structure);

    Pointer AXValueCreate(Pointer type, Structure structure);

    // 从CFTypeRef中提取值的函数
    int CFNumberGetValue(Pointer number, int type, int[] value);

    int CFStringGetCString(Pointer cfString, byte[] buffer, int bufferSize, int encoding);

    // 内存管理
    void CFRelease(Pointer ref);

    // 创建CFString的辅助函数
    static Pointer createCFString(String str) {
        Foundation foundation = Foundation.INSTANCE;
        return foundation.CFStringCreateWithCString(Pointer.NULL, str, 0x08000100);
    }

}
