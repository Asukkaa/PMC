package priv.koishi.pmc.Callback;

import priv.koishi.pmc.Bean.VO.ClickPositionVO;

import java.util.List;

/**
 * 输入录制回调接口
 *
 * @author KOISHI
 * Date:2025-11-27
 * Time:13:49
 */
public interface InputRecordCallback {

    /**
     * 保存操作步骤
     *
     * @param events  操作步骤
     * @param addType 添加方式
     */
    void saveAddEvents(List<? extends ClickPositionVO> events, int addType);

    /**
     * 更新记录信息
     *
     * @param log 记录信息
     */
    void updateRecordLog(String log);

    /**
     * 更新滑轮记录信息
     *
     * @param wheelRotation 滑轮滚动方向
     * @param wheelNum      滑轮滚动次数
     */
    void onWheelRecorded(int wheelRotation, int wheelNum);

    /**
     * 创建一个具有默认值的自动操作步骤类
     *
     * @return clickPositionVO 具有默认值的自动操作步骤类
     */
    ClickPositionVO createDefaultClickPosition();

    /**
     * 获取当前步骤数
     *
     * @return 当前步骤数
     */
    int getCurrentStepCount();

    /**
     * 获取是否录制鼠标移动事件
     *
     * @return true-记录
     */
    boolean isRecordMove();

    /**
     * 获取是否录制鼠标拖拽事件
     *
     * @return true-记录
     */
    boolean isRecordDrag();

    /**
     * 停止所有任务
     */
    void stopWorkAll();

    /**
     * 显示错误信息
     */
    void showError();

}
