package priv.koishi.pmc.Finals.Enum;

import lombok.Getter;

/**
 * 窗口列表选项枚举类
 *
 * @author applesaucepenguin
 * Date 2025-09-11
 * time 17:33
 */
@Getter
public enum WindowListOptionEnum {

    /**
     * 包含所有窗口
     */
    ALL(0),

    /**
     * 仅包含当前屏幕上的窗口
     */
    ON_SCREEN_ONLY(1),

    /**
     * 包含屏幕上指定窗口之上的窗口
     */
    ON_SCREEN_ABOVE_WINDOW(2),

    /**
     * 包含屏幕上指定窗口之下的窗口
     */
    ON_SCREEN_BELOW_WINDOW(4),

    /**
     * 排除桌面元素（如 Dock、菜单栏等）
     */
    EXCLUDE_DESKTOP_ELEMENTS(0x10);

    /**
     * 枚举值
     */
    private final int value;

    /**
     * 构造函数
     *
     * @param value 枚举值
     */
    WindowListOptionEnum(int value) {
        this.value = value;
    }

}
