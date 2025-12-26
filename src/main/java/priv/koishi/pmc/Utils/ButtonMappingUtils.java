package priv.koishi.pmc.Utils;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import static priv.koishi.pmc.Finals.CommonFinals.isWin;
import static priv.koishi.pmc.Finals.i18nFinal.*;

/**
 * 按键映射工具类
 *
 * @author KOISHI
 * Date:2025-12-11
 * Time:11:15
 */
public class ButtonMappingUtils {

    /**
     * 取消按键
     */
    public static int cancelKey = NativeKeyEvent.VC_ESCAPE;

    /**
     * 自动操作的操作类型选项对应的鼠标行为（操作用）
     */
    public static final BidiMap<String, MouseButton> runClickTypeMap = new DualHashBidiMap<>();

    /**
     * 更新自动操作的操作类型选项对应的鼠标行为（操作用）
     */
    public static void updateRunClickTypeMap() {
        runClickTypeMap.clear();
        runClickTypeMap.put(mouseButton_primary(), MouseButton.PRIMARY);
        runClickTypeMap.put(mouseButton_secondary(), MouseButton.SECONDARY);
        runClickTypeMap.put(mouseButton_middle(), MouseButton.MIDDLE);
        runClickTypeMap.put(mouseButton_forward(), MouseButton.FORWARD);
        runClickTypeMap.put(mouseButton_back(), MouseButton.BACK);
        runClickTypeMap.put(mouseButton_none(), MouseButton.NONE);
    }

    /**
     * 自动操作的操作类型选项对应的鼠标行为（录制用）
     */
    public static final BidiMap<Integer, String> recordClickTypeMap = new DualHashBidiMap<>();

    /**
     * 更新自动操作的操作类型选项对应的鼠标行为（录制用）
     */
    public static void updateRecordClickTypeMap() {
        recordClickTypeMap.clear();
        recordClickTypeMap.put(NativeMouseEvent.BUTTON1, mouseButton_primary());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON2, mouseButton_secondary());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON3, mouseButton_middle());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON4, mouseButton_back());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON5, mouseButton_forward());
        recordClickTypeMap.put(NativeMouseEvent.NOBUTTON, mouseButton_none());
    }

    /**
     * 录制与点击按键类映射
     */
    public static final BidiMap<Integer, MouseButton> NativeMouseToMouseButton = new DualHashBidiMap<>();

    static {
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON1, MouseButton.PRIMARY);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON2, MouseButton.SECONDARY);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON3, MouseButton.MIDDLE);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON4, MouseButton.BACK);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON5, MouseButton.FORWARD);
        NativeMouseToMouseButton.put(NativeMouseEvent.NOBUTTON, MouseButton.NONE);
    }

    /**
     * 键盘按键映射
     */
    public static final BidiMap<Integer, KeyCode> NativeKeyToKeyCode = new DualHashBidiMap<>();

    static {
        // 字母键
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_A, KeyCode.A);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_B, KeyCode.B);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_C, KeyCode.C);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_D, KeyCode.D);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_E, KeyCode.E);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F, KeyCode.F);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_G, KeyCode.G);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_H, KeyCode.H);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_I, KeyCode.I);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_J, KeyCode.J);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_K, KeyCode.K);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_L, KeyCode.L);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_M, KeyCode.M);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_N, KeyCode.N);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_O, KeyCode.O);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_P, KeyCode.P);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_Q, KeyCode.Q);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_R, KeyCode.R);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_S, KeyCode.S);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_T, KeyCode.T);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_U, KeyCode.U);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_V, KeyCode.V);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_W, KeyCode.W);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_X, KeyCode.X);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_Y, KeyCode.Y);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_Z, KeyCode.Z);
        // 数字键
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_0, KeyCode.DIGIT0);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_1, KeyCode.DIGIT1);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_2, KeyCode.DIGIT2);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_3, KeyCode.DIGIT3);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_4, KeyCode.DIGIT4);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_5, KeyCode.DIGIT5);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_6, KeyCode.DIGIT6);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_7, KeyCode.DIGIT7);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_8, KeyCode.DIGIT8);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_9, KeyCode.DIGIT9);
        // 功能键
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F1, KeyCode.F1);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F2, KeyCode.F2);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F3, KeyCode.F3);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F4, KeyCode.F4);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F5, KeyCode.F5);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F6, KeyCode.F6);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F7, KeyCode.F7);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F8, KeyCode.F8);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F9, KeyCode.F9);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F10, KeyCode.F10);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F11, KeyCode.F11);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F12, KeyCode.F12);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F13, KeyCode.F13);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F14, KeyCode.F14);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F15, KeyCode.F15);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F16, KeyCode.F16);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F17, KeyCode.F17);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F18, KeyCode.F18);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F19, KeyCode.F19);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F20, KeyCode.F20);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F21, KeyCode.F21);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F22, KeyCode.F22);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F23, KeyCode.F23);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_F24, KeyCode.F24);
        // 控制键
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_ENTER, KeyCode.ENTER);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_BACKSPACE, KeyCode.BACK_SPACE);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_TAB, KeyCode.TAB);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_CLEAR, KeyCode.CLEAR);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_SHIFT, KeyCode.SHIFT);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_CONTROL, KeyCode.CONTROL);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_ALT, KeyCode.ALT);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_PAUSE, KeyCode.PAUSE);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_ESCAPE, KeyCode.ESCAPE);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_SPACE, KeyCode.SPACE);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_CAPS_LOCK, KeyCode.CAPS);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_NUM_LOCK, KeyCode.NUM_LOCK);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_SCROLL_LOCK, KeyCode.SCROLL_LOCK);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_PAGE_UP, KeyCode.PAGE_UP);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_PAGE_DOWN, KeyCode.PAGE_DOWN);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_HOME, KeyCode.HOME);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_END, KeyCode.END);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_INSERT, KeyCode.INSERT);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_DELETE, KeyCode.DELETE);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_VOLUME_MUTE, KeyCode.MUTE);
        if (isWin) {
            NativeKeyToKeyCode.put(NativeKeyEvent.VC_META, KeyCode.WINDOWS);
        } else {
            NativeKeyToKeyCode.put(NativeKeyEvent.VC_META, KeyCode.META);
        }
        // 方向键
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_UP, KeyCode.UP);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_DOWN, KeyCode.DOWN);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_LEFT, KeyCode.LEFT);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_RIGHT, KeyCode.RIGHT);
        // 符号键
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_COMMA, KeyCode.COMMA);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_PERIOD, KeyCode.PERIOD);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_SLASH, KeyCode.SLASH);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_SEMICOLON, KeyCode.SEMICOLON);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_EQUALS, KeyCode.EQUALS);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_OPEN_BRACKET, KeyCode.OPEN_BRACKET);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_CLOSE_BRACKET, KeyCode.CLOSE_BRACKET);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_BACK_SLASH, KeyCode.BACK_SLASH);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_BACKQUOTE, KeyCode.BACK_QUOTE);
        NativeKeyToKeyCode.put(NativeKeyEvent.VC_MINUS, KeyCode.MINUS);
    }

}
