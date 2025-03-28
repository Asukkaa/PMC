package priv.koishi.pmc.Bean;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

/**
 * 自动操作步骤类
 *
 * @author KOISHI
 * Date:2025-03-28
 * Time:13:12
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
     * 终止操作的图片
     */
    @JsonSerialize(contentAs = ImgFileBean.class)
    List<ImgFileBean> stopImgFiles;

    /**
     * 要点击的图片识别匹配阈值
     */
    String clickMatchThreshold;

    /**
     * 终止操作的图片识别匹配阈值
     */
    String stopMatchThreshold;

    /**
     * 要点击的图片识别重试次数
     */
    String clickRetryTimes;

    /**
     * 终止操作的图片识别重试次数
     */
    String stopRetryTimes;

    /**
     * 要点击的图像识别重试设置
     */
    String retryType;

    /**
     * 匹配到图像后直接进行下一个操作步骤
     */
    boolean skip;


}
