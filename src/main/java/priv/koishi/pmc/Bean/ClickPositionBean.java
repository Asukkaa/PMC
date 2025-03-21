package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
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
     * 要点击的图片路径
     */
    String clickImgPath;

    /**
     * 上次所选要点击的图片路径
     */
    String clickImgSelectPath;

    /**
     * 终止操作的图片路径
     */
    List<String> stopImgPaths;

    /**
     * 上次所选终止操作的图片路径
     */
    String stopImgSelectPath;

    /**
     * 要点击的图片识别匹配阈值
     */
    String clickMatchThreshold;

    /**
     * 终止操作的图片识别匹配阈值
     */
    String stopMatchThreshold;

    /**
     * 图像识别匹配最大时长（单位：秒）
     */
    String overtime;

    /**
     * 要点击的图片识别重试次数
     */
    String clickRetryTimes;

    /**
     * 终止操作的图片识别重试次数
     */
    String stopRetryTimes;

    /**
     * 详情页删除标志
     */
    boolean remove;

}
