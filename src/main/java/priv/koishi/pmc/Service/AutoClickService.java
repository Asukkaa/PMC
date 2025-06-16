package priv.koishi.pmc.Service;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.Point;
import priv.koishi.pmc.Bean.*;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Queue.DynamicQueue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static priv.koishi.pmc.Finals.CommonFinals.activation;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.Service.ImageRecognitionService.*;
import static priv.koishi.pmc.Utils.FileUtils.getExistsFileName;

/**
 * 自动点击线程任务类
 *
 * @author KOISHI
 * Date:2025-02-18
 * Time:14:42
 */
public class AutoClickService {

    /**
     * 执行自动流程前点击第一个起始坐标标识
     */
    private static final AtomicBoolean firstClick = new AtomicBoolean();

    /**
     * 浮窗信息栏
     */
    private static Label floatingLabel;

    /**
     * 程序界面信息栏
     */
    private static Label massageLabel;

    /**
     * 随机数
     */
    private static final Random random = new Random();

    /**
     * 日志动态容量队列
     */
    private static DynamicQueue<ClickLogBean> dynamicQueue;

    /**
     * 自动点击任务线程
     *
     * @param taskBean 线程任务参数
     * @return 执行记录
     */
    public static Task<List<ClickLogBean>> autoClick(AutoClickTaskBean taskBean, Robot robot) {
        return new Task<>() {
            @Override
            protected List<ClickLogBean> call() throws Exception {
                List<ClickLogBean> clickLogBeans = new CopyOnWriteArrayList<>();
                Timeline timeline = taskBean.getRunTimeline();
                if (timeline != null) {
                    timeline.stop();
                }
                dynamicQueue = new DynamicQueue<>();
                int maxLogNum = taskBean.getMaxLogNum();
                if (maxLogNum > 0) {
                    dynamicQueue.setMaxSize(maxLogNum);
                }
                List<ClickPositionVO> tableViewItems = taskBean.getBeanList();
                int loopTime = taskBean.getLoopTime();
                if (loopTime == 0) {
                    int i = 0;
                    while (!isCancelled()) {
                        i++;
                        String loopTimeText = text_cancelTask() + text_execution() + i + " / ∞" + text_executionTime();
                        if (isCancelled()) {
                            return clickLogBeans;
                        }
                        // 执行操作流程
                        clickLogBeans = clicks(tableViewItems, loopTimeText);
                    }
                } else {
                    for (int i = 0; i < loopTime && !isCancelled(); i++) {
                        String loopTimeText = text_cancelTask() + text_execution() + (i + 1) + " / " + loopTime + text_executionTime();
                        if (isCancelled()) {
                            return clickLogBeans;
                        }
                        // 执行操作流程
                        clickLogBeans = clicks(tableViewItems, loopTimeText);
                    }
                }
                return clickLogBeans;
            }

            // 执行操作流程
            private List<ClickLogBean> clicks(List<ClickPositionVO> tableViewItems, String loopTimeText) throws Exception {
                int dataSize = tableViewItems.size();
                floatingLabel = taskBean.getFloatingLabel();
                massageLabel = taskBean.getMassageLabel();
                firstClick.set(taskBean.isFirstClick());
                updateProgress(0, dataSize);
                int currentStep = 0;
                while (currentStep < dataSize) {
                    int progress = currentStep + 1;
                    updateProgress(progress, dataSize);
                    ClickPositionVO clickPositionVO = tableViewItems.get(currentStep);
                    int startX = Integer.parseInt((clickPositionVO.getStartX()));
                    int startY = Integer.parseInt((clickPositionVO.getStartY()));
                    String waitTime = clickPositionVO.getWaitTime();
                    String clickTime = clickPositionVO.getClickTime();
                    String name = clickPositionVO.getName();
                    String clickKey = clickPositionVO.getClickKey();
                    String clickType = clickPositionVO.getClickType();
                    String interval = clickPositionVO.getClickInterval();
                    String clickImgPath = clickPositionVO.getClickImgPath();
                    String matchType = clickPositionVO.getMatchedType();
                    int clickNum = Integer.parseInt(clickPositionVO.getClickNum()) - 1;
                    String clickText;
                    if (clickType_moveTrajectory().equalsIgnoreCase(clickType)
                            || clickType_move().equalsIgnoreCase(clickType)
                            || clickType_moveTo().equalsIgnoreCase(clickType)) {
                        clickText = "";
                    } else {
                        clickText = bundle.getString("taskInfo") + clickKey + clickType;
                    }
                    Platform.runLater(() -> {
                        String text = loopTimeText +
                                text_progress() + progress + "/" + dataSize +
                                text_willBe() + waitTime + text_msWillBe() + name +
                                text_point() + " X：" + startX + " Y：" + startY + clickText +
                                bundle.getString("clickTime") + clickTime + " " + text_ms() +
                                bundle.getString("repeat") + clickNum + bundle.getString("interval") + interval + " " + text_ms();
                        if (StringUtils.isNotBlank(clickImgPath)) {
                            try {
                                text = loopTimeText +
                                        text_progress() + progress + "/" + dataSize +
                                        text_willBe() + waitTime + text_msWillBe() + name +
                                        bundle.getString("picTarget") +
                                        "\n" + getExistsFileName(new File(clickImgPath)) +
                                        bundle.getString("afterMatch") + matchType;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        massageLabel.setText(text);
                        floatingLabel.setText(text);
                    });
                    long wait = Long.parseLong(waitTime);
                    // 处理随机等待时间偏移
                    if (activation.equals(clickPositionVO.getRandomWaitTime())) {
                        int randomTime = Integer.parseInt(clickPositionVO.getRandomTime());
                        wait = (long) Math.max(0, wait + (random.nextDouble() * 2 - 1) * randomTime);
                    }
                    // 执行前等待时间
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            break;
                        }
                    }
                    if (taskBean.isWaitLog()) {
                        ClickLogBean waitLog = new ClickLogBean();
                        waitLog.setClickTime(String.valueOf(wait))
                                .setType(log_wait())
                                .setName(name);
                        dynamicQueue.add(waitLog);
                    }
                    // 执行自动流程
                    ClickResultBean clickResultBean = click(clickPositionVO, robot, loopTimeText, taskBean);
                    int stepIndex = clickResultBean.getStepIndex();
                    // 点击匹配图像直到图像不存在
                    if (stepIndex == -1) {
                        // 重复点击改为立刻执行
                        clickPositionVO.setWaitTime("0");
                        tableViewItems.set(currentStep, clickPositionVO);
                        continue;
                        // 跳转到指定步骤
                    } else if (stepIndex > 0) {
                        currentStep = stepIndex - 1;
                        continue;
                    }
                    currentStep++;
                }
                return dynamicQueue.getSnapshot();
            }
        };
    }

