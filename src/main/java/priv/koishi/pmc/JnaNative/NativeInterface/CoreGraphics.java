package priv.koishi.pmc.JnaNative.NativeInterface;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * 调用 macOS CoreGraphics 框架原生接口的 JNA 接口定义。
 * 提供访问窗口管理、屏幕捕获权限检测等相关功能。
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

    /**
     * 获取窗口信息列表
     *
     * @param option           窗口列表选项，可使用上述 kCGWindowListOption* 常量
     * @param relativeToWindow 相对窗口的 ID，如果为 0 则忽略
     * @return 包含窗口信息的CFArrayRef指针
     */
    Pointer CGWindowListCopyWindowInfo(int option, int relativeToWindow);

    /**
     * 获取 CFArray 中的元素数量
     *
     * @param array CFArrayRef 指针
     * @return 数组中的元素个数
     */
    int CFArrayGetCount(Pointer array);

    /**
     * 获取 CFArray 中指定索引的元素
     *
     * @param array CFArrayRef 指针
     * @param index 元素索引
     * @return 指向元素的指针
     */
    Pointer CFArrayGetValueAtIndex(Pointer array, int index);

    /**
     * 释放 Core Foundation 对象
     *
     * @param ref 需要释放的对象指针
     */
    void CFRelease(Pointer ref);

    /**
     * 从 CFDictionary 中获取指定键对应的值
     *
     * @param dict CFDictionaryRef 指针
     * @param key  键的CFStringRef 指针
     * @return 值的指针，如果键不存在则返回 null
     */
    Pointer CFDictionaryGetValue(Pointer dict, Pointer key);

    /**
     * 将 CFString 转换为 C 字符串指针
     *
     * @param cfStr    CFStringRef 指针
     * @param encoding 字符串编码格式
     * @return C字符串指针，如果转换失败则返回 null
     */
    String CFStringGetCStringPtr(Pointer cfStr, int encoding);

    /**
     * 从 CFNumber 中获取 double 类型的值
     *
     * @param number CFNumberRef 指针
     * @param type   CFNumber 类型标识
     * @param value  用于存储结果的 double 数组
     * @return 成功获取返回 true，否则返回 false
     */
    boolean CFNumberGetValue(Pointer number, int type, double[] value);

    /**
     * 从 CFNumber 中获取 int 类型的值
     *
     * @param number CFNumberRef 指针
     * @param type   CFNumber 类型标识
     * @param value  用于存储结果的 int 数组
     * @return 成功获取返回 true，否则返回 false
     */
    boolean CFNumberGetValue(Pointer number, int type, int[] value);

}
