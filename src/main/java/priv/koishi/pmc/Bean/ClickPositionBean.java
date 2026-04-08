package priv.koishi.pmc.Bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.AbstractBean.BaseCopyBean;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Finals.Enum.*;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.Serializer.DoubleStringToIntSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.calculateRelativePosition;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.getKeyText;
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
@EqualsAndHashCode(callSuper = true)
public class ClickPositionBean extends BaseCopyBean {

    /**
     * 唯一标识符
     */
    @JsonIgnore
    private String uuid = UUID.randomUUID().toString();

    /**
     * 操作名称
     */
    private String name;

    /**
     * 起始横（X）坐标
     */
    private String startX = "0";

    /**
     * 起始纵（Y）坐标
     */
    private String startY = "0";

    /**
     * 相对横（X）坐标
     */
    private String relativeX;

    /**
     * 相对纵（Y）坐标
     */
    private String relativeY;

    /**
     * 是否启用相对坐标（true 启用，默认禁用）
     */
    private boolean useRelative;

    /**
     * 操作时长（单位：毫秒）
     */
    private String clickTime;

    /**
     * 操作次数
     */
    private String clickNum = "1";

    /**
     * 操作间隔时间（单位：毫秒）
     */
    private String clickInterval = "0";

    /**
     * 操作执行前等待时间（单位：毫秒）
     */
    private String waitTime = "0";

    /**
     * 操作按键
     */
    @JsonIgnore
    private String clickKey;

    /**
     * 鼠标按键枚举值
     */
    private int mouseKeyEnum = NativeMouseEvent.BUTTON1;

    /**
     * 键盘按键枚举值
     */
    private int keyboardKeyEnum = noKeyboard;

    /**
     * 目标图像（根据图像识别类型可以是：图片路径/颜色/文字）
     */
    private String clickImgTarget;

    /**
     * 终止操作的图片
     */
    @JsonSerialize(contentAs = ImgFileBean.class)
    private List<ImgFileBean> stopImgFiles;

    /**
     * 要识别的图片识别匹配阈值
     */
    @JsonSerialize(using = DoubleStringToIntSerializer.class)
    private String clickMatchThreshold = defaultClickOpacity;

    /**
     * 终止操作的图片识别匹配阈值
     */
    @JsonSerialize(using = DoubleStringToIntSerializer.class)
    private String stopMatchThreshold = defaultStopOpacity;

    /**
     * 要识别的图片识别重试次数
     */
    private String clickRetryTimes = defaultClickRetryNum;

    /**
     * 终止操作的图片识别重试次数
     */
    private String stopRetryTimes = defaultStopRetryNum;

    /**
     * 要识别的图像识别重试设置
     */
    @JsonIgnore
    private String retryType;

    /**
     * 要识别的图像识别重试设置枚举值
     */
    private int retryTypeEnum = RetryTypeEnum.STOP.ordinal();

    /**
     * 要识别的图像识别失败后要跳转的步骤序号
     */
    private String retryStep;

    /**
     * 图像识别匹配逻辑
     */
    @JsonIgnore
    private String matchedType;

    /**
     * 图像识别匹配逻辑枚举值
     */
    private int matchedTypeEnum = MatchedTypeEnum.CLICK.ordinal();

    /**
     * 要识别的图像匹配成功后要跳转的步骤序号
     */
    private String matchedStep;

    /**
     * 移动轨迹
     */
    @JsonSerialize(contentAs = TrajectoryPointBean.class)
    private List<TrajectoryPointBean> moveTrajectory = new CopyOnWriteArrayList<>();

    /**
     * 轨迹采样间隔配置（单位：毫秒）
     */
    @JsonIgnore
    private int sampleInterval = Integer.parseInt(defaultSampleInterval);

    /**
     * 操作类型
     */
    @JsonIgnore
    private String clickType;

    /**
     * 操作类型枚举值
     */
    private int clickTypeEnum = ClickTypeEnum.CLICK.ordinal();

    /**
     * 横轴随机偏移量
     */
    private String randomX = defaultRandomClickX;

    /**
     * 纵轴随机偏移量
     */
    private String randomY = defaultRandomClickY;

    /**
     * 是否启用随机点击坐标（默认禁用）
     */
    private boolean randomClick;

    /**
     * 是否启用随机轨迹（默认禁用）
     */
    private boolean randomTrajectory;

    /**
     * 随机偏移时长（单位：毫秒）
     */
    private String randomTime = defaultRandomTime;

    /**
     * 是否启用随机点击时长（true 启用，默认禁用）
     */
    private boolean randomClickTime;

    /**
     * 是否启用随机等待时长（true 启用，默认禁用）
     */
    private boolean randomWaitTime;

    /**
     * 是否启用随机点击间隔（true 启用，默认禁用）
     */
    private boolean randomClickInterval;

    /**
     * 匹配图像坐标横轴偏移量
     */
    private String imgX = "0";

    /**
     * 匹配图像坐标纵轴偏移量
     */
    private String imgY = "0";

    /**
     * 要识别的图像区域设置
     */
    private FloatingWindowConfig clickWindowConfig;

    /**
     * 终止操作图像区域设置
     */
    private FloatingWindowConfig stopWindowConfig;

    /**
     * 要打开的路径
     */
    private String targetPath;

    /**
     * 脚本工作路径
     */
    private String workPath;

    /**
     * 脚本参数
     */
    private String parameter;

    /**
     * 是否启用脚本窗口最小化执行（true 启用，默认启用）
     */
    private boolean minScriptWindow = true;

    /**
     * 是否启用不移动鼠标（true 启用，默认禁用）
     */
    private boolean noMove;

    /**
     * 是否启用忽略移动窗口失败（true 启用，默认启用）
     */
    private boolean ignoreFailure = true;

    /**
     * 是否启用忽略图片识别坐标（true 启用，默认禁用）
     */
    private boolean ignoreImg;

    /**
     * 图像识别类型枚举（0-图像识别 1-颜色识别 2-文字识别）
     */
    private int recognitionType = RecognitionTypeEnum.IMAGE.ordinal();

    /**
     * 颜色容差（0-255）
     */
    private String colorTolerance = String.valueOf(defaultColorTolerance);

    /**
     * 文字识别模型设置
     */
    @JsonSerialize(contentAs = TessdataBean.class)
    private List<TessdataBean> tessdata;

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
     * 获取点击按键
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
                            String s = getKeyText(k);
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
        return getKeyText(keyboardKeyEnum);
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

    /**
     * 更新 UUID
     */
    @Override
    public void updateUuid() {
        uuid = UUID.randomUUID().toString();
    }

}
