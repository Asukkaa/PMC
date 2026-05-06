package priv.koishi.pmc.Bean.Config;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.TessdataBean;

import java.util.List;

import static priv.koishi.pmc.Finals.DefaultConfig.AutoClickDefault.defaultColorTolerance;

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
    private int recognitionType;

    /**
     * 根据识别类型匹配要识别的图像路径/要识别的颜色/要识别的文字
     */
    private String template;

    /**
     * 颜色容差（0-255，默认 {@value priv.koishi.pmc.Finals.DefaultConfig.AutoClickDefault#defaultColorTolerance}）
     */
    private int colorTolerance = defaultColorTolerance;

    /**
     * 文字识别模型设置
     */
    private List<TessdataBean> tessdata;

    /**
     * 最大重试次数
     */
    private int maxRetry;

    /**
     * 最低通过匹配度
     */
    private double matchThreshold;

    /**
     * 是否持续匹配
     */
    private boolean continuously;

    /**
     * 匹配失败后重试时间间隔
     */
    private int retryWait;

    /**
     * 单次匹配最大时间
     */
    private int overTime;

    /**
     * 操作名称
     */
    private String name;

    /**
     * 图像识别设置
     */
    private FloatingWindowConfig floatingWindowConfig;

    /**
     * 当前识别次数
     */
    private int findTime;

}
