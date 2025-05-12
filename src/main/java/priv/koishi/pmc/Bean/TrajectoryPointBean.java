package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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
     * 按下的键
     */
    List<Integer> pressButtons;

}
