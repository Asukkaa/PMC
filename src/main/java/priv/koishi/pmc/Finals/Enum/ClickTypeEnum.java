package priv.koishi.pmc.Finals.Enum;

/**
 * 操作类型枚举类
 *
 * @author KOISHI
 * Date:2025-06-10
 * Time:17:55
 */
public enum ClickTypeEnum {

    /**
     * 打开文件
     */
    OPEN_FILE,

    /**
     * 运行脚本
     */
    RUN_SCRIPT,

    /**
     * 打开网址
     */
    OPEN_URL,

    /**
     * 点击后松开
     */
    CLICK,

    /**
     * 拖拽
     */
    DRAG,

    /**
     * 带轨迹的移动
     */
    MOVE_TRAJECTORY,

    /**
     * 仅移动
     */
    MOVE,

    /**
     * 移动到设置坐标
     */
    MOVETO,

    /**
     * 滚轮上滑
     */
    WHEEL_UP,

    /**
     * 滚轮下滑
     */
    WHEEL_DOWN,

    /**
     * 键盘输入
     */
    KEYBOARD,

    /**
     * 组合键输入
     */
    COMBINATIONS

}
