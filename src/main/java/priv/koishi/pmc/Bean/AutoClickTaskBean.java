package priv.koishi.pmc.Bean;

import javafx.animation.Timeline;
import javafx.scene.control.Label;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;

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
    int loopTime;

    /**
     * 执行自动流程前点击第一个起始坐标
     */
    boolean firstClick;

    /**
     * 执行自动操作时的信息输出栏
     */
    Label floatingLabel;

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

}
