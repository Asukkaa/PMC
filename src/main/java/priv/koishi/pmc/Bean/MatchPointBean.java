package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bytedeco.opencv.opencv_core.Point;

/**
 * 图像识别匹配结果类
 *
 * @author KOISHI
 * Date:2025-03-27
 * Time:17:00
 */
@Data
@Accessors(chain = true)
public class MatchPointBean {

    /**
     * 图像识别匹配的坐标
     */
    Point point;

    /**
     * 图像识别匹配值
     */
    int matchThreshold;

}
