package priv.koishi.pmc.Bean;

import javafx.animation.Timeline;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;

/**
 * 自动操作工具多线程任务所需设置类
 *
 * @author KOISHI
 * Date:2025-02-26
 * Time:17:42
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AutoClickTaskBean extends TaskBean<ClickPositionVO> {

    /**
     * 自动点击任务循环次数
     */
    int loopTimes;

    /**
     * 执行自动流程前点击第一个起始坐标
     */
    boolean firstClick;

    /**
     * 执行自动操作时的信息输出栏
     */
    FloatingWindowDescriptor massageFloating;

    /**
     * 运行时间线
     */
    Timeline runTimeline;

    /**
     * 匹配失败后重试时间间隔
     */
    int retrySecondValue;

    /**
     * 单次匹配最大时间
     */
    int overTimeValue;

    /**
     * 运行自动流程时记录点击信息
     */
    boolean clickLog;

    /**
     * 运行自动流程时记录移动轨迹
     */
    boolean moveLog;

    /**
     * 运行自动流程时记录拖拽轨迹
     */
    boolean dragLog;

    /**
     * 运行自动流程时记目标图像识别
     */
    boolean clickImgLog;

    /**
     * 运行自动流程时记终止图像识别
     */
    boolean stopImgLog;

    /**
     * 运行自动流程时记录等待信息
     */
    boolean waitLog;

    /**
     * 运行自动流程时记录打开文件事件
     */
    boolean openFileLog;

    /**
     * 运行自动流程时记录运行脚本事件
     */
    boolean runScriptLog;

    /**
     * 运行自动流程时记录打开网址事件
     */
    boolean openUrlLog;

    /**
     * 最大记录数量
     */
    int maxLogNum;

}
