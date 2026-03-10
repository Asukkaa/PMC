package priv.koishi.pmc.Finals.Enum;

/**
 * 图像识别匹配逻辑枚举类
 *
 * @author KOISHI
 * Date:2025-06-10
 * Time:17:47
 */
public enum MatchedTypeEnum {

    /**
     * 按操作类型设置处理匹配的图像
     */
    CLICK,

    /**
     * 直接执行下一个操作步骤
     */
    BREAK,

    /**
     * 跳转到指定操作步骤
     */
    STEP,

    /**
     * 处理匹配图像后跳转指定步骤
     */
    CLICK_STEP,

    /**
     * 匹配图像存在则重复执行
     */
    CLICK_WHILE

}
