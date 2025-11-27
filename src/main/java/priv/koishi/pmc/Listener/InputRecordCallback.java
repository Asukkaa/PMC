package priv.koishi.pmc.Listener;

import priv.koishi.pmc.Bean.VO.ClickPositionVO;

import java.util.List;

/**
 * @author KOISHI
 * Date:2025-11-27
 * Time:13:49
 */
public interface InputRecordCallback {

    void addEventsToTable(List<? extends ClickPositionVO> events, int addType);

    void updateRecordLog(String log);

    void onWheelRecorded(int wheelRotation, int wheelNum);

    ClickPositionVO createDefaultClickPosition();

    int getCurrentStepCount();

    boolean isRecordMove();

    boolean isRecordDrag();

}
