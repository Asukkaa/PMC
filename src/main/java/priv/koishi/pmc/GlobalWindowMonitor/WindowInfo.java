package priv.koishi.pmc.GlobalWindowMonitor;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 窗口信息属性类
 *
 * @author KOISHI
 * Date:2025-09-09
 * Time:14:27
 */
@Data
@Accessors(chain = true)
public class WindowInfo {

    /**
     * 窗口标题
     */
    String title;

    /**
     * 窗口横坐标
     */
    int x;

    /**
     * 窗口纵坐标
     */
    int y;

    /**
     * 窗口宽度
     */
    int width;

    /**
     * 窗口高度
     */
    int height;

}
