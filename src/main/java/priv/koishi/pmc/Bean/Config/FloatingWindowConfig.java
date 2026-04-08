package priv.koishi.pmc.Bean.Config;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;

/**
 * 浮窗配置类
 *
 * @author KOISHI
 * Date:2025-09-02
 * Time:15:44
 */
@Data
@Accessors(chain = true)
public class FloatingWindowConfig {

    /**
     * 浮窗 X 坐标
     */
    private int x;

    /**
     * 浮窗 Y 坐标
     */
    private int y;

    /**
     * 浮窗宽度
     */
    private int width;

    /**
     * 浮窗高度
     */
    private int height;

    /**
     * 图像识别类型枚举
     */
    private int findImgTypeEnum;

    /**
     * 第一次识别失败后改为识别整个屏幕（true 改变识别范围，默认禁用）
     */
    private boolean allRegion;

    /**
     * 目标窗口信息
     */
    private WindowInfo windowInfo;

    /**
     * 窗口是否需要实时刷新（true 开启实时刷新，默认禁用）
     */
    private boolean alwaysRefresh;

}
