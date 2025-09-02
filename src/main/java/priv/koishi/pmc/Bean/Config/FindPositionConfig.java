package priv.koishi.pmc.Bean.Config;

import lombok.Data;
import lombok.experimental.Accessors;

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
     * 要识别的图像路径
     */
    String templatePath;

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

}
