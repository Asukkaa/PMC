package priv.koishi.pmc.Bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Finals.Enum.ClickTypeEnum;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.Serializer.DoubleStringToIntSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.calculateRelativePosition;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.recordClickTypeMap;

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
     * 相对横（X）坐标
     */
    String relativeX;

    /**
     * 相对纵（Y）坐标
     */
    String relativeY;

    /**
     * 是否启用相对坐标 0-不启用，1-启用
     */
    String useRelative = unActivation;

    /**
     * 操作时长（单位：毫秒）
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
     * 鼠标按键枚举值
     */
    int mouseKeyEnum;

    /**
     * 键盘按键枚举值
     */
    int keyboardKeyEnum = noKeyboard;

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
    String randomClick = unActivation;

    /**
     * 是否启用随机轨迹 0-不启用，1-启用
     */
    String randomTrajectory = unActivation;

    /**
     * 随机偏移时长（单位：毫秒）
     */
    String randomTime;

    /**
     * 是否启用随机点击时长 0-不启用，1-启用
     */
    String randomClickTime = unActivation;

    /**
     * 是否启用随机等待时长 0-不启用，1-启用
     */
    String randomWaitTime = unActivation;

    /**
     * 是否启用随机点击间隔 0-不启用，1-启用
     */
    String randomClickInterval = unActivation;

    /**
     * 匹配图像坐标横轴偏移量
     */
    String imgX;

    /**
     * 匹配图像坐标纵轴偏移量
     */
    String imgY;

    /**
     * 要识别的图像区域设置
     */
    FloatingWindowConfig clickWindowConfig;

    /**
     * 终止操作图像区域设置
     */
    FloatingWindowConfig stopWindowConfig;

    /**
     * 要打开的路径
     */
    String targetPath;

    /**
     * 脚本工作路径
     */
    String workPath;

    /**
     * 脚本参数
     */
    String parameter;

    /**
     * 是否启用脚本窗口最小化执行 0-不启用，1-启用
     */
    String minScriptWindow = activation;

    /**
     * 添加移动轨迹
     *
     * @param x                 横坐标
     * @param y                 纵坐标
     * @param pressMouseKeys    当前按下的鼠标按键
     * @param pressKeyboardKeys 当前按下的键盘按键
     * @param isDragging        是否是拖拽（true-拖拽，false-普通移动）
     * @param wheelRotation     滑轮滚动量
     */
    public void addMovePoint(int x, int y, List<Integer> pressMouseKeys, List<Integer> pressKeyboardKeys,
                             boolean isDragging, int wheelRotation) {
        long timestamp = System.currentTimeMillis();
        if (!moveTrajectory.isEmpty()) {
            TrajectoryPointBean last = moveTrajectory.getLast();
            double distance = Math.sqrt(Math.pow(x - last.getX(), 2) + Math.pow(y - last.getY(), 2));
            // 最小像素距离阈值，拖拽记录结束时不校验
            if ((distance < 1 && isDragging) && (pressMouseKeys != null && pressKeyboardKeys != null) && wheelRotation == 0) {
                return;
            }
        }
        // 移动轨迹为空时认为是轨迹起点，直接添加起始轨迹点
        if (moveTrajectory.isEmpty()
                // 只有时间间隔超过采样间隔时才添加轨迹点
                || (timestamp - moveTrajectory.getLast().getTimestamp() >= sampleInterval)
                // 拖拽时如果轨迹点为空则认为是结束拖拽，直接添加结束轨迹点
                || (isDragging && pressMouseKeys == null && pressKeyboardKeys == null)
                // 有滑轮事件时总是记录
                || wheelRotation != 0) {
            TrajectoryPointBean trajectoryPointBean = new TrajectoryPointBean()
                    .setPressKeyboardKeys(pressKeyboardKeys)
                    .setPressMouseKeys(pressMouseKeys)
                    .setWheelRotation(wheelRotation)
                    .setTimestamp(timestamp)
                    .setX(x)
                    .setY(y);
            if (clickWindowConfig != null &&
                    FindImgTypeEnum.WINDOW.ordinal() == clickWindowConfig.getFindImgTypeEnum()) {
                WindowInfo windowInfo = clickWindowConfig.getWindowInfo();
                if (windowInfo != null) {
                    trajectoryPointBean.updatePosition(windowInfo);
                }
            }
            moveTrajectory.add(trajectoryPointBean);
        }
    }

    /**
     * 换算相对坐标
     */
    public void updateRelativePosition() {
        if (clickWindowConfig != null &&
                FindImgTypeEnum.WINDOW.ordinal() == clickWindowConfig.getFindImgTypeEnum()) {
            WindowInfo windowInfo = clickWindowConfig.getWindowInfo();
            if (windowInfo != null) {
                if (StringUtils.isNotBlank(startX) && StringUtils.isNotBlank(startY)) {
                    Map<String, String> relativePosition = calculateRelativePosition(windowInfo,
                            Integer.parseInt(startX), Integer.parseInt(startY));
                    relativeX = relativePosition.get(RelativeX);
                    relativeY = relativePosition.get(RelativeY);
                }
            }
        }
    }

    /**
     * 获取点击按键（通过反射调用）
     *
     * @return 点击按键对应的文本
     */
    public String getClickKey() {
        // 处理组合键的显示
        if (clickTypeEnum == ClickTypeEnum.COMBINATIONS.ordinal()) {
            String combinationsKeys;
            if (CollectionUtils.isNotEmpty(moveTrajectory)) {
                Set<String> keySet = new LinkedHashSet<>();
                for (TrajectoryPointBean t : moveTrajectory) {
                    List<Integer> pressMouseKeys = t.getPressMouseKeys();
                    if (CollectionUtils.isNotEmpty(pressMouseKeys)) {
                        for (Integer m : pressMouseKeys) {
                            String s = recordClickTypeMap.get(m);
                            if (StringUtils.isNoneBlank(s)) {
                                keySet.add(s);
                            }
                        }
                    }
                    List<Integer> pressKeyboardKeys = t.getPressKeyboardKeys();
                    if (CollectionUtils.isNotEmpty(pressKeyboardKeys)) {
                        for (Integer k : pressKeyboardKeys) {
                            String s = NativeKeyEvent.getKeyText(k);
                            if (StringUtils.isNoneBlank(s)) {
                                keySet.add(s);
                            }
                        }
                    }
                    int wheelRotation = t.getWheelRotation();
                    if (wheelRotation != 0) {
                        keySet.add(wheelRotation < 0 ? clickType_wheelUp() : clickType_wheelDown());
                    }
                }
                combinationsKeys = String.join(" ", keySet);
                return combinationsKeys;
            }
            // 如果没有键盘按键则获取鼠标按键
        } else if (keyboardKeyEnum == noKeyboard) {
            return getMouseKey();
        }
        // 获取键盘按键
        return getKeyboardKey();
    }

    /**
     * 获取鼠标操作按键
     *
     * @return 鼠标操作按键对应的文本
     */
    @JsonIgnore
    public String getMouseKey() {
        return recordClickTypeMap.get(mouseKeyEnum);
    }

    /**
     * 获取键盘操作按键
     *
     * @return 键盘操作按键对应的文本
     */
    @JsonIgnore
    public String getKeyboardKey() {
        return NativeKeyEvent.getKeyText(keyboardKeyEnum);
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
