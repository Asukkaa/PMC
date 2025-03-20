package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * 自动操作步骤类
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:45
 */
@Data
@Accessors(chain = true)
public class ClickPositionBean {

    /**
     * 唯一标识符
     */
    String uuid = UUID.randomUUID().toString();

    /**
     * 操作名称
     */
    String name;

    /**
     * 起始横（X）坐标
     */
    String startX;

    /**
     * 起始纵（Y）坐标
     */
    String startY;

    /**
     * 结束横（X）坐标
     */
    String endX;

    /**
     * 结束纵（Y）坐标
     */
    String endY;

    /**
     * 点击时长（单位：毫秒）
     */
    String clickTime;

    /**
     * 操作次数
     */
    String clickNum;

    /**
     * 操作间隔时间（单位：毫秒）
     */
    String clickInterval;

    /**
     * 操作执行前等待时间（单位：毫秒）
     */
    String waitTime;

    /**
     * 操作类型
     */
    String type;

    /**
     * 图像识别模板图片路径
     */
    String templatePath;

    /**
     * 匹配阈值
     */
    double matchThreshold;

    /**
     * 匹配最大时常（单位：秒）
     */
    String overtime;

}
