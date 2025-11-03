package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;

import java.util.List;
import java.util.Map;

import static priv.koishi.pmc.Finals.CommonFinals.RelativeX;
import static priv.koishi.pmc.Finals.CommonFinals.RelativeY;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.calculateRelativePosition;

/**
 * 鼠标移动轨迹点
 *
 * @author KOISHI
 * Date:2025-04-28
 * Time:15:48
 */
@Data
@Accessors(chain = true)
public class TrajectoryPointBean {

    /**
     * 轨迹点时间戳
     */
    long timestamp;

    /**
     * 轨迹点横坐标
     */
    double x;

    /**
     * 轨迹点纵坐标
     */
    double y;

    /**
     * 轨迹点相对横（X）坐标
     */
    String relativeX;

    /**
     * 轨迹点相对纵（Y）坐标
     */
    String relativeY;

    /**
     * 按下的键
     */
    List<Integer> pressButtons;

    /**
     * 滑轮状态（正数上滑，负数下滑，0 为无滑动状态）
     */
    int wheelRotation;

    /**
     * 换算绝对和相对坐标
     */
    public void updatePosition(WindowInfo windowInfo) {
        if (windowInfo != null) {
            Map<String, String> relativePosition = calculateRelativePosition(windowInfo, (int) x, (int) y);
            relativeX = relativePosition.get(RelativeX);
            relativeY = relativePosition.get(RelativeY);
        }
    }

}
