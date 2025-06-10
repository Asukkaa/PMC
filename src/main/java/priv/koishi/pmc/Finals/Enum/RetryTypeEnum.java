package priv.koishi.pmc.Finals.Enum;

/**
 * 要识别的图像识别重试设置枚举类
 *
 * @author KOISHI
 * Date:2025-06-10
 * Time:17:42
 */
public enum RetryTypeEnum {

    /**
     * 重试直到图像出现
     */
    CONTINUOUSLY,

    /**
     * 按设置次数重试后点击设置位置
     */
    CLICK,

    /**
     * 按设置次数重试后终止操作
     */
    STOP,

    /**
     * 按设置次数重试后跳过本次操作
     */
    BREAK,

    /**
     * 按设置次数重试后跳转指定步骤
     */
    STEP

}
