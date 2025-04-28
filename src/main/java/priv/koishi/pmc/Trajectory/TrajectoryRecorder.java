package priv.koishi.pmc.Trajectory;

import priv.koishi.pmc.Bean.VO.ClickPositionVO;

/**
 * 轨迹记录工具类
 *
 * @author KOISHI
 * Date:2025-04-28
 * Time:15:58
 */
public class TrajectoryRecorder {

    /**
     * 自动操作步骤展示类
     */
    private final ClickPositionVO clickBean;

    /**
     * 是否正在录制
     */
    private volatile boolean isRecording = true;

    /**
     * 构造函数
     *
     * @param clickBean 自动操作步骤展示类
     */
    public TrajectoryRecorder(ClickPositionVO clickBean) {
        this.clickBean = clickBean;
    }

    /**
     * 添加移动点
     *
     * @param x x坐标
     * @param y y坐标
     */
    public void recordMovePoint(int x, int y) {
        if (isRecording) {
            clickBean.addMovePoint(x, y);
        }
    }

    /**
     * 停止记录
     */
    public void stopRecording() {
        isRecording = false;
    }

}