    /**
     * 按照操作设置执行操作
     *
     * @param clickPositionVO 操作设置
     * @param robot           Robot实例
     * @param loopTimeText    信息浮窗日志
     * @param taskBean        线程任务参数
     * @return 执行结果
     */
    private static ClickResultBean click(ClickPositionVO clickPositionVO, Robot robot, String loopTimeText, AutoClickTaskBean taskBean) throws Exception {
        ClickResultBean clickResultBean = new ClickResultBean();
        clickResultBean.setStepIndex(0);
        int retrySecondValue = taskBean.getRetrySecondValue();
        int overTimeValue = taskBean.getOverTimeValue();
        int clickNum = Integer.parseInt(clickPositionVO.getClickNum());
        double startX = Double.parseDouble(clickPositionVO.getStartX());
        double startY = Double.parseDouble(clickPositionVO.getStartY());
        String name = clickPositionVO.getName();
        // 匹配终止操作图像
        List<ImgFileBean> stopImgFileVOS = clickPositionVO.getStopImgFiles();
        if (CollectionUtils.isNotEmpty(stopImgFileVOS)) {
            stopImgFileVOS.stream().parallel().forEach(stopImgFileBean -> {
                String stopPath = stopImgFileBean.getPath();
                AtomicReference<String> fileName = new AtomicReference<>();
                Platform.runLater(() -> {
                    try {
                        fileName.set(getExistsFileName(new File(stopPath)));
                        String text = loopTimeText + bundle.getString("searchingStop") + fileName.get();
                        floatingLabel.setText(text);
                        massageLabel.setText(text);
                    } catch (IOException e) {
                        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                        throw new RuntimeException(e);
                    }
                });
                long start = System.currentTimeMillis();
                FindPositionConfig findPositionConfig = new FindPositionConfig();
                double stopMatchThreshold = Double.parseDouble(clickPositionVO.getStopMatchThreshold());
                findPositionConfig.setMaxRetry(Integer.parseInt(clickPositionVO.getStopRetryTimes()))
                        .setMatchThreshold(stopMatchThreshold)
                        .setRetryWait(retrySecondValue)
                        .setOverTime(overTimeValue)
                        .setTemplatePath(stopPath)
                        .setContinuously(false);
                MatchPointBean matchPointBean;
                try {
                    matchPointBean = findPosition(findPositionConfig);
                } catch (Exception e) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    throw new RuntimeException(e);
                }
                try (Point position = matchPointBean.getPoint()) {
                    long end = System.currentTimeMillis();
                    int x = position.x();
                    int y = position.y();
                    int matchThreshold = matchPointBean.getMatchThreshold();
                    if (taskBean.isStopImgLog()) {
                        ClickLogBean clickLogBean = new ClickLogBean();
                        clickLogBean.setClickTime(String.valueOf(end - start))
                                .setResult(matchThreshold + " %")
                                .setX(String.valueOf(x))
                                .setY(String.valueOf(y))
                                .setType(log_stopImg())
                                .setName(name);
                        dynamicQueue.add(clickLogBean);
                    }
                    if (matchThreshold >= stopMatchThreshold) {
                        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                        throw new Exception(text_index() + clickPositionVO.getIndex() + bundle.getString("taskStop") +
                                bundle.getString("findStopImg") + fileName.get() +
                                bundle.getString("matchThreshold") + matchThreshold + " %" +
                                "\n" + text_point() + " X：" + x + " Y：" + y);
                    }
                } catch (Exception e) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    throw new RuntimeException(e);
                }
            });
        }
        // 匹配要点击的图像
        String clickPath = clickPositionVO.getClickImgPath();
        String clickType = clickPositionVO.getClickType();
        AtomicReference<String> fileName = new AtomicReference<>();
        if (StringUtils.isNotBlank(clickPath)) {
            Platform.runLater(() -> {
                try {
                    fileName.set(getExistsFileName(new File(clickPath)));
                    String text = loopTimeText + bundle.getString("searchingClick") + fileName.get();
                    floatingLabel.setText(text);
                    massageLabel.setText(text);
                } catch (IOException e) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    throw new RuntimeException(e);
                }
            });
            long start = System.currentTimeMillis();
            String retryType = clickPositionVO.getRetryType();
            FindPositionConfig findPositionConfig = new FindPositionConfig();
            double clickMatchThreshold = Double.parseDouble(clickPositionVO.getClickMatchThreshold());
            findPositionConfig.setMaxRetry(Integer.parseInt(clickPositionVO.getClickRetryTimes()))
                    .setContinuously(retryType_continuously().equals(retryType))
                    .setMatchThreshold(clickMatchThreshold)
                    .setRetryWait(retrySecondValue)
                    .setOverTime(overTimeValue)
                    .setTemplatePath(clickPath);
            MatchPointBean matchPointBean = findPosition(findPositionConfig);
            try (Point position = matchPointBean.getPoint()) {
                String matchedType = clickPositionVO.getMatchedType();
                long end = System.currentTimeMillis();
                if (!clickType_moveTo().equals(clickType)) {
                    startX = position.x() + Integer.parseInt(clickPositionVO.getImgX());
                    startY = position.y() + Integer.parseInt(clickPositionVO.getImgY());
                }
                int matchThreshold = matchPointBean.getMatchThreshold();
                if (taskBean.isClickImgLog()) {
                    ClickLogBean clickLogBean = new ClickLogBean();
                    clickLogBean.setClickTime(String.valueOf(end - start))
                            .setResult(matchThreshold + " %")
                            .setX(String.valueOf(position.x()))
                            .setY(String.valueOf(position.y()))
                            .setType(log_clickImg())
                            .setName(name);
                    dynamicQueue.add(clickLogBean);
                }
                if (matchThreshold >= clickMatchThreshold) {
                    // 匹配成功后直接执行下一个操作步骤
                    if (clickMatched_break().equals(matchedType)) {
                        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                        return clickResultBean;
                        // 匹配成功后执行指定步骤
                    } else if (clickMatched_step().equals(matchedType)) {
                        clickResultBean.setStepIndex(Integer.parseInt(clickPositionVO.getMatchedStep()))
                                .setClickLogs(dynamicQueue.getSnapshot());
                        return clickResultBean;
                        // 匹配成功后点击匹配图像并执行指定步骤
                    } else if (clickMatched_clickStep().equals(matchedType)) {
                        clickResultBean.setStepIndex(Integer.parseInt(clickPositionVO.getMatchedStep()));
                        // 匹配图像存在则重复点击
                    } else if (clickMatched_clickWhile().equals(matchedType)) {
                        clickResultBean.setStepIndex(-1);
                    }
                    // 匹配失败后或图像识别匹配逻辑为 匹配图像存在则重复点击 跳过本次操作
                } else if (retryType_break().equals(retryType) || clickMatched_clickWhile().equals(matchedType)) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    return clickResultBean;
                    // 匹配失败后终止操作
                } else if (retryType_stop().equals(retryType)) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    try {
                        throw new Exception(text_index() + clickPositionVO.getIndex() + bundle.getString("taskErr") +
                                bundle.getString("maxRetry") + clickPositionVO.getClickRetryTimes() + " " + bundle.getString("unit.times") +
                                bundle.getString("notFound") + fileName.get() +
                                bundle.getString("closestMatchThreshold") + matchPointBean.getMatchThreshold() + " %" +
                                "\n" + text_point() + " X：" + position.x() + " Y：" + position.y());
                    } catch (Exception e) {
                        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                        throw new RuntimeException(e);
                    }
                    // 匹配失败后执行指定步骤
                } else if (retryType_Step().equals(retryType)) {
                    clickResultBean.setStepIndex(Integer.parseInt(clickPositionVO.getRetryStep()))
                            .setClickLogs(dynamicQueue.getSnapshot());
                    return clickResultBean;
                }
            }
        }
        long clickTime = Long.parseLong(clickPositionVO.getClickTime());
        long clickInterval = Long.parseLong(clickPositionVO.getClickInterval());
        int randomTime = Integer.parseInt(clickPositionVO.getRandomTime());
        // 按照操作次数执行
        for (int i = 0; i < clickNum; i++) {
            // 每次操作的间隔时间
            if (i > 0) {
                // 处理随机间隔时间偏移
                if (activation.equals(clickPositionVO.getRandomClickInterval())) {
                    clickInterval = (long) Math.max(0, clickTime + (random.nextDouble() * 2 - 1) * randomTime);
                }
                try {
                    Thread.sleep(clickInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                if (taskBean.isWaitLog()) {
                    ClickLogBean clickLogBean = new ClickLogBean();
                    clickLogBean.setClickTime(String.valueOf(clickInterval))
                            .setType(log_wait())
                            .setName(name);
                    dynamicQueue.add(clickLogBean);
                }
            }
            MouseButton mouseButton = runClickTypeMap.get(clickPositionVO.getClickKey());
            String clickKey = clickPositionVO.getClickKey();
            // 处理随机坐标偏移量
            if (activation.equals(clickPositionVO.getRandomClick())) {
                int randomX = Integer.parseInt(clickPositionVO.getRandomX());
                int randomY = Integer.parseInt(clickPositionVO.getRandomY());
                startX = Math.min(Math.max(0, startX + (random.nextDouble() * 2 - 1) * randomX), screenWidth);
                startY = Math.min(Math.max(0, startY + (random.nextDouble() * 2 - 1) * randomY), screenHeight);
            }
            double finalStartX = startX;
            double finalStartY = startY;
            CompletableFuture<Void> actionFuture = new CompletableFuture<>();
            Platform.runLater(() -> {
                robot.mouseMove(finalStartX, finalStartY);
                if (taskBean.isMoveLog()) {
                    ClickLogBean moveLog = new ClickLogBean();
                    moveLog.setX(String.valueOf((int) finalStartX))
                            .setY(String.valueOf((int) finalStartY))
                            .setType(log_move())
                            .setName(name);
                    dynamicQueue.add(moveLog);
                }
                // 执行自动流程前点击第一个起始坐标
                if (firstClick.compareAndSet(true, false)) {
                    robot.mousePress(mouseButton);
                    if (taskBean.isClickLog()) {
                        ClickLogBean pressLog = new ClickLogBean();
                        pressLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(clickKey)
                                .setType(log_press())
                                .setName(name);
                        dynamicQueue.add(pressLog);
                    }
                    robot.mouseRelease(mouseButton);
                    if (taskBean.isClickLog()) {
                        ClickLogBean releaseLog = new ClickLogBean();
                        releaseLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setType(log_release())
                                .setClickKey(clickKey)
                                .setName(name);
                        dynamicQueue.add(releaseLog);
                    }
                }
                if (clickType_click().equals(clickType)) {
                    robot.mousePress(mouseButton);
                    if (taskBean.isClickLog()) {
                        ClickLogBean pressLog = new ClickLogBean();
                        pressLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(clickKey)
                                .setType(log_press())
                                .setName(name);
                        dynamicQueue.add(pressLog);
                    }
                }
                actionFuture.complete(null);
            });
            // 等待任务完成
            try {
                actionFuture.get();
            } catch (Exception e) {
                clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                Thread.currentThread().interrupt();
                break;
            }
            // 执行长按操作
            if (!clickType_drag().equals(clickType) && !clickType_moveTrajectory().equals(clickType)) {
                // 处理随机点击时长偏移
                if (activation.equals(clickPositionVO.getRandomClickTime())) {
                    clickTime = (long) Math.max(0, clickTime + (random.nextDouble() * 2 - 1) * randomTime);
                }
                // 单次操作时间
                try {
                    Thread.sleep(clickTime);
                } catch (InterruptedException e) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    Thread.currentThread().interrupt();
                    break;
                }
                if (taskBean.isClickLog()) {
                    ClickLogBean clickLog = new ClickLogBean();
                    clickLog.setClickTime(String.valueOf(clickTime))
                            .setX(String.valueOf((int) finalStartX))
                            .setY(String.valueOf((int) finalStartY))
                            .setClickKey(clickKey)
                            .setType(log_hold())
                            .setName(name);
                    dynamicQueue.add(clickLog);
                }
                CompletableFuture<Void> releaseFuture = new CompletableFuture<>();
                Platform.runLater(() -> {
                    if (clickType_click().equals(clickType)) {
                        robot.mouseRelease(mouseButton);
                        if (taskBean.isClickLog()) {
                            ClickLogBean releaseLog = new ClickLogBean();
                            releaseLog.setX(String.valueOf((int) finalStartX))
                                    .setY(String.valueOf((int) finalStartY))
                                    .setType(log_release())
                                    .setClickKey(clickKey)
                                    .setName(name);
                            dynamicQueue.add(releaseLog);
                        }
                    }
                    releaseFuture.complete(null);
                });
                // 等待任务完成
                try {
                    releaseFuture.get();
                } catch (Exception e) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // 计算鼠标轨迹
                executeTrajectoryPoints(robot, clickPositionVO, taskBean);
                clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
            }
        }
        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
        return clickResultBean;
    }

    /**
     * 精确执行轨迹点移动序列
     *
     * @param robot           机器人操作实例
     * @param clickPositionVO 点击位置信息
     * @param taskBean        线程任务参数
     * @throws Exception 当移动超时或线程中断时抛出
     */
    private static void executeTrajectoryPoints(Robot robot, ClickPositionVO clickPositionVO, AutoClickTaskBean taskBean) throws Exception {
        String name = clickPositionVO.getName();
        List<TrajectoryPointBean> points = clickPositionVO.getMoveTrajectory();
        if (!points.isEmpty()) {
            TrajectoryPointBean lastPoint = null;
            for (TrajectoryPointBean point : points) {
                // 当前轨迹点按下的按键
                List<Integer> pressButtons = point.getPressButtons();
                // 当前轨迹点要抬起的按键
                List<Integer> releaseButtons = new CopyOnWriteArrayList<>();
                // 当前轨迹点新增的要按下的按键
                List<Integer> nowPressButtons;
                long remaining = 0;
                if (lastPoint != null) {
                    remaining = point.getTimestamp() - lastPoint.getTimestamp();
                    // 上一个轨迹点按下的按键
                    List<Integer> lastPressButtons = lastPoint.getPressButtons();
                    if (CollectionUtils.isEmpty(pressButtons)) {
                        nowPressButtons = null;
                        releaseButtons = lastPressButtons;
                    } else {
                        if (CollectionUtils.isNotEmpty(lastPressButtons)) {
                            releaseButtons = (List<Integer>) CollectionUtils.subtract(lastPressButtons, pressButtons);
                            nowPressButtons = (List<Integer>) CollectionUtils.subtract(pressButtons, lastPressButtons);
                        } else {
                            nowPressButtons = pressButtons;
                        }
                    }
                } else {
                    nowPressButtons = pressButtons;
                }
                lastPoint = point;
                double x = point.getX();
                double y = point.getY();
                if (activation.equals(clickPositionVO.getRandomTrajectory())) {
                    int randomX = Integer.parseInt(clickPositionVO.getRandomX());
                    int randomY = Integer.parseInt(clickPositionVO.getRandomY());
                    x = Math.min(Math.max(0, x + (random.nextDouble() * 2 - 1) * randomX), screenWidth);
                    y = Math.min(Math.max(0, y + (random.nextDouble() * 2 - 1) * randomY), screenHeight);
                }
                CompletableFuture<Void> moveFuture = new CompletableFuture<>();
                List<Integer> finalReleaseButtons = releaseButtons;
                double finalX = x;
                double finalY = y;
                Platform.runLater(() -> {
                    if (CollectionUtils.isNotEmpty(nowPressButtons)) {
                        nowPressButtons.forEach(button -> {
                            robot.mousePress(NativeMouseToMouseButton.get(button));
                            if (taskBean.isDragLog()) {
                                ClickLogBean clickLog = new ClickLogBean();
                                clickLog.setClickKey(recordClickTypeMap.get(button))
                                        .setX(String.valueOf((int) finalX))
                                        .setY(String.valueOf((int) finalY))
                                        .setType(log_press())
                                        .setName(name);
                                dynamicQueue.add(clickLog);
                            }
                        });
                    }
                    if (CollectionUtils.isNotEmpty(finalReleaseButtons)) {
                        finalReleaseButtons.forEach(button -> {
                            robot.mouseRelease(NativeMouseToMouseButton.get(button));
                            if (taskBean.isDragLog()) {
                                ClickLogBean releaseLog = new ClickLogBean();
                                releaseLog.setClickKey(recordClickTypeMap.get(button))
                                        .setX(String.valueOf((int) finalX))
                                        .setY(String.valueOf((int) finalY))
                                        .setType(log_release())
                                        .setName(name);
                                dynamicQueue.add(releaseLog);
                            }
                        });
                    }
                    robot.mouseMove(finalX, finalY);
                    if (CollectionUtils.isEmpty(pressButtons) && taskBean.isMoveLog()) {
                        ClickLogBean moveLog = new ClickLogBean();
                        moveLog.setX(String.valueOf((int) finalX))
                                .setY(String.valueOf((int) finalY))
                                .setType(log_move())
                                .setName(name);
                        dynamicQueue.add(moveLog);
                    } else if (CollectionUtils.isNotEmpty(pressButtons) && taskBean.isDragLog()) {
                        List<String> clickKeys = new ArrayList<>();
                        pressButtons.forEach(button -> {
                            String clickKey = recordClickTypeMap.get(button);
                            clickKeys.add(clickKey);
                        });
                        ClickLogBean moveLog = new ClickLogBean();
                        moveLog.setClickKey(String.join(",", clickKeys))
                                .setX(String.valueOf((int) finalX))
                                .setY(String.valueOf((int) finalY))
                                .setType(log_drag())
                                .setName(name);
                        dynamicQueue.add(moveLog);
                    }
                    moveFuture.complete(null);
                });
                // 精确控制时间间隔
                moveFuture.get(remaining + 500, TimeUnit.MILLISECONDS);
                if (remaining > 0) {
                    Thread.sleep(remaining);
                    if (taskBean.isWaitLog()) {
                        ClickLogBean sleepLog = new ClickLogBean();
                        sleepLog.setClickTime(String.valueOf(remaining))
                                .setType(log_wait())
                                .setName(name);
                        dynamicQueue.add(sleepLog);
                    }
                }
            }
        }
    }

    /**
     * 解除组件引用
     */
    public static void clearReferences() {
        floatingLabel = null;
        massageLabel = null;
        dynamicQueue = null;
    }

    /**
     * 获取当前产生的日志（任务终止时使用）
     *
     * @return 当前产生的日志
     **/
    public static List<ClickLogBean> getNowLogs() {
        return dynamicQueue.getSnapshot();
    }

}
