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
import priv.koishi.pmc.Bean.Config.FindPositionConfig;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Finals.Enum.ClickTypeEnum;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.Finals.Enum.MatchedTypeEnum;
import priv.koishi.pmc.Finals.Enum.RetryTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor;
import priv.koishi.pmc.Queue.DynamicQueue;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.getMainWindowInfo;
import static priv.koishi.pmc.MainApplication.mainStage;
import static priv.koishi.pmc.Service.ImageRecognitionService.*;
import static priv.koishi.pmc.Service.PMCFileService.loadPMCFile;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.updateMessageLabel;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.*;
import static priv.koishi.pmc.Utils.CommonUtils.copyAllProperties;
import static priv.koishi.pmc.Utils.CommonUtils.isValidUrl;
import static priv.koishi.pmc.Utils.FileUtils.getExistsFileName;
import static priv.koishi.pmc.Utils.FileUtils.openFile;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.ScriptUtils.runScript;
import static priv.koishi.pmc.Utils.UiUtils.showStageAlert;

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
    private static FloatingWindowDescriptor messageFloating;

    /**
     * 程序界面信息栏
     */
    private static Label messageLabel;

    /**
     * 随机数
     */
    private static final Random random = new Random();

    /**
     * 日志动态容量队列
     */
    private static DynamicQueue<ClickLogBean> clickLog;

    /**
     * JavaFX 机器人输入标志，true 为机器人输入，将不会触发快捷键取消自动任务
     */
    public static volatile boolean isRobotInput;

    /**
     * 批量运行 PMC 文件
     *
     * @param robot        Robot 实例
     * @param pmcListBeans 需要批量运行的 PMC 文件列表
     * @param baseTaskBean 任务参数
     */
    public static Task<List<ClickLogBean>> autoClicks(Robot robot, List<? extends PMCListBean> pmcListBeans,
                                                      AutoClickTaskBean baseTaskBean) {
        return new Task<>() {
            @Override
            protected List<ClickLogBean> call() throws Exception {
                int totalPMCs = pmcListBeans.size();
                updateProgress(0, totalPMCs);
                for (int i = 0; i < totalPMCs; i++) {
                    PMCListBean pmcListBean = pmcListBeans.get(i);
                    String path = pmcListBean.getPath();
                    String text = text_checkPMCFile() +
                            "\n" + text_fileName() + pmcListBean.getName() +
                            "\n" + text_filePath() + path;
                    updateMessage(text);
                    updateProgress(i + 1, totalPMCs);
                    List<ClickPositionVO> clickPositionVOS = loadPMCFile(new File(path));
                    pmcListBean.setClickPositionVOS(clickPositionVOS);
                }
                clickLog = new DynamicQueue<>();
                int maxLogNum = baseTaskBean.getMaxLogNum();
                if (maxLogNum > 0) {
                    clickLog.setMaxSize(maxLogNum);
                }
                // 获取整体循环次数
                int overallLoopTimes = baseTaskBean.getLoopTimes();
                // 使用单线程顺序执行
                try (ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                })) {
                    // 无限循环或有限循环
                    if (overallLoopTimes == 0) {
                        // 无限循环
                        int loopCount = 0;
                        while (!isCancelled()) {
                            loopCount++;
                            String loopTimeText = text_cancelTask() + text_execution() + loopCount + " / ∞" + text_executionTime();
                            updateMessage(loopTimeText);
                            // 执行当前批次
                            if (executeBatch(pmcListBeans, executor, baseTaskBean, 0, loopTimeText)) {
                                break;
                            }
                        }
                    } else {
                        // 有限循环
                        for (int loop = 0; loop < overallLoopTimes && !isCancelled(); loop++) {
                            String loopTimeText = text_cancelTask() + text_execution() + (loop + 1) + " / " +
                                    overallLoopTimes + text_executionTime();
                            updateMessage(loopTimeText);
                            // 执行当前批次
                            if (executeBatch(pmcListBeans, executor, baseTaskBean, loop, loopTimeText)) {
                                break;
                            }
                        }
                    }
                }
                // 返回所有日志
                return clickLog.getSnapshot();
            }

            /**
             * 执行单个批次的 PMC 文件
             *
             * @param pmcListBeans 需要批量运行的 PMC 文件列表
             * @param executor     线程执行器
             * @param baseTaskBean 任务参数
             * @param currentLoop  当前批次索引
             * @param loopText     当前循环信息
             */
            private boolean executeBatch(List<? extends PMCListBean> pmcListBeans, ExecutorService executor,
                                         AutoClickTaskBean baseTaskBean, int currentLoop, String loopText) throws Exception {
                int totalPMCs = pmcListBeans.size();
                updateProgress(0, totalPMCs);
                for (int i = 0; i < totalPMCs; i++) {
                    if (isCancelled()) {
                        return true;
                    }
                    PMCListBean pmc = pmcListBeans.get(i);
                    String text = loopText +
                            text_progress() + i + 1 + " / " + totalPMCs +
                            text_willBe() + pmc.getWaitTime() + text_msWillBe() + pmc.getName();
                    updateMessage(text);
                    // 执行前等待
                    String waitTime = pmc.getWaitTime();
                    if (StringUtils.isNotBlank(waitTime)) {
                        long waitMillis = Long.parseLong(waitTime);
                        Thread.sleep(waitMillis);
                    }
                    try {
                        // 准备并执行单个任务
                        AutoClickTaskBean subTaskBean = new AutoClickTaskBean();
                        copyAllProperties(baseTaskBean, subTaskBean);
                        // 设置循环次数（覆盖 PMC 文件自身的循环次数，因为现在是整体循环）
                        String runNum = pmc.getRunNum();
                        if (StringUtils.isNotBlank(runNum)) {
                            try {
                                subTaskBean.setLoopTimes(Integer.parseInt(runNum));
                            } catch (NumberFormatException e) {
                                subTaskBean.setLoopTimes(1);
                            }
                        } else {
                            subTaskBean.setLoopTimes(1);
                        }
                        subTaskBean.setBeanList(pmc.getClickPositionVOS());
                        // 只有在整体第一次循环的第一个任务才需要点击起始坐标
                        if (baseTaskBean.isFirstClick()) {
                            subTaskBean.setFirstClick(currentLoop == 0 && i == 0);
                        }
                        // 创建子任务
                        Task<List<ClickLogBean>> subTask = autoClick(subTaskBean, robot, clickLog);
                        // 使用 CountDownLatch 等待子任务完成
                        CountDownLatch latch = new CountDownLatch(1);
                        AtomicBoolean taskSuccess = new AtomicBoolean(false);
                        AtomicReference<Exception> taskException = new AtomicReference<>();
                        subTask.setOnSucceeded(_ -> {
                            try {
                                taskSuccess.set(true);
                            } finally {
                                latch.countDown();
                            }
                        });
                        subTask.setOnFailed(_ -> {
                            taskException.set((Exception) subTask.getException());
                            latch.countDown();
                        });
                        subTask.setOnCancelled(_ -> latch.countDown());
                        // 在单独的线程中执行子任务
                        executor.execute(() -> {
                            Thread subThread = new Thread(subTask);
                            subThread.setDaemon(true);
                            subThread.start();
                        });
                        // 等待子任务完成
                        latch.await();
                        // 检查子任务是否成功
                        if (taskException.get() != null) {
                            throw taskException.get();
                        }
                        if (!taskSuccess.get()) {
                            throw new RuntimeException(text_subTaskNoFinished());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(text_subTaskFailed() + pmc.getPath(), e);
                    }
                    updateProgress(i + 1, totalPMCs);
                }
                return false;
            }
        };
    }

    /**
     * 自动点击任务线程
     *
     * @param taskBean 线程任务参数
     * @param robot    Robot 实例
     * @return 执行记录
     */
    public static Task<List<ClickLogBean>> autoClick(AutoClickTaskBean taskBean, Robot robot,
                                                     DynamicQueue<ClickLogBean> clickLogQueue) {
        return new Task<>() {
            @Override
            protected List<ClickLogBean> call() throws Exception {
                List<ClickPositionVO> tableViewItems = taskBean.getBeanList();
                List<String> errs;
                // 检查跳转逻辑参数与操作类型设置是否合理
                errs = checkJumpSetting(tableViewItems);
                if (showErrAlert(errs, text_jumpSettingErr())) {
                    return null;
                }
                // 校验并更新识别范围设置
                errs = updateWindowInfos(tableViewItems);
                if (showErrAlert(errs, text_windowInfoErr())) {
                    return null;
                }
                List<ClickLogBean> clickLogBeans = new CopyOnWriteArrayList<>();
                Timeline timeline = taskBean.getRunTimeline();
                if (timeline != null) {
                    timeline.stop();
                }
                clickLog = clickLogQueue;
                int maxLogNum = taskBean.getMaxLogNum();
                if (maxLogNum > 0) {
                    clickLog.setMaxSize(maxLogNum);
                }
                int loopTime = taskBean.getLoopTimes();
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

            /**
             * 检查跳转逻辑参数与操作类型设置是否合理
             *
             * @param clickPositionVOS 操作步骤
             * @throws RuntimeException 参数设置相关错误
             */
            private List<String> checkJumpSetting(List<? extends ClickPositionVO> clickPositionVOS) {
                List<String> errs = new ArrayList<>();
                int maxIndex = clickPositionVOS.size();
                updateProgress(0, maxIndex);
                updateFloatingMessage(text_checkJumpSetting());
                for (int i = 0; i < maxIndex; i++) {
                    updateProgress(i + 1, maxIndex);
                    ClickPositionVO clickPositionVO = clickPositionVOS.get(i);
                    int index = clickPositionVO.getIndex();
                    String err = autoClick_index() + index + autoClick_name() + clickPositionVO.getName() + autoClick_settingErr();
                    boolean isErr = false;
                    int matchedType = clickPositionVO.getMatchedTypeEnum();
                    if (MatchedTypeEnum.STEP.ordinal() == matchedType ||
                            MatchedTypeEnum.CLICK_STEP.ordinal() == matchedType) {
                        int matchStep = Integer.parseInt(clickPositionVO.getMatchedStep());
                        if (matchStep > maxIndex) {
                            err += text_matchedStepGreaterMax();
                            isErr = true;
                        }
                    }
                    if (RetryTypeEnum.STEP.ordinal() == clickPositionVO.getRetryTypeEnum()) {
                        int retryStep = Integer.parseInt(clickPositionVO.getRetryStep());
                        if (retryStep > maxIndex) {
                            err += text_retryStepGreaterMax();
                            isErr = true;
                        } else if (retryStep == index) {
                            err += text_retryStepEqualIndex();
                            isErr = true;
                        }
                    }
                    if (isErr) {
                        errs.add(err);
                    }
                }
                return errs;
            }

            /**
             * 更新窗口信息
             *
             * @param tableViewItems 自动操作设置列表
             * @return 错误信息
             */
            private List<String> updateWindowInfos(List<? extends ClickPositionVO> tableViewItems) {
                Set<String> processPaths = new HashSet<>();
                int tableSize = tableViewItems.size();
                updateFloatingMessage(text_checkingWindowInfo());
                List<String> errs = new ArrayList<>();
                updateProgress(0, tableSize);
                // 错误的窗口设置集合
                List<Integer> clickErrIndex = new ArrayList<>();
                List<Integer> stopErrIndex = new ArrayList<>();
                // 校验窗口信息设置是否有误
                checkWindowInfoSet(tableViewItems, errs, clickErrIndex, processPaths, stopErrIndex);
                // 更新窗口信息
                Map<String, WindowInfo> windowInfoMap = new HashMap<>();
                int pathSize = processPaths.size();
                updateFloatingMessage(text_gettingWindowInfo());
                updateProgress(0, pathSize);
                updateWindowInfo(pathSize, processPaths, windowInfoMap);
                // 校验窗口是否存在
                if (!windowInfoMap.isEmpty()) {
                    updateFloatingMessage(text_updatingWindowInfo());
                    updateProgress(0, tableSize);
                    checkWindowExists(tableViewItems, clickErrIndex, windowInfoMap, errs, stopErrIndex);
                }
                return errs;
            }

            /**
             * 校验窗口是否存在
             *
             * @param tableViewItems 自动操作设置列表
             * @param clickErrIndex  目标窗口错误设置步骤索引
             * @param windowInfoMap  窗口信息集合
             * @param errs           错误信息
             * @param stopErrIndex   终止操作窗口错误设置步骤索引
             */
            private void checkWindowExists(List<? extends ClickPositionVO> tableViewItems,
                                           List<Integer> clickErrIndex, Map<String, ? extends WindowInfo> windowInfoMap,
                                           List<? super String> errs, List<Integer> stopErrIndex) {
                int tableSize = tableViewItems.size();
                for (int i = 0; i < tableSize; i++) {
                    updateProgress(i + 1, tableSize);
                    ClickPositionVO clickPositionVO = tableViewItems.get(i);
                    int index = clickPositionVO.getIndex();
                    String err = text_checkIndex() + index + text_taskErr();
                    // 校验目标窗口是否存在
                    if (clickErrIndex.contains(index)) {
                        continue;
                    }
                    FloatingWindowConfig clickWindowConfig = clickPositionVO.getClickWindowConfig();
                    if (clickWindowConfig != null && clickWindowConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                        WindowInfo clickInfo = clickWindowConfig.getWindowInfo();
                        WindowInfo windowInfo = windowInfoMap.get(clickInfo.getProcessPath());
                        if (windowInfo == null || StringUtils.isBlank(windowInfo.getProcessPath())) {
                            errs.add(err + text_noClickWindowInfo());
                        } else {
                            // 保留原有的相对坐标和相对大小属性
                            windowInfo.setRelativeHeight(clickInfo.getRelativeHeight())
                                    .setRelativeWidth(clickInfo.getRelativeWidth())
                                    .setRelativeY(clickInfo.getRelativeY())
                                    .setRelativeX(clickInfo.getRelativeX());
                            clickWindowConfig.setWindowInfo(windowInfo);
                            clickPositionVO.setClickWindowConfig(clickWindowConfig);
                        }
                    }
                    // 校验终止操作窗口是否存在
                    if (CollectionUtils.isNotEmpty(clickPositionVO.getStopImgFiles())) {
                        if (stopErrIndex.contains(index)) {
                            continue;
                        }
                        FloatingWindowConfig stopWindowConfig = clickPositionVO.getStopWindowConfig();
                        if (stopWindowConfig != null && stopWindowConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                            WindowInfo stopInfo = stopWindowConfig.getWindowInfo();
                            WindowInfo windowInfo = windowInfoMap.get(stopInfo.getProcessPath());
                            if (windowInfo == null || StringUtils.isBlank(windowInfo.getProcessPath())) {
                                errs.add(err + text_noStopWindowInfo());
                            } else {
                                // 保留原有的相对坐标和相对大小属性
                                windowInfo.setRelativeHeight(stopInfo.getRelativeHeight())
                                        .setRelativeWidth(stopInfo.getRelativeWidth())
                                        .setRelativeY(stopInfo.getRelativeY())
                                        .setRelativeX(stopInfo.getRelativeX());
                                stopWindowConfig.setWindowInfo(windowInfo);
                                clickPositionVO.setStopWindowConfig(stopWindowConfig);
                            }
                        }
                    }
                }
            }

            /**
             * 校验窗口设置是否有误
             *
             * @param pathSize       窗口进程路径数量
             * @param processPaths   窗口进程路径
             * @param windowInfoMap 窗口信息集合
             */
            private void updateWindowInfo(int pathSize, Set<String> processPaths, Map<? super String, ? super WindowInfo> windowInfoMap) {
                for (int i = 0; i < pathSize; i++) {
                    updateProgress(i + 1, pathSize);
                    String processPath = processPaths.toArray(new String[0])[i];
                    try {
                        WindowInfo windowInfo = getMainWindowInfo(processPath);
                        if (windowInfo != null) {
                            windowInfoMap.put(processPath, windowInfo);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            /**
             * 校验窗口设置是否有误
             *
             * @param tableViewItems 自动操作设置列表
             * @param errs           错误信息
             * @param clickErrIndex  目标窗口错误设置步骤索引
             * @param processPaths   窗口进程路径
             * @param stopErrIndex   终止操作窗口错误设置步骤索引
             */
            private void checkWindowInfoSet(List<? extends ClickPositionVO> tableViewItems,
                                            List<? super String> errs, List<? super Integer> clickErrIndex,
                                            Set<? super String> processPaths, List<? super Integer> stopErrIndex) {
                int tableSize = tableViewItems.size();
                for (int i = 0; i < tableSize; i++) {
                    updateProgress(i + 1, tableSize);
                    ClickPositionVO clickPositionVO = tableViewItems.get(i);
                    int clickType = clickPositionVO.getClickTypeEnum();
                    if (clickType <= ClickTypeEnum.OPEN_URL.ordinal()) {
                        continue;
                    }
                    int index = clickPositionVO.getIndex();
                    String err = text_checkIndex() + index + text_taskErr();
                    // 校验目标窗口设置是否有误
                    FloatingWindowConfig clickWindowConfig = clickPositionVO.getClickWindowConfig();
                    if (clickWindowConfig != null && clickWindowConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                        WindowInfo clickInfo = clickWindowConfig.getWindowInfo();
                        if (clickInfo == null || StringUtils.isBlank(clickInfo.getProcessPath())) {
                            errs.add(err + text_noClickWindowInfo());
                            clickErrIndex.add(index);
                        } else {
                            processPaths.add(clickInfo.getProcessPath());
                        }
                    }
                    // 校验终止操作窗口设置是否有误
                    if (CollectionUtils.isNotEmpty(clickPositionVO.getStopImgFiles())) {
                        FloatingWindowConfig stopWindowConfig = clickPositionVO.getStopWindowConfig();
                        if (stopWindowConfig != null && stopWindowConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                            WindowInfo stopInfo = stopWindowConfig.getWindowInfo();
                            if (stopInfo == null || StringUtils.isBlank(stopInfo.getProcessPath())) {
                                errs.add(err + text_noStopWindowInfo());
                                stopErrIndex.add(index);
                            } else {
                                processPaths.add(stopInfo.getProcessPath());
                            }
                        }
                    }
                }
            }

            /**
             * 执行操作流程
             *
             * @param tableViewItems 自动操作设置列表
             * @param loopTimeText 循环信息文案
             */
            private List<ClickLogBean> clicks(List<? extends ClickPositionVO> tableViewItems, String loopTimeText) throws Exception {
                // 复制要执行的操作步骤
                List<ClickPositionVO> backup = creatBackUp(tableViewItems);
                int dataSize = backup.size();
                messageFloating = taskBean.getMessageFloating();
                messageLabel = taskBean.getMessageLabel();
                firstClick.set(taskBean.isFirstClick());
                updateProgress(0, dataSize);
                int currentStep = 0;
                while (currentStep < dataSize) {
                    int progress = currentStep + 1;
                    updateProgress(progress, dataSize);
                    ClickPositionVO clickPositionVO = backup.get(currentStep);
                    int clickType = clickPositionVO.getClickTypeEnum();
                    String waitTime = clickPositionVO.getWaitTime();
                    String name = clickPositionVO.getName();
                    String text = loopTimeText +
                            text_progress() + progress + " / " + dataSize +
                            text_willBe() + waitTime + text_msWillBe() + name;
                    if (clickType <= ClickTypeEnum.OPEN_URL.ordinal()) {
                        if (openLinkPath(text, clickPositionVO)) {
                            break;
                        }
                        currentStep++;
                        continue;
                    }
                    int startX = Integer.parseInt((clickPositionVO.getStartX()));
                    int startY = Integer.parseInt((clickPositionVO.getStartY()));
                    // 处理相对路径
                    String useRelative = clickPositionVO.getUseRelative();
                    FloatingWindowConfig clickWindowConfig = clickPositionVO.getClickWindowConfig();
                    if (clickWindowConfig != null &&
                            FindImgTypeEnum.WINDOW.ordinal() == clickWindowConfig.getFindImgTypeEnum()) {
                        WindowInfo windowInfo = clickWindowConfig.getWindowInfo();
                        if (windowInfo != null && activation.equals(useRelative)) {
                            String relativeX = clickPositionVO.getRelativeX();
                            String relativeY = clickPositionVO.getRelativeY();
                            if (StringUtils.isNotBlank(relativeX) && StringUtils.isNotBlank(relativeY)) {
                                double rX = Double.parseDouble(relativeX);
                                double rY = Double.parseDouble(relativeY);
                                Map<String, Integer> absolutePosition = WindowMonitor.calculateAbsolutePosition(windowInfo, rX, rY);
                                startX = absolutePosition.get(AbsoluteX);
                                startY = absolutePosition.get(AbsoluteY);
                                clickPositionVO.setStartX(String.valueOf(startX));
                                clickPositionVO.setStartY(String.valueOf(startY));
                            }
                        }
                    }
                    String clickTime = clickPositionVO.getClickTime();
                    String interval = clickPositionVO.getClickInterval();
                    String clickImgPath = clickPositionVO.getClickImgPath();
                    int clickNum = Integer.parseInt(clickPositionVO.getClickNum()) - 1;
                    String clickText = text_taskInfo() + clickPositionVO.getClickType() + " ";
                    if (clickType >= ClickTypeEnum.CLICK.ordinal()) {
                        clickText += clickPositionVO.getClickKey();
                    }
                    if (StringUtils.isNotBlank(clickImgPath)) {
                        text += text_picTarget() + "\n" + getExistsFileName(new File(clickImgPath)) +
                                text_afterMatch() + clickPositionVO.getMatchedType();
                    } else {
                        text += text_point() + " X：" + startX + " Y：" + startY + clickText +
                                text_clickTime() + clickTime + " " + unit_ms() +
                                text_repeat() + clickNum + text_interval() + interval + " " + unit_ms();
                    }
                    // 更新操作信息
                    updateFloatingMessage(text);
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
                                .setClickKey(mouseButton_none())
                                .setType(log_wait())
                                .setName(name);
                        clickLog.add(waitLog);
                    }
                    // 执行自动流程
                    ClickResultBean clickResultBean = click(clickPositionVO, robot, loopTimeText, taskBean);
                    int stepIndex = clickResultBean.getStepIndex();
                    // 点击匹配图像直到图像不存在
                    if (stepIndex == -1) {
                        // 设置重复点击时运行参数
                        clickPositionVO.setRetryTypeEnum(RetryTypeEnum.BREAK.ordinal())
                                .setWaitTime(clickPositionVO.getClickInterval())
                                .setClickNum("1");
                        backup.set(currentStep, clickPositionVO);
                        continue;
                        // 跳转到指定步骤
                    } else if (stepIndex > 0) {
                        currentStep = stepIndex - 1;
                        continue;
                    }
                    currentStep++;
                }
                return clickLog.getSnapshot();
            }

            /**
             * 处理要链接相关操作
             *
             * @param text 提示信息
             * @param clickPositionVO 自动操作设置信息
             *
             * @throws Exception 链接操作异常
             */
            private boolean openLinkPath(String text, ClickPositionVO clickPositionVO) throws Exception {
                String waitTime = clickPositionVO.getWaitTime();
                String name = clickPositionVO.getName();
                text += text_taskInfo() + clickPositionVO.getClickType();
                updateFloatingMessage(text);
                long wait = Long.parseLong(waitTime);
                if (activation.equals(clickPositionVO.getRandomWaitTime())) {
                    int randomTime = Integer.parseInt(clickPositionVO.getRandomTime());
                    wait = (long) Math.max(0, wait + (random.nextDouble() * 2 - 1) * randomTime);
                }
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    if (isCancelled()) {
                        return true;
                    }
                }
                if (taskBean.isWaitLog()) {
                    ClickLogBean waitLog = new ClickLogBean();
                    waitLog.setClickTime(String.valueOf(wait))
                            .setClickKey(mouseButton_none())
                            .setType(log_wait())
                            .setName(name);
                    clickLog.add(waitLog);
                }
                openLink(clickPositionVO, taskBean);
                return false;
            }
        };
    }

    /**
     * 复制要执行的操作步骤
     *
     * @param tableViewItems 需要复制的操作步骤
     * @return 复制的操作步骤
     */
    private static List<ClickPositionVO> creatBackUp(List<? extends ClickPositionVO> tableViewItems) {
        List<ClickPositionVO> backup = new ArrayList<>(tableViewItems.size());
        for (ClickPositionVO clickPositionVO : tableViewItems) {
            ClickPositionVO vo = new ClickPositionVO();
            try {
                // 自动拷贝父类中的属性
                copyAllProperties(clickPositionVO, vo);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            backup.add(vo);
        }
        return backup;
    }

    /**
     * 处理要链接相关操作
     *
     * @param clickPositionVO 自动操作设置信息
     * @param taskBean        任务设置信息
     * @throws Exception 运行脚本时发生错误、链接操作异常
     */
    private static void openLink(ClickPositionVO clickPositionVO, AutoClickTaskBean taskBean) throws Exception {
        long start = System.currentTimeMillis();
        String link = clickPositionVO.getTargetPath();
        String workDir = clickPositionVO.getWorkPath();
        String param = clickPositionVO.getParameter();
        int clickType = clickPositionVO.getClickTypeEnum();
        boolean minScriptWindow = clickPositionVO.getMinScriptWindow().equals(activation);
        String name = clickPositionVO.getName();
        if (StringUtils.isBlank(link)) {
            throw new RuntimeException(text_pathNull());
        }
        if (clickType == ClickTypeEnum.OPEN_FILE.ordinal()) {
            File file = new File(link);
            if (!file.exists()) {
                throw new RuntimeException(text_fileNotExists());
            }
            openFile(link);
            if (taskBean.isOpenFileLog()) {
                long end = System.currentTimeMillis();
                String key = file.getName();
                long time = end - start;
                addLinkLog(name, time, key, clickType_openFile());
            }
        } else if (clickType == ClickTypeEnum.OPEN_URL.ordinal()) {
            if (isValidUrl(link)) {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                    if (taskBean.isOpenUrlLog()) {
                        long end = System.currentTimeMillis();
                        long time = end - start;
                        addLinkLog(name, time, link, clickType_openUrl());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(text_urlErr());
                }
            } else {
                throw new RuntimeException(text_urlErr());
            }
        } else if (clickType == ClickTypeEnum.RUN_SCRIPT.ordinal()) {
            File file = new File(link);
            if (!file.exists()) {
                throw new RuntimeException(text_fileNotExists());
            }
            runScript(file, workDir, param, minScriptWindow);
            if (taskBean.isRunScriptLog()) {
                long end = System.currentTimeMillis();
                String key = file.getName();
                long time = end - start;
                addLinkLog(name, time, key, clickType_runScript());
            }
        }
    }

    /**
     * 运行脚本任务
     *
     * @param taskBean        任务参数
     * @param script          要运行的脚本文件
     * @param workDir         运行脚本的目录
     * @param parameter       运行脚本的参数
     * @param minScriptWindow 是否最小化窗口 （true 最小化窗口执行）
     * @return 运行脚本任务对象
     */
    public static Task<Void> scriptRun(TaskBean<?> taskBean, File script, String workDir,
                                       String parameter, boolean minScriptWindow) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                changeDisableNodes(taskBean, true);
                updateMessage(text_testing());
                runScript(script, workDir, parameter, minScriptWindow);
                updateMessage("");
                return null;
            }
        };
    }

    /**
     * 添加链接操作日志
     *
     * @param name      操作名称
     * @param clickTime 操作时长
     * @param key       链接对应的文件名称或链接网址
     * @param type      操作类型
     */
    private static void addLinkLog(String name, long clickTime, String key, String type) {
        ClickLogBean clickLogBean = new ClickLogBean()
                .setClickTime(String.valueOf(clickTime))
                .setClickKey(mouseButton_none())
                .setResult(key)
                .setType(type)
                .setName(name);
        clickLog.add(clickLogBean);
    }

    /**
     * 显示错误弹窗
     *
     * @param errs    错误信息
     * @param errType 错误类型
     * @return true-显示弹窗 false-不显示弹窗
     */
    private static boolean showErrAlert(List<String> errs, String errType) {
        if (CollectionUtils.isNotEmpty(errs)) {
            Platform.runLater(() -> showStageAlert(errs, errType, mainStage));
            return true;
        }
        return false;
    }

    /**
     * 按照操作设置执行操作
     *
     * @param clickPositionVO 操作设置
     * @param robot           Robot 实例
     * @param loopTimeText    信息浮窗日志
     * @param taskBean        线程任务参数
     * @return 执行结果
     */
    private static ClickResultBean click(ClickPositionVO clickPositionVO, Robot robot,
                                         String loopTimeText, AutoClickTaskBean taskBean) throws Exception {
        ClickResultBean clickResultBean = new ClickResultBean();
        clickResultBean.setStepIndex(0);
        int retrySecondValue = taskBean.getRetrySecondValue();
        int overTimeValue = taskBean.getOverTimeValue();
        String name = clickPositionVO.getName();
        double startX = Double.parseDouble(clickPositionVO.getStartX());
        double startY = Double.parseDouble(clickPositionVO.getStartY());
        // 匹配终止操作图像
        matchStopImg(clickPositionVO, loopTimeText, taskBean, clickResultBean);
        // 匹配要点击的图像
        String clickPath = clickPositionVO.getClickImgPath();
        int clickType = clickPositionVO.getClickTypeEnum();
        AtomicReference<String> fileName = new AtomicReference<>();
        if (StringUtils.isNotBlank(clickPath)) {
            fileName.set(getExistsFileName(new File(clickPath)));
            String text = loopTimeText + text_searchingClick() + fileName.get();
            // 更新操作信息
            updateFloatingMessage(text);
            long start = System.currentTimeMillis();
            int retryType = clickPositionVO.getRetryTypeEnum();
            double clickMatchThreshold = Double.parseDouble(clickPositionVO.getClickMatchThreshold());
            FloatingWindowConfig clickWindowConfig = clickPositionVO.getClickWindowConfig();
            // 实时刷新
            if (clickWindowConfig != null) {
                WindowInfo windowInfo = clickWindowConfig.getWindowInfo();
                if (windowInfo != null && activation.equals(clickWindowConfig.getAlwaysRefresh())) {
                    clickWindowConfig.setWindowInfo(getMainWindowInfo(windowInfo.getProcessPath()));
                }
            }
            FindPositionConfig findPositionConfig = new FindPositionConfig()
                    .setMaxRetry(Integer.parseInt(clickPositionVO.getClickRetryTimes()))
                    .setContinuously(RetryTypeEnum.CONTINUOUSLY.ordinal() == retryType)
                    .setFloatingWindowConfig(clickWindowConfig)
                    .setMatchThreshold(clickMatchThreshold)
                    .setRetryWait(retrySecondValue)
                    .setOverTime(overTimeValue)
                    .setTemplatePath(clickPath)
                    .setName(name);
            MatchPointBean matchPointBean = findPosition(findPositionConfig, clickLog);
            try (Point position = matchPointBean.getPoint()) {
                int matchedType = clickPositionVO.getMatchedTypeEnum();
                long end = System.currentTimeMillis();
                if (ClickTypeEnum.MOVETO.ordinal() != clickType) {
                    startX = position.x() + Integer.parseInt(clickPositionVO.getImgX());
                    startY = position.y() + Integer.parseInt(clickPositionVO.getImgY());
                }
                int matchThreshold = matchPointBean.getMatchThreshold();
                if (taskBean.isClickImgLog()) {
                    ClickLogBean clickLogBean = new ClickLogBean();
                    clickLogBean.setClickTime(String.valueOf(end - start))
                            .setResult(matchThreshold + percentage)
                            .setX(String.valueOf(position.x()))
                            .setY(String.valueOf(position.y()))
                            .setClickKey(mouseButton_none())
                            .setType(log_clickImg())
                            .setName(name);
                    clickLog.add(clickLogBean);
                }
                if (matchThreshold >= clickMatchThreshold) {
                    // 匹配成功后直接执行下一个操作步骤
                    if (MatchedTypeEnum.BREAK.ordinal() == matchedType) {
                        clickResultBean.setClickLogs(clickLog.getSnapshot());
                        return clickResultBean;
                        // 匹配成功后执行指定步骤
                    } else if (MatchedTypeEnum.STEP.ordinal() == matchedType) {
                        clickResultBean.setStepIndex(Integer.parseInt(clickPositionVO.getMatchedStep()))
                                .setClickLogs(clickLog.getSnapshot());
                        return clickResultBean;
                        // 匹配成功后点击匹配图像并执行指定步骤
                    } else if (MatchedTypeEnum.CLICK_STEP.ordinal() == matchedType) {
                        clickResultBean.setStepIndex(Integer.parseInt(clickPositionVO.getMatchedStep()));
                        // 匹配图像存在则重复点击
                    } else if (MatchedTypeEnum.CLICK_WHILE.ordinal() == matchedType) {
                        clickResultBean.setStepIndex(-1);
                    }
                    // 匹配失败后或图像识别匹配逻辑为 匹配图像存在则重复点击 跳过本次操作
                } else if (RetryTypeEnum.BREAK.ordinal() == retryType ||
                        MatchedTypeEnum.CLICK_WHILE.ordinal() == matchedType) {
                    clickResultBean.setClickLogs(clickLog.getSnapshot());
                    return clickResultBean;
                    // 匹配失败后终止操作
                } else if (RetryTypeEnum.STOP.ordinal() == retryType) {
                    clickResultBean.setClickLogs(clickLog.getSnapshot());
                    try {
                        throw new RuntimeException(text_index() + clickPositionVO.getIndex() + text_taskErr() +
                                text_maxRetry() + clickPositionVO.getClickRetryTimes() + " " + unit_times() +
                                text_notFound() + fileName.get() +
                                text_closestMatchThreshold() + matchPointBean.getMatchThreshold() + percentage +
                                "\n" + text_point() + " X：" + position.x() + " Y：" + position.y());
                    } catch (Exception e) {
                        clickResultBean.setClickLogs(clickLog.getSnapshot());
                        throw new RuntimeException(e);
                    }
                    // 匹配失败后执行指定步骤
                } else if (RetryTypeEnum.STEP.ordinal() == retryType) {
                    clickResultBean.setStepIndex(Integer.parseInt(clickPositionVO.getRetryStep()))
                            .setClickLogs(clickLog.getSnapshot());
                    return clickResultBean;
                }
            }
        }
        // 按照操作次数执行
        executeClick(clickPositionVO, robot, taskBean, clickResultBean, startX, startY);
        clickResultBean.setClickLogs(clickLog.getSnapshot());
        return clickResultBean;
    }

    /**
     * 执行操作
     *
     * @param clickPositionVO 操作步骤设置
     * @param robot           Robot 实例
     * @param taskBean        线程任务设置
     * @param clickResultBean 操作结果类
     * @param startX          操作起始 X 坐标
     * @param startY          操作起始 Y 坐标
     * @throws Exception 当移动超时或线程中断时抛出
     */
    private static void executeClick(ClickPositionVO clickPositionVO, Robot robot, AutoClickTaskBean taskBean,
                                     ClickResultBean clickResultBean, double startX, double startY) throws Exception {
        int clickNum = Integer.parseInt(clickPositionVO.getClickNum());
        long clickTime = Long.parseLong(clickPositionVO.getClickTime());
        long clickInterval = Long.parseLong(clickPositionVO.getClickInterval());
        int randomTime = Integer.parseInt(clickPositionVO.getRandomTime());
        int clickType = clickPositionVO.getClickTypeEnum();
        String name = clickPositionVO.getName();
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
                            .setClickKey(mouseButton_none())
                            .setType(log_wait())
                            .setName(name);
                    clickLog.add(clickLogBean);
                }
            }
            MouseButton mouseButton = NativeMouseToMouseButton.get(clickPositionVO.getMouseKeyEnum());
            int keyCode = clickPositionVO.getKeyboardKeyEnum();
            String clickKey = clickPositionVO.getMouseKey();
            String keyboard = clickPositionVO.getKeyboardKey();
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
                if (unActivation.equals(clickPositionVO.getNoMove())) {
                    robot.mouseMove(finalStartX, finalStartY);
                    if (taskBean.isMoveLog()) {
                        ClickLogBean moveLog = new ClickLogBean();
                        moveLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(mouseButton_none())
                                .setType(log_move())
                                .setName(name);
                        clickLog.add(moveLog);
                    }
                }
                // 执行自动流程前点击第一个起始坐标
                if (firstClick.compareAndSet(true, false)) {
                    robot.mouseMove(finalStartX, finalStartY);
                    if (taskBean.isMoveLog()) {
                        ClickLogBean moveLog = new ClickLogBean();
                        moveLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(mouseButton_none())
                                .setType(log_move())
                                .setName(name);
                        clickLog.add(moveLog);
                    }
                    robot.mousePress(MouseButton.PRIMARY);
                    if (taskBean.isClickLog()) {
                        ClickLogBean pressLog = new ClickLogBean();
                        pressLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(clickKey)
                                .setType(log_press())
                                .setName(name);
                        clickLog.add(pressLog);
                    }
                    robot.mouseRelease(MouseButton.PRIMARY);
                    if (taskBean.isClickLog()) {
                        ClickLogBean releaseLog = new ClickLogBean();
                        releaseLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setType(log_release())
                                .setClickKey(clickKey)
                                .setName(name);
                        clickLog.add(releaseLog);
                    }
                }
                if (ClickTypeEnum.CLICK.ordinal() == clickType && mouseButton != MouseButton.NONE) {
                    robot.mousePress(mouseButton);
                    if (taskBean.isClickLog()) {
                        ClickLogBean pressLog = new ClickLogBean();
                        pressLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(clickKey)
                                .setType(log_press())
                                .setName(name);
                        clickLog.add(pressLog);
                    }
                } else if (ClickTypeEnum.WHEEL_DOWN.ordinal() == clickType) {
                    robot.mouseWheel(1);
                    if (taskBean.isClickLog()) {
                        ClickLogBean wheelLog = new ClickLogBean();
                        wheelLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setType(clickType_wheelDown())
                                .setClickKey(clickKey)
                                .setName(name);
                        clickLog.add(wheelLog);
                    }
                } else if (ClickTypeEnum.WHEEL_UP.ordinal() == clickType) {
                    robot.mouseWheel(-1);
                    if (taskBean.isClickLog()) {
                        ClickLogBean wheelLog = new ClickLogBean();
                        wheelLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setType(clickType_wheelUp())
                                .setClickKey(clickKey)
                                .setName(name);
                        clickLog.add(wheelLog);
                    }
                } else if (ClickTypeEnum.KEYBOARD.ordinal() == clickType) {
                    isRobotInput = true;
                    robot.keyPress(NativeKeyToKeyCode.get(keyCode));
                    if (taskBean.isKeyboardLog()) {
                        ClickLogBean keyPressLog = new ClickLogBean();
                        keyPressLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(keyboard)
                                .setType(log_press())
                                .setName(name);
                        clickLog.add(keyPressLog);
                    }
                }
                actionFuture.complete(null);
            });
            // 等待任务完成
            try {
                actionFuture.get();
            } catch (Exception e) {
                clickResultBean.setClickLogs(clickLog.getSnapshot());
                Thread.currentThread().interrupt();
                break;
            }
            // 执行长按操作
            if (ClickTypeEnum.DRAG.ordinal() != clickType &&
                    ClickTypeEnum.MOVE_TRAJECTORY.ordinal() != clickType &&
                    ClickTypeEnum.COMBINATIONS.ordinal() != clickType) {
                // 处理随机点击时长偏移
                if (activation.equals(clickPositionVO.getRandomClickTime())) {
                    clickTime = (long) Math.max(0, clickTime + (random.nextDouble() * 2 - 1) * randomTime);
                }
                // 单次操作时间
                try {
                    Thread.sleep(clickTime);
                } catch (InterruptedException e) {
                    clickResultBean.setClickLogs(clickLog.getSnapshot());
                    Thread.currentThread().interrupt();
                    break;
                }
                if (taskBean.isClickLog() && !mouseButton_none().equals(clickKey)) {
                    ClickLogBean clickLog = new ClickLogBean();
                    clickLog.setClickTime(String.valueOf(clickTime))
                            .setX(String.valueOf((int) finalStartX))
                            .setY(String.valueOf((int) finalStartY))
                            .setClickKey(clickKey)
                            .setType(log_hold())
                            .setName(name);
                    AutoClickService.clickLog.add(clickLog);
                } else if (taskBean.isKeyboardLog() && ClickTypeEnum.KEYBOARD.ordinal() == clickType) {
                    ClickLogBean clickLog = new ClickLogBean();
                    clickLog.setClickTime(String.valueOf(clickTime))
                            .setX(String.valueOf((int) finalStartX))
                            .setY(String.valueOf((int) finalStartY))
                            .setClickKey(keyboard)
                            .setType(log_hold())
                            .setName(name);
                    AutoClickService.clickLog.add(clickLog);
                }
                CompletableFuture<Void> releaseFuture = new CompletableFuture<>();
                Platform.runLater(() -> {
                    if (ClickTypeEnum.CLICK.ordinal() == clickType && mouseButton != MouseButton.NONE) {
                        robot.mouseRelease(mouseButton);
                        if (taskBean.isClickLog()) {
                            ClickLogBean releaseLog = new ClickLogBean();
                            releaseLog.setX(String.valueOf((int) finalStartX))
                                    .setY(String.valueOf((int) finalStartY))
                                    .setType(log_release())
                                    .setClickKey(clickKey)
                                    .setName(name);
                            clickLog.add(releaseLog);
                        }
                    } else if (ClickTypeEnum.KEYBOARD.ordinal() == clickType) {
                        robot.keyRelease(NativeKeyToKeyCode.get(keyCode));
                        isRobotInput = false;
                        if (taskBean.isKeyboardLog()) {
                            ClickLogBean releaseLog = new ClickLogBean();
                            releaseLog.setX(String.valueOf((int) finalStartX))
                                    .setY(String.valueOf((int) finalStartY))
                                    .setType(log_release())
                                    .setClickKey(keyboard)
                                    .setName(name);
                            clickLog.add(releaseLog);
                        }
                    }
                    releaseFuture.complete(null);
                });
                // 等待任务完成
                try {
                    releaseFuture.get();
                } catch (Exception e) {
                    clickResultBean.setClickLogs(clickLog.getSnapshot());
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // 计算鼠标轨迹
                executeTrajectoryPoints(robot, clickPositionVO, taskBean);
                clickResultBean.setClickLogs(clickLog.getSnapshot());
            }
        }
    }

    /**
     * 匹配终止操作图片
     *
     * @param clickPositionVO 操作步骤设置
     * @param loopTimeText    当前操作步骤信息
     * @param taskBean        线程任务设置
     * @param clickResultBean 匹配结果详情
     */
    private static void matchStopImg(ClickPositionVO clickPositionVO, String loopTimeText,
                                     AutoClickTaskBean taskBean, ClickResultBean clickResultBean) {
        String name = clickPositionVO.getName();
        int overTimeValue = taskBean.getOverTimeValue();
        int retrySecondValue = taskBean.getRetrySecondValue();
        List<ImgFileBean> stopImgFileVOS = clickPositionVO.getStopImgFiles();
        if (CollectionUtils.isNotEmpty(stopImgFileVOS)) {
            stopImgFileVOS.stream().parallel().forEach(stopImgFileBean -> {
                String stopPath = stopImgFileBean.getPath();
                AtomicReference<String> fileName = new AtomicReference<>();
                fileName.set(getExistsFileName(new File(stopPath)));
                String text = loopTimeText + text_searchingStop() + fileName.get();
                // 更新操作信息
                updateFloatingMessage(text);
                long start = System.currentTimeMillis();
                double stopMatchThreshold = Double.parseDouble(clickPositionVO.getStopMatchThreshold());
                FloatingWindowConfig stopWindowConfig = clickPositionVO.getStopWindowConfig();
                // 实时刷新
                if (stopWindowConfig != null) {
                    WindowInfo windowInfo = stopWindowConfig.getWindowInfo();
                    if (windowInfo != null && activation.equals(stopWindowConfig.getAlwaysRefresh())) {
                        stopWindowConfig.setWindowInfo(getMainWindowInfo(windowInfo.getProcessPath()));
                    }
                }
                FindPositionConfig findPositionConfig = new FindPositionConfig()
                        .setMaxRetry(Integer.parseInt(clickPositionVO.getStopRetryTimes()))
                        .setFloatingWindowConfig(stopWindowConfig)
                        .setMatchThreshold(stopMatchThreshold)
                        .setRetryWait(retrySecondValue)
                        .setOverTime(overTimeValue)
                        .setTemplatePath(stopPath)
                        .setContinuously(false)
                        .setName(name);
                MatchPointBean matchPointBean;
                try {
                    matchPointBean = findPosition(findPositionConfig, clickLog);
                } catch (Exception e) {
                    clickResultBean.setClickLogs(clickLog.getSnapshot());
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
                                .setResult(matchThreshold + percentage)
                                .setClickKey(mouseButton_none())
                                .setX(String.valueOf(x))
                                .setY(String.valueOf(y))
                                .setType(log_stopImg())
                                .setName(name);
                        clickLog.add(clickLogBean);
                    }
                    if (matchThreshold >= stopMatchThreshold) {
                        clickResultBean.setClickLogs(clickLog.getSnapshot());
                        throw new RuntimeException(text_index() + clickPositionVO.getIndex() + text_taskStop() +
                                text_findStopImg() + fileName.get() +
                                text_matchThreshold() + matchThreshold + percentage +
                                "\n" + text_point() + " X：" + x + " Y：" + y);
                    }
                } catch (Exception e) {
                    clickResultBean.setClickLogs(clickLog.getSnapshot());
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * 精确执行轨迹点移动序列
     *
     * @param robot           机器人操作实例
     * @param clickPositionVO 点击位置信息
     * @param taskBean        线程任务参数
     * @throws Exception 当移动超时或线程中断时抛出
     */
    private static void executeTrajectoryPoints(Robot robot, ClickPositionVO clickPositionVO,
                                                AutoClickTaskBean taskBean) throws Exception {
        String name = clickPositionVO.getName();
        List<TrajectoryPointBean> points = clickPositionVO.getMoveTrajectory();
        if (!points.isEmpty()) {
            TrajectoryPointBean lastPoint = null;
            List<CompletableFuture<Void>> allFutures = new ArrayList<>();
            for (TrajectoryPointBean point : points) {
                // 当前轨迹点按下的鼠标按键
                List<Integer> pressMouseKeys = point.getPressMouseKeys();
                // 当前轨迹点要抬起的鼠标按键
                List<Integer> releaseMouseKeys = new CopyOnWriteArrayList<>();
                // 当前轨迹点新增的要按下的鼠标按键
                List<Integer> nowPressMouseKeys;
                // 当前轨迹点按下的键盘按键
                List<Integer> pressKeyboardKeys = point.getPressKeyboardKeys();
                // 当前轨迹点要抬起的键盘按键
                List<Integer> releaseKeyboardKeys = new CopyOnWriteArrayList<>();
                // 当前轨迹点新增的要按下的键盘按键
                List<Integer> nowPressKeyboardKeys;
                long remaining = 0;
                if (lastPoint != null) {
                    remaining = point.getTimestamp() - lastPoint.getTimestamp();
                    // 上一个轨迹点按下的鼠标按键
                    List<Integer> lastPressMouseKeys = lastPoint.getPressMouseKeys();
                    if (CollectionUtils.isEmpty(pressMouseKeys)) {
                        nowPressMouseKeys = null;
                        releaseMouseKeys = lastPressMouseKeys;
                    } else {
                        if (CollectionUtils.isNotEmpty(lastPressMouseKeys)) {
                            releaseMouseKeys = (List<Integer>) CollectionUtils.subtract(lastPressMouseKeys, pressMouseKeys);
                            nowPressMouseKeys = (List<Integer>) CollectionUtils.subtract(pressMouseKeys, lastPressMouseKeys);
                        } else {
                            nowPressMouseKeys = pressMouseKeys;
                        }
                    }
                    // 上一个轨迹点按下的键盘按键
                    List<Integer> lastPressKeyboardKeys = lastPoint.getPressKeyboardKeys();
                    if (CollectionUtils.isEmpty(pressKeyboardKeys)) {
                        nowPressKeyboardKeys = null;
                        releaseKeyboardKeys = lastPressKeyboardKeys;
                    } else {
                        if (CollectionUtils.isNotEmpty(lastPressKeyboardKeys)) {
                            releaseKeyboardKeys = (List<Integer>) CollectionUtils.subtract(lastPressKeyboardKeys, pressKeyboardKeys);
                            nowPressKeyboardKeys = (List<Integer>) CollectionUtils.subtract(pressKeyboardKeys, lastPressKeyboardKeys);
                        } else {
                            nowPressKeyboardKeys = pressKeyboardKeys;
                        }
                    }
                } else {
                    nowPressMouseKeys = pressMouseKeys;
                    nowPressKeyboardKeys = pressKeyboardKeys;
                }
                lastPoint = point;
                double x = point.getX();
                double y = point.getY();
                int wheelRotation = point.getWheelRotation();
                // 处理相对路径
                String useRelative = clickPositionVO.getUseRelative();
                FloatingWindowConfig clickWindowConfig = clickPositionVO.getClickWindowConfig();
                if (clickWindowConfig != null &&
                        FindImgTypeEnum.WINDOW.ordinal() == clickWindowConfig.getFindImgTypeEnum()) {
                    WindowInfo windowInfo = clickWindowConfig.getWindowInfo();
                    if (windowInfo != null && activation.equals(useRelative)) {
                        String relativeX = point.getRelativeX();
                        String relativeY = point.getRelativeY();
                        if (StringUtils.isNotBlank(relativeX) && StringUtils.isNotBlank(relativeY)) {
                            double rX = Double.parseDouble(relativeX);
                            double rY = Double.parseDouble(relativeY);
                            Map<String, Integer> absolutePosition = WindowMonitor.calculateAbsolutePosition(windowInfo, rX, rY);
                            x = absolutePosition.get(AbsoluteX);
                            y = absolutePosition.get(AbsoluteY);
                        }
                    }
                }
                if (activation.equals(clickPositionVO.getRandomTrajectory())) {
                    int randomX = Integer.parseInt(clickPositionVO.getRandomX());
                    int randomY = Integer.parseInt(clickPositionVO.getRandomY());
                    x = Math.min(Math.max(0, x + (random.nextDouble() * 2 - 1) * randomX), screenWidth);
                    y = Math.min(Math.max(0, y + (random.nextDouble() * 2 - 1) * randomY), screenHeight);
                }
                CompletableFuture<Void> moveFuture = new CompletableFuture<>();
                allFutures.add(moveFuture);
                List<Integer> finalReleaseButtons = releaseMouseKeys;
                List<Integer> finalReleaseKeyboardKeys = releaseKeyboardKeys;
                double finalX = x;
                double finalY = y;
                // 精确控制时间间隔
                if (remaining > 0) {
                    Thread.sleep(remaining);
                    if (taskBean.isWaitLog()) {
                        ClickLogBean sleepLog = new ClickLogBean();
                        sleepLog.setClickTime(String.valueOf(remaining))
                                .setClickKey(mouseButton_none())
                                .setType(log_wait())
                                .setName(name);
                        clickLog.add(sleepLog);
                    }
                }
                Platform.runLater(() -> {
                    if (CollectionUtils.isNotEmpty(nowPressMouseKeys)) {
                        nowPressMouseKeys.forEach(button -> {
                            robot.mousePress(NativeMouseToMouseButton.get(button));
                            if (taskBean.isDragLog()) {
                                ClickLogBean clickLog = new ClickLogBean();
                                clickLog.setClickKey(recordClickTypeMap.get(button))
                                        .setX(String.valueOf((int) finalX))
                                        .setY(String.valueOf((int) finalY))
                                        .setType(log_press())
                                        .setName(name);
                                AutoClickService.clickLog.add(clickLog);
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
                                clickLog.add(releaseLog);
                            }
                        });
                    }
                    if (CollectionUtils.isNotEmpty(nowPressKeyboardKeys)) {
                        nowPressKeyboardKeys.forEach(button -> {
                            isRobotInput = true;
                            robot.keyPress(NativeKeyToKeyCode.get(button));
                            if (taskBean.isKeyboardLog()) {
                                ClickLogBean pressLog = new ClickLogBean();
                                pressLog.setX(String.valueOf((int) finalX))
                                        .setY(String.valueOf((int) finalY))
                                        .setClickKey(getKeyText(button))
                                        .setType(log_press())
                                        .setName(name);
                                clickLog.add(pressLog);
                            }
                        });
                    }
                    if (CollectionUtils.isNotEmpty(finalReleaseKeyboardKeys)) {
                        finalReleaseKeyboardKeys.forEach(button -> {
                            robot.keyRelease(NativeKeyToKeyCode.get(button));
                            isRobotInput = false;
                            if (taskBean.isKeyboardLog()) {
                                ClickLogBean releaseLog = new ClickLogBean();
                                releaseLog.setX(String.valueOf((int) finalX))
                                        .setY(String.valueOf((int) finalY))
                                        .setClickKey(getKeyText(button))
                                        .setType(log_release())
                                        .setName(name);
                                clickLog.add(releaseLog);
                            }
                        });
                    }
                    if (unActivation.equals(clickPositionVO.getNoMove())) {
                        robot.mouseMove(finalX, finalY);
                    }
                    robot.mouseWheel(wheelRotation);
                    if (taskBean.isMouseWheelLog() && wheelRotation != 0) {
                        ClickLogBean wheelLog = new ClickLogBean();
                        wheelLog.setX(String.valueOf((int) finalX))
                                .setY(String.valueOf((int) finalY))
                                .setClickKey(mouseButton_none())
                                .setName(name);
                        if (wheelRotation > 0) {
                            wheelLog.setType(clickType_wheelDown());
                        } else {
                            wheelLog.setType(clickType_wheelUp());
                        }
                        clickLog.add(wheelLog);
                    }
                    if (CollectionUtils.isEmpty(pressMouseKeys) && taskBean.isMoveLog()) {
                        ClickLogBean moveLog = new ClickLogBean();
                        moveLog.setX(String.valueOf((int) finalX))
                                .setY(String.valueOf((int) finalY))
                                .setClickKey(mouseButton_none())
                                .setType(log_move())
                                .setName(name);
                        clickLog.add(moveLog);
                    } else if (CollectionUtils.isNotEmpty(pressMouseKeys) && taskBean.isDragLog()) {
                        List<String> clickKeys = new ArrayList<>();
                        pressMouseKeys.forEach(button -> {
                            String clickKey = recordClickTypeMap.get(button);
                            clickKeys.add(clickKey);
                        });
                        ClickLogBean moveLog = new ClickLogBean();
                        moveLog.setClickKey(String.join(",", clickKeys))
                                .setX(String.valueOf((int) finalX))
                                .setY(String.valueOf((int) finalY))
                                .setType(log_drag())
                                .setName(name);
                        clickLog.add(moveLog);
                    }
                    moveFuture.complete(null);
                });
            }
            // 等待所有异步操作完成
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).get();
        } else {
            String clickTime = clickPositionVO.getClickTime();
            long waitTime = Long.parseLong(clickTime);
            Thread.sleep(waitTime);
            if (taskBean.isWaitLog()) {
                ClickLogBean sleepLog = new ClickLogBean();
                sleepLog.setClickTime(String.valueOf(waitTime))
                        .setClickKey(mouseButton_none())
                        .setType(log_wait())
                        .setName(name);
                clickLog.add(sleepLog);
            }
        }
    }

    /**
     * 更新操作信息
     *
     * @param message 要更新的信息
     */
    private static void updateFloatingMessage(String message) {
        Platform.runLater(() -> {
            if (messageLabel != null) {
                messageLabel.setText(message);
            }
            if (messageFloating != null) {
                updateMessageLabel(messageFloating, message);
            }
        });
    }

    /**
     * 解除组件引用
     */
    public static void clearReferences() {
        messageFloating = null;
        messageLabel = null;
        clickLog = null;
    }

    /**
     * 获取当前产生的日志（任务终止时使用）
     *
     * @return 当前产生的日志
     **/
    public static List<ClickLogBean> getNowLogs() {
        if (clickLog == null) {
            return null;
        }
        return clickLog.getSnapshot();
    }

}
