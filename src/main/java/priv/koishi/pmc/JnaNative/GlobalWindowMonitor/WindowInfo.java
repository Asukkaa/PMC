package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

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
     * 窗口唯一标识（Windows为HWND的数值，macOS为CGWindowID）
     */
    long id;

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

    /**
     * 窗口进程ID
     */
    int pid;

    /**
     * 窗口层级
     */
    int layer;

    /**
     * 窗口进程名称
     */
    String processName;

    /**
     * 窗口进程路径
     */
    String processPath;

    /**
     * 窗口标题
     */
    String title;

}
