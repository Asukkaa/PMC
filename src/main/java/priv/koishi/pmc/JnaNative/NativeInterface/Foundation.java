package priv.koishi.pmc.JnaNative.NativeInterface;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * 调用 macOS Foundation 框架原生接口的 JNA 接口定义。
 * 提供基础数据类型转换和创建功能。
 *
 * @author applesaucepenguin
 * Date 2025-09-10
 * time 11:55
 */
public interface Foundation extends Library {

    /**
     * 调用本地 Foundation api
     */
    Foundation INSTANCE = Native.load("Foundation", Foundation.class);

    /**
     * 使用 C 字符串创建 CFString 对象
     *
     * @param alloc    分配器，通常传入 Pointer.NULL 使用默认分配器
     * @param str      要转换的 Java 字符串
     * @param encoding 字符串编码格式
     * @return 新创建的 CFStringRef 指针，需要调用 CFRelease 释放
     */
    Pointer CFStringCreateWithCString(Pointer alloc, String str, int encoding);

}
