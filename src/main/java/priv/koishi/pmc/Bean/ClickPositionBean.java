package priv.koishi.pmc.Bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Serializer.DoubleStringToIntSerializer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static priv.koishi.pmc.Finals.i18nFinal.*;

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
    @JsonIgnore
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
     * 操作按键
     */
    @JsonIgnore
    String clickKey;

    /**
     * 操作按键枚举值
     */
    int clickKeyEnum;

    /**
     * 要识别的图片路径
     */
    String clickImgPath;

    /**
     * 终止操作的图片
     */
    @JsonSerialize(contentAs = ImgFileBean.class)
    List<ImgFileBean> stopImgFiles;

    /**
     * 要识别的图片识别匹配阈值
     */
    @JsonSerialize(using = DoubleStringToIntSerializer.class)
    String clickMatchThreshold;

    /**
     * 终止操作的图片识别匹配阈值
     */
    @JsonSerialize(using = DoubleStringToIntSerializer.class)
    String stopMatchThreshold;

    /**
     * 要识别的图片识别重试次数
     */
    String clickRetryTimes;

    /**
     * 终止操作的图片识别重试次数
     */
    String stopRetryTimes;

    /**
     * 要识别的图像识别重试设置
     */
    @JsonIgnore
    String retryType;

    /**
     * 要识别的图像识别重试设置枚举值
     */
    int retryTypeEnum;

    /**
     * 要识别的图像识别失败后要跳转的步骤序号
     */
    String retryStep;

    /**
     * 图像识别匹配逻辑
     */
    @JsonIgnore
    String matchedType;

    /**
     * 图像识别匹配逻辑枚举值
     */
    int matchedTypeEnum;

    /**
     * 要识别的图像匹配成功后要跳转的步骤序号
     */
    String matchedStep;

    /**
     * 移动轨迹
     */
    @JsonSerialize(contentAs = TrajectoryPointBean.class)
    List<TrajectoryPointBean> moveTrajectory = new CopyOnWriteArrayList<>();

    /**
     * 轨迹采样间隔配置（单位：毫秒）
     */
    @JsonIgnore
    int sampleInterval;

    /**
     * 操作类型
     */
    @JsonIgnore
    String clickType;

    /**
     * 操作类型枚举值
     */
    int clickTypeEnum;

    /**
     * 横轴随机偏移量
     */
    String randomX;

    /**
     * 纵轴随机偏移量
     */
    String randomY;

    /**
     * 是否启用随机点击坐标 0-不启用，1-启用
     */
    String randomClick;

    /**
     * 是否启用随机轨迹 0-不启用，1-启用
     */
    String randomTrajectory;

    /**
     * 随机偏移时长（单位：毫秒）
     */
    String randomTime;

    /**
     * 是否启用随机点击时长 0-不启用，1-启用
     */
    String randomClickTime;

    /**
     * 是否启用随机等待时长 0-不启用，1-启用
     */
    String randomWaitTime;

    /**
     * 是否启用随机点击间隔 0-不启用，1-启用
     */
    String randomClickInterval;

    /**
     * 添加移动轨迹
     *
     * @param x            横坐标
     * @param y            纵坐标
     * @param pressButtons 当前按下的按键
     * @param isDragging   是否是拖拽（true-拖拽，false-普通移动）
     */
    public void addMovePoint(int x, int y, List<Integer> pressButtons, boolean isDragging) {
        long timestamp = System.currentTimeMillis();
        if (!moveTrajectory.isEmpty()) {
            TrajectoryPointBean last = moveTrajectory.getLast();
            double distance = Math.sqrt(Math.pow(x - last.getX(), 2) + Math.pow(y - last.getY(), 2));
            // 最小像素距离阈值，拖拽记录结束时不校验
            if (distance < 1 && (!isDragging || pressButtons != null)) {
                return;
            }
        }
        // 移动轨迹为空时认为是轨迹起点，直接添加起始轨迹点
        if (moveTrajectory.isEmpty()
                // 只有时间间隔超过采样间隔时才添加轨迹点
                || (timestamp - moveTrajectory.getLast().getTimestamp() >= sampleInterval)
                // 拖拽时如果轨迹点为空则认为是结束拖拽，直接添加结束轨迹点
                || (isDragging && pressButtons == null)) {
            TrajectoryPointBean trajectoryPointBean = new TrajectoryPointBean();
            trajectoryPointBean.setPressButtons(pressButtons)
                    .setTimestamp(timestamp)
                    .setX(x)
                    .setY(y);
            moveTrajectory.add(trajectoryPointBean);
        }
    }

    /**
     * 获取操作按键
     *
     * @return 操作按键对应的文本
     */
    public String getClickKey() {
        return recordClickTypeMap.get(clickKeyEnum);
    }

    /**
     * 获取要识别的图像识别重试设置
     *
     * @return 要识别的图像识别重试设置对应的文本
     */
    public String getRetryType() {
        return retryTypeMap.get(retryTypeEnum);
    }

    /**
     * 获取图像识别匹配逻辑
     *
     * @return 图像识别匹配逻辑对应的文本
     */
    public String getMatchedType() {
        return matchedTypeMap.get(matchedTypeEnum);
    }

    /**
     * 获取操作类型
     *
     * @return 操作类型对应的文本
     */
    public String getClickType() {
        return clickTypeMap.get(clickTypeEnum);
    }

}
