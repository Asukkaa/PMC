package priv.koishi.pmc.Bean.Config;

import lombok.Data;
import lombok.experimental.Accessors;

import static priv.koishi.pmc.Finals.CommonFinals.defaultColorTolerance;

/**
 * 图像识别设置类
 *
 * @author KOISHI
 * Date:2025-03-27
 * Time:19:49
 */
@Data
@Accessors(chain = true)
public class FindPositionConfig {

    /**
     * 图像识别类型枚举（0-图像识别 1-颜色识别 2-文字识别）
     */
    int recognitionType;

    /**
     * 根据识别类型匹配要识别的图像路径/要识别的颜色/要识别的文字
     */
    String template;

    /**
     * 颜色容差（0-255）
     */
    int colorTolerance = defaultColorTolerance;

    /**
     * OCR 语言，默认简体中文
     */
    String ocrLanguage = "chi_sim";

    /**
     * 最大重试次数
     */
    int maxRetry;

    /**
     * 最低通过匹配度
     */
    double matchThreshold;

    /**
     * 是否持续匹配
     */
    boolean continuously;

    /**
     * 匹配失败后重试时间间隔
     */
    int retryWait;

    /**
     * 单次匹配最大时间
     */
    int overTime;

    /**
     * 操作名称
     */
    String name;

    /**
     * 图像识别设置
     */
    FloatingWindowConfig floatingWindowConfig;

    /**
     * 当前识别次数
     */
    int findTime;

}
