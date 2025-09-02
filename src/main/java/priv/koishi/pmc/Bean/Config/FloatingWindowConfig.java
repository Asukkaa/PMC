package priv.koishi.pmc.Bean.Config;

import lombok.Data;
import lombok.experimental.Accessors;

import static priv.koishi.pmc.Finals.CommonFinals.defaultFloatingHeightInt;
import static priv.koishi.pmc.Finals.CommonFinals.defaultFloatingWidthInt;

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
     * 浮窗X坐标
     */
    int x;

    /**
     * 浮窗Y坐标
     */
    int y;

    /**
     * 浮窗宽度
     */
    int width;

    /**
     * 浮窗高度
     */
    int height;

    /**
     * 图像识别类型枚举
     */
    int findImgTypeEnum;

    /**
     * 获取浮窗宽度
     *
     * @return 浮窗宽度
     */
    public int getWidth() {
        return Math.max(defaultFloatingWidthInt, width);
    }

    /**
     * 获取浮窗高度
     *
     * @return 浮窗高度
     */
    public int getHeight() {
        return Math.max(defaultFloatingHeightInt, height);
    }

}
