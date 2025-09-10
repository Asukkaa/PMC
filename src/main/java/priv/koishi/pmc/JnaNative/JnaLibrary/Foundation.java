package priv.koishi.pmc.JnaNative.JnaLibrary;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 *
 * @author applesaucepenguin
 * Date 2025-09-10
 * time 11:55
 */
public interface Foundation extends Library {

    Foundation INSTANCE = Native.load("Foundation", Foundation.class);

    Pointer CFStringCreateWithCString(Pointer alloc, String str, int encoding);

}
