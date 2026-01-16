package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.text.DecimalFormat;

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
     * 窗口唯一标识（Windows 为 HWND 的数值，macOS 为 CGWindowID）
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
     * 窗口进程 ID（-1 表示应用未启动）
     */
    int pid = -1;

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

    /**
     * 识别范围相对高度
     */
    double relativeHeight = 1;

    /**
     * 识别范围相对宽度
     */
    double relativeWidth = 1;

    /**
     * 识别范围相对纵坐标
     */
    double relativeY = 0;

    /**
     * 识别范围相对横坐标
     */
    double relativeX = 0;

    /**
     * 更新识别范围相对横坐标
     *
     * @param absoluteX 绝对横坐标
     * @return 用于链式调用的自身对象
     */
    public WindowInfo updateRelativeX(int absoluteX) {
        DecimalFormat df = new DecimalFormat("#.####");
        relativeX = Double.parseDouble(df.format((double) (absoluteX - x) / width));
        return this;
    }

    /**
     * 更新识别范围相对纵坐标
     *
     * @param absoluteY 绝对纵坐标
     */
    public void updateRelativeY(int absoluteY) {
        DecimalFormat df = new DecimalFormat("#.####");
        relativeY = Double.parseDouble(df.format((double) (absoluteY - y) / height));
    }

    /**
     * 更新识别范围相对宽度
     *
     * @param absoluteWidth 绝对宽度
     * @return 用于链式调用的自身对象
     */
    public WindowInfo updateRelativeWidth(int absoluteWidth) {
        DecimalFormat df = new DecimalFormat("#.####");
        relativeWidth = Double.parseDouble(df.format((double) absoluteWidth / width));
        return this;
    }

    /**
     * 更新识别范围相对高度
     *
     * @param absoluteHeight 绝对高度
     * @return 用于链式调用的自身对象
     */
    public WindowInfo updateRelativeHeight(int absoluteHeight) {
        DecimalFormat df = new DecimalFormat("#.####");
        relativeHeight = Double.parseDouble(df.format((double) absoluteHeight / height));
        return this;
    }

}
