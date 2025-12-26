package priv.koishi.pmc.Listener;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import javafx.application.Platform;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Callback.InputRecordCallback;
import priv.koishi.pmc.Finals.Enum.ClickTypeEnum;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static priv.koishi.pmc.Finals.CommonFinals.noAdd;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.recordClickTypeMap;
import static priv.koishi.pmc.Utils.ListenerUtils.addNativeListener;
import static priv.koishi.pmc.Utils.ListenerUtils.removeNativeListener;

/**
 * 统一输入事件录制监听器（同时录制鼠标和键盘）
 *
 * @author KOISHI
 * Date:2025-11-27
 * Time:13:47
 */
public class UnifiedInputRecordListener implements NativeMouseListener, NativeMouseMotionListener,
        NativeMouseWheelListener, NativeKeyListener {

    /**
     * 记录按键按下时刻
     */
    private long pressTime;

    /**
     * 记标按键松开时刻
     */
    private long releasedTime;

    /**
     * 首次点击标记
     */
    private boolean isFirstClick = true;

    /**
     * 添加类型
     */
    private final int addType;

    /**
     * 开始移动时刻
     */
    private long startMoveTime;

    /**
     * 回调函数
     */
    private final InputRecordCallback callback;

    /**
     * 录制开始时间
     */
    private long recordingStartTime;

    /**
     * 正在录制标志
     */
    private boolean isRecording;

    /**
     * 鼠标移动记录器
     */
    private ClickPositionVO movePoint;

    /**
     * 带点击的步骤
     */
    private ClickPositionVO clickBean;

    /**
     * 正在记录鼠标移动轨迹
     */
    private boolean hasPendingMoveTrajectory;

    /**
     * 当前鼠标按下的按键
     */
    private final List<Integer> pressMouseButtons = new CopyOnWriteArrayList<>();

    /**
     * 当前键盘按下的按键
     */
    private final List<Integer> pressKeyboardKeys = new CopyOnWriteArrayList<>();

    /**
     * 构造函数
     *
     * @param addType  添加类型
     * @param callback 回调函数
     */
    public UnifiedInputRecordListener(int addType, InputRecordCallback callback) {
        this.addType = addType;
        this.callback = callback;
        movePoint = callback.createDefaultClickPosition();
    }

    /**
     * 鼠标拖拽监听器
     */
    public final NativeMouseMotionListener dragMotionListener = new NativeMouseMotionListener() {

        /**
         * 鼠标按下时拖拽监听
         *
         * @param e 鼠标拖拽事件
         */
        @Override
        public void nativeMouseDragged(NativeMouseEvent e) {
            if (isRecording && callback.isRecordDrag()) {
                Point mousePoint = MousePositionListener.getMousePoint();
                int x = (int) mousePoint.getX();
                int y = (int) mousePoint.getY();
                List<Integer> currentMouseButtons = new CopyOnWriteArrayList<>(pressMouseButtons);
                List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                if (clickBean != null) {
                    clickBean.addMovePoint(x, y, currentMouseButtons, currentKeyboardKeys, true, 0);
                    clickBean.setClickTypeEnum(ClickTypeEnum.DRAG.ordinal());
                }
            }
        }

        /**
         * 键盘按下时鼠标移动监听
         *
         * @param e 鼠标移动事件
         */
        @Override
        public void nativeMouseMoved(NativeMouseEvent e) {
            // 键盘按下时移动鼠标也视为拖拽
            if (isRecording && callback.isRecordDrag() && !pressKeyboardKeys.isEmpty()) {
                Point mousePoint = MousePositionListener.getMousePoint();
                int x = (int) mousePoint.getX();
                int y = (int) mousePoint.getY();
                List<Integer> currentMouseButtons = new CopyOnWriteArrayList<>(pressMouseButtons);
                List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                if (clickBean != null) {
                    clickBean.addMovePoint(x, y, currentMouseButtons, currentKeyboardKeys, true, 0);
                    clickBean.setClickTypeEnum(ClickTypeEnum.DRAG.ordinal());
                }
            }
        }
    };

    /**
     * 鼠标移动监听器
     */
    public final NativeMouseMotionListener moveMotionListener = new NativeMouseMotionListener() {

        /**
         * 无按键按下时鼠标移动监听
         *
         * @param e 鼠标移动事件
         */
        @Override
        public void nativeMouseMoved(NativeMouseEvent e) {
            if (isRecording && callback.isRecordMove()) {
                Point mousePoint = MousePositionListener.getMousePoint();
                int x = (int) mousePoint.getX();
                int y = (int) mousePoint.getY();
                if (movePoint != null) {
                    movePoint.addMovePoint(x, y, null, null, false, 0);
                }
                hasPendingMoveTrajectory = true;
            }
        }
    };

    /**
     * 监听鼠标按下
     *
     * @param e 鼠标按下事件
     */
    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        if (isRecording) {
            int pressButton = e.getButton();
            // 避免重复记录（如果按键已经按下）
            if (!pressMouseButtons.contains(pressButton)) {
                // 停止移动轨迹记录
                if (callback.isRecordMove()) {
                    removeNativeListener(moveMotionListener);
                }
                // 记录按下时刻的时间戳
                pressTime = System.currentTimeMillis();
                long waitTime;
                // 记录移动轨迹时因为点击和移动是分开的两个步骤，所以点击不用再等待一次移动的时间
                if (callback.isRecordMove()) {
                    waitTime = 0;
                } else {
                    waitTime = isFirstClick ?
                            pressTime - recordingStartTime :
                            pressTime - releasedTime;
                }
                Point mousePoint = MousePositionListener.getMousePoint();
                int startX = (int) mousePoint.getX();
                int startY = (int) mousePoint.getY();
                // 添加移动轨迹到表格
                addMoveTrajectory(startX, startY);
                // 创建点击位置对象
                if (pressKeyboardKeys.isEmpty() && pressMouseButtons.isEmpty()) {
                    clickBean = callback.createDefaultClickPosition();
                    int index = callback.getCurrentStepCount() + 1;
                    clickBean.setName(text_step() + index + text_isRecord())
                            .setWaitTime(String.valueOf(waitTime))
                            .setStartX(String.valueOf(startX))
                            .setStartY(String.valueOf(startY))
                            .setMouseKeyEnum(pressButton);
                } else {
                    clickBean.setClickTypeEnum(ClickTypeEnum.COMBINATIONS.ordinal());
                }
                // 记录按下的鼠标按键
                pressMouseButtons.add(pressButton);
                String log = text_cancelTask() + text_recordClicking() + "\n" +
                        text_recorded() + recordClickTypeMap.get(pressButton) + " " + log_press();
                Platform.runLater(() -> callback.updateRecordLog(log));
                // 如果是组合按键的开始，记录轨迹点
                if (clickBean != null) {
                    // 创建轨迹点记录组合按键
                    List<Integer> currentMouseButtons = new CopyOnWriteArrayList<>(pressMouseButtons);
                    List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                    clickBean.addMovePoint(startX, startY, currentMouseButtons, currentKeyboardKeys, false, 0);
                }
                // 开始拖拽轨迹记录
                if (callback.isRecordDrag()) {
                    addNativeListener(dragMotionListener);
                }
            }
        }
    }

    /**
     * 监听鼠标松开
     *
     * @param e 鼠标松开事件
     */
    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        if (isRecording && clickBean != null) {
            // 记录松开的鼠标按键
            pressMouseButtons.remove(Integer.valueOf(e.getButton()));
            Point mousePoint = MousePositionListener.getMousePoint();
            int endX = (int) mousePoint.getX();
            int endY = (int) mousePoint.getY();
            // 所有按键都松开后停止拖拽轨迹记录
            if (callback.isRecordDrag() && pressKeyboardKeys.isEmpty() && pressMouseButtons.isEmpty()) {
                removeNativeListener(dragMotionListener);
                // 拖拽结束时添加释放鼠标的坐标
                clickBean.addMovePoint(endX, endY, null, null, true, 0);
            } else {
                // 创建轨迹点记录组合按键
                List<Integer> currentMouseButtons = new CopyOnWriteArrayList<>(pressMouseButtons);
                List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                clickBean.addMovePoint(endX, endY, currentMouseButtons, currentKeyboardKeys, false, 0);
            }
            isFirstClick = false;
            // 记录移动轨迹
            if (callback.isRecordMove()) {
                movePoint = callback.createDefaultClickPosition();
                // 所有按键都松开后开始移动轨迹记录
                if (pressKeyboardKeys.isEmpty() && pressMouseButtons.isEmpty()) {
                    startMoveTime = System.currentTimeMillis();
                    addNativeListener(moveMotionListener);
                }
            }
            // 只有在所有鼠标按键都松开时才算一个完整的操作步骤
            if (pressMouseButtons.isEmpty() && pressKeyboardKeys.isEmpty()) {
                releasedTime = System.currentTimeMillis();
                // 计算点击持续时间（毫秒）
                long duration = releasedTime - pressTime;
                // 设置点击持续时间
                clickBean.setClickTime(String.valueOf(duration))
                        .updateRelativePosition();
                // 添加到表格
                addMouseEventToTable(clickBean, endX, endY);
            }
        }
    }

    /**
     * 键盘按下监听器
     *
     * @param e 键盘按下事件
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (isRecording) {
            int keyCode = e.getKeyCode();
            // 避免重复记录（如果按键已经按下）
            if (!pressKeyboardKeys.contains(keyCode)) {
                // 停止移动轨迹记录
                if (callback.isRecordMove()) {
                    removeNativeListener(moveMotionListener);
                }
                // 记录按下时刻的时间戳
                pressTime = System.currentTimeMillis();
                long waitTime;
                // 记录移动轨迹时因为点击和移动是分开的两个步骤，所以点击不用再等待一次移动的时间
                if (callback.isRecordMove()) {
                    waitTime = 0;
                } else {
                    waitTime = isFirstClick ?
                            pressTime - recordingStartTime :
                            pressTime - releasedTime;
                }
                // 记录按下的坐标
                Point mousePoint = MousePositionListener.getMousePoint();
                int startX = (int) mousePoint.getX();
                int startY = (int) mousePoint.getY();
                // 添加移动轨迹到表格
                addMoveTrajectory(startX, startY);
                // 创建点击位置对象
                if (pressKeyboardKeys.isEmpty() && pressMouseButtons.isEmpty()) {
                    clickBean = callback.createDefaultClickPosition();
                    int index = callback.getCurrentStepCount() + 1;
                    clickBean.setClickTypeEnum(ClickTypeEnum.KEYBOARD.ordinal())
                            .setName(text_step() + index + text_isRecord())
                            .setWaitTime(String.valueOf(waitTime))
                            .setStartX(String.valueOf(startX))
                            .setStartY(String.valueOf(startY))
                            .setKeyboardKeyEnum(keyCode);
                } else {
                    clickBean.setClickTypeEnum(ClickTypeEnum.COMBINATIONS.ordinal());
                }
                // 记录按下的按键
                pressKeyboardKeys.add(keyCode);
                String log = text_cancelTask() + text_recordClicking() + "\n" +
                        text_recorded() + NativeKeyEvent.getKeyText(keyCode) + " " + log_press();
                Platform.runLater(() -> callback.updateRecordLog(log));
                // 如果是组合按键的开始，记录轨迹点
                if (clickBean != null) {
                    // 创建轨迹点记录组合按键
                    List<Integer> currentMouseButtons = new CopyOnWriteArrayList<>(pressMouseButtons);
                    List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                    clickBean.addMovePoint(startX, startY, currentMouseButtons, currentKeyboardKeys, false, 0);
                }
                // 开始拖拽轨迹记录
                if (callback.isRecordDrag()) {
                    addNativeListener(dragMotionListener);
                }
            }
        }
    }

    /**
     * 键盘松开监听器
     *
     * @param e 键盘松开事件
     */
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (isRecording && clickBean != null) {
            // 记录松开的键盘按键
            pressKeyboardKeys.remove(Integer.valueOf(e.getKeyCode()));
            Point mousePoint = MousePositionListener.getMousePoint();
            int endX = (int) mousePoint.getX();
            int endY = (int) mousePoint.getY();
            // 所有按键都松开后停止记录
            if (callback.isRecordDrag() && pressKeyboardKeys.isEmpty() && pressMouseButtons.isEmpty()) {
                removeNativeListener(dragMotionListener);
                // 拖拽结束时添加释放鼠标的坐标
                clickBean.addMovePoint(endX, endY, null, null, true, 0);
            } else {
                // 创建轨迹点记录组合按键
                List<Integer> currentMouseButtons = new CopyOnWriteArrayList<>(pressMouseButtons);
                List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                clickBean.addMovePoint(endX, endY, currentMouseButtons, currentKeyboardKeys, false, 0);
            }
            isFirstClick = false;
            // 记录移动轨迹
            if (callback.isRecordMove()) {
                movePoint = callback.createDefaultClickPosition();
                // 所有按键都松开后开始移动轨迹记录
                if (pressKeyboardKeys.isEmpty() && pressMouseButtons.isEmpty()) {
                    startMoveTime = System.currentTimeMillis();
                    addNativeListener(moveMotionListener);
                }
            }
            // 只有在所有按键都松开时才算一个完整的操作步骤
            if (pressKeyboardKeys.isEmpty() && pressMouseButtons.isEmpty()) {
                releasedTime = System.currentTimeMillis();
                // 计算点击持续时间（毫秒）
                long duration = releasedTime - pressTime;
                // 设置点击持续时间
                clickBean.setClickTime(String.valueOf(duration))
                        .updateRelativePosition();
                addKeyEventToTable(clickBean);
            }
        }
    }

    /**
     * 滑轮滑动监听器
     *
     * @param e 滑轮滑动事件
     */
    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        if (isRecording) {
            // 只记录垂直方向滑动
            if (e.getWheelDirection() == NativeMouseWheelEvent.WHEEL_VERTICAL_DIRECTION) {
                Point mousePoint = MousePositionListener.getMousePoint();
                int x = (int) mousePoint.getX();
                int y = (int) mousePoint.getY();
                // 根据滑轮滚动方向设置操作类型
                int wheelRotation = e.getWheelRotation();
                if (wheelRotation == 0) {
                    callback.stopWorkAll();
                    Platform.runLater(callback::showError);
                } else {
                    String log = text_cancelTask() + text_recordClicking() + "\n" +
                            text_recorded() + (wheelRotation < 0 ? clickType_wheelUp() : clickType_wheelDown());
                    Platform.runLater(() -> callback.updateRecordLog(log));
                    if (clickBean != null) {
                        List<Integer> currentMouseButtons = new CopyOnWriteArrayList<>(pressMouseButtons);
                        List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                        clickBean.addMovePoint(x, y, currentMouseButtons, currentKeyboardKeys, false, wheelRotation);
                    } else if (callback.isRecordMove() && isRecordingMoveTrajectory()) {
                        // 有移动轨迹时在轨迹点中记录滑轮事件
                        List<Integer> currentKeyboardKeys = new CopyOnWriteArrayList<>(pressKeyboardKeys);
                        movePoint.addMovePoint(x, y, null, currentKeyboardKeys, false, wheelRotation);
                        Platform.runLater(() -> callback.onWheelRecorded(wheelRotation, Math.abs(wheelRotation)));
                    } else {
                        // 没有移动轨迹时单独记录滑轮事件
                        pressTime = System.currentTimeMillis();
                        long waitTime = isFirstClick ?
                                pressTime - recordingStartTime :
                                pressTime - releasedTime;
                        int clickType = wheelRotation > 0 ?
                                ClickTypeEnum.WHEEL_DOWN.ordinal() :
                                ClickTypeEnum.WHEEL_UP.ordinal();
                        int wheelNum = Math.abs(wheelRotation);
                        // 创建单独的滑轮步骤
                        ClickPositionVO wheelBean = callback.createDefaultClickPosition();
                        int index = callback.getCurrentStepCount() + 1;
                        wheelBean.setName(text_step() + index + text_isRecord())
                                .setMouseKeyEnum(NativeMouseEvent.NOBUTTON)
                                .setWaitTime(String.valueOf(waitTime))
                                .setClickNum(String.valueOf(wheelNum))
                                .setStartX(String.valueOf(x))
                                .setStartY(String.valueOf(y))
                                .setClickTypeEnum(clickType)
                                .setClickTime("0")
                                .updateRelativePosition();
                        // 添加至表格
                        addWheelEventToTable(wheelBean);
                    }
                    releasedTime = System.currentTimeMillis();
                    startMoveTime = System.currentTimeMillis();
                    isFirstClick = false;
                }
            }
        }
    }

    /**
     * 启动录制
     */
    public void startRecording() {
        recordingStartTime = System.currentTimeMillis();
        isRecording = true;
        movePoint = callback.createDefaultClickPosition();
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        // 停止时添加最后的移动轨迹
        if (callback.isRecordMove() && isRecordingMoveTrajectory() && hasPendingMoveTrajectory) {
            Point mousePoint = MousePositionListener.getMousePoint();
            int startX = (int) mousePoint.getX();
            int startY = (int) mousePoint.getY();
            addMoveTrajectory(startX, startY);
            hasPendingMoveTrajectory = false;
        }
        pressMouseButtons.clear();
        pressKeyboardKeys.clear();
        isFirstClick = true;
        removeNativeListener(this);
        isRecording = false;
    }

    /**
     * 检查是否正在记录移动轨迹
     */
    private boolean isRecordingMoveTrajectory() {
        return movePoint != null && !movePoint.getMoveTrajectory().isEmpty();
    }

    /**
     * 添加移动轨迹到表格
     *
     * @param startX 起始横坐标
     * @param startY 起始纵坐标
     */
    private void addMoveTrajectory(int startX, int startY) {
        if (callback.isRecordMove() && pressMouseButtons.isEmpty() && pressKeyboardKeys.isEmpty() && movePoint != null) {
            long endMoveTime = System.currentTimeMillis();
            long moveTime = isFirstClick ?
                    endMoveTime - recordingStartTime :
                    endMoveTime - startMoveTime;
            int index = callback.getCurrentStepCount() + 1;
            movePoint.setClickTypeEnum(ClickTypeEnum.MOVE_TRAJECTORY.ordinal())
                    .setName(text_step() + index + text_isRecord())
                    .setMouseKeyEnum(NativeMouseEvent.NOBUTTON)
                    .setClickTime(String.valueOf(moveTime))
                    .setStartX(String.valueOf(startX))
                    .setStartY(String.valueOf(startY))
                    .updateRelativePosition();
            // 添加到表格
            addTrajectoryEventToTable(movePoint);
            hasPendingMoveTrajectory = false;
        }
    }

    /**
     * 添加鼠标点击步骤到表格
     *
     * @param event 鼠标点击步骤
     * @param endX  终点横坐标
     * @param endY  终点纵坐标
     */
    private void addMouseEventToTable(ClickPositionVO event, int endX, int endY) {
        Platform.runLater(() -> {
            List<ClickPositionVO> events = new ArrayList<>();
            events.add(event);
            callback.addEventsToTable(events, addType);
            if (addType != noAdd) {
                String log = text_cancelTask() + text_recordClicking() + "\n" +
                        text_recorded() + clickBean.getMouseKey() + text_click() + " X：" + endX + " Y：" + endY;
                callback.updateRecordLog(log);
            }
        });
    }

    /**
     * 添加键盘点击步骤到表格
     *
     * @param event 键盘点击步骤
     */
    private void addKeyEventToTable(ClickPositionVO event) {
        Platform.runLater(() -> {
            List<ClickPositionVO> events = new ArrayList<>();
            events.add(event);
            callback.addEventsToTable(events, addType);
            if (addType != noAdd) {
                String log = text_cancelTask() + text_recordClicking() + "\n" +
                        text_recorded() + clickType_keyboard() + " " + event.getKeyboardKey();
                callback.updateRecordLog(log);
            }
        });
    }

    /**
     * 添加滑轮滚动步骤到表格
     *
     * @param event 滑轮滚动步骤
     */
    private void addWheelEventToTable(ClickPositionVO event) {
        Platform.runLater(() -> {
            List<ClickPositionVO> events = new ArrayList<>();
            events.add(event);
            callback.addEventsToTable(events, addType);
        });
    }

    /**
     * 添加轨迹步骤到表格
     *
     * @param event 轨迹步骤
     */
    private void addTrajectoryEventToTable(ClickPositionVO event) {
        Platform.runLater(() -> {
            List<ClickPositionVO> events = new ArrayList<>();
            events.add(event);
            callback.addEventsToTable(events, addType);
            if (addType != noAdd) {
                String log = text_cancelTask() + text_recordClicking() + "\n" +
                        text_recorded() + autoClick_mouseTrajectory();
                callback.updateRecordLog(log);
            }
        });
    }

}
