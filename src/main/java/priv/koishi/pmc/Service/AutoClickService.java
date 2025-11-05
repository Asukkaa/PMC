package priv.koishi.pmc.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.Point;
import priv.koishi.pmc.Bean.*;
import priv.koishi.pmc.Bean.Config.FileConfig;
import priv.koishi.pmc.Bean.Config.FindPositionConfig;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.Result.PMCLoadResult;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
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
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.getMainWindowInfo;
import static priv.koishi.pmc.MainApplication.mainStage;
import static priv.koishi.pmc.Service.ImageRecognitionService.*;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.updateMassageLabel;
import static priv.koishi.pmc.Utils.CommonUtils.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ScriptUtils.runScript;
import static priv.koishi.pmc.Utils.UiUtils.*;

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
    private static FloatingWindowDescriptor massageFloating;

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
     * 批量加载 PMC 文件
     *
     * @param taskBean 线程任务参数
     * @param files    文件列表
     * @return PMC 文件列表
     */
    public static Task<PMCLoadResult> loadPMCFils(TaskBean<ClickPositionVO> taskBean, List<? extends File> files) {
        return new Task<>() {
            @Override
            protected PMCLoadResult call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                List<ClickPositionVO> clickPositionBeans = new ArrayList<>();
                Map<String, String> imgMap = new HashMap<>();
                String lastPMCPath = "";
                int size = files.size();
                updateProgress(0, size);
                for (int i = 0; i < size; i++) {
                    File file = files.get(i);
                    if (PMC.equals(getExistsFileType(file))) {
                        lastPMCPath = file.getPath();
                        try {
                            clickPositionBeans.addAll(loadPMCFile(file));
                        } catch (IOException e) {
                            showExceptionAlert(e);
                        }
                    } else if (file.isDirectory()) {
                        List<String> filterExtensionList = new ArrayList<>(imageType);
                        filterExtensionList.add(PMC);
                        FileConfig fileConfig = new FileConfig()
                                .setFilterExtensionList(filterExtensionList)
                                .setShowDirectory(search_fileDirectory())
                                .setShowHideFile(hide_noHideFile())
                                .setPath(file.getPath())
                                .setRecursion(true);
                        for (File readFile : readAllFiles(fileConfig)) {
                            if (PMC.equals(getExistsFileType(readFile))) {
                                lastPMCPath = readFile.getPath();
                                try {
                                    clickPositionBeans.addAll(loadPMCFile(readFile));
                                } catch (IOException e) {
                                    showExceptionAlert(e);
                                }
                            } else {
                                imgMap.put(readFile.getPath(), getExistsFileName(readFile));
                            }
                        }
                    } else if (imageType.contains(getExistsFileType(file))) {
                        imgMap.put(file.getPath(), getExistsFileName(file));
                    }
                    updateProgress(i + 1, size);
                }
                // 匹配图片
                matchSameNameImg(imgMap, clickPositionBeans);
                taskBean.getTableView().refresh();
                return new PMCLoadResult(clickPositionBeans, lastPMCPath);
            }

            /**
             * 匹配图片
             *
             * @param imgMap 文件夹中用于匹配的图片
             * @param clickPositionBeans 需要匹配的自动操作步骤
             */
            private void matchSameNameImg(Map<String, String> imgMap, List<? extends ClickPositionVO> clickPositionBeans) {
                if (!imgMap.isEmpty()) {
                    updateMessage(text_matchImg());
                    int clickPositionBeansSize = clickPositionBeans.size();
                    updateProgress(0, clickPositionBeansSize);
                    // 匹配要点击的图片
                    for (int j = 0; j < clickPositionBeansSize; j++) {
                        ClickPositionVO clickPositionVO = clickPositionBeans.get(j);
                        String clickImgPath = clickPositionVO.getClickImgPath();
                        if (StringUtils.isNotBlank(clickImgPath)) {
                            File file = new File(clickImgPath);
                            if (!file.exists()) {
                                // 通过文件获取路径可消除不同操作系统的路径分隔符的差异
                                String imgPath = getSameNameImgPath(imgMap, file);
                                if (StringUtils.isNotBlank(imgPath)) {
                                    clickPositionVO.setClickImgPath(imgPath);
                                }
                            }
                        }
                        // 匹配终止操作图片
                        List<ImgFileBean> stopImgFiles = clickPositionVO.getStopImgFiles();
                        stopImgFiles.forEach(stopImgFile -> {
                            String stopImgPath = stopImgFile.getPath();
                            if (StringUtils.isNotBlank(stopImgPath)) {
                                File stopFile = new File(stopImgPath);
                                if (!stopFile.exists()) {
                                    String imgPath = getSameNameImgPath(imgMap, stopFile);
                                    if (StringUtils.isNotBlank(imgPath)) {
                                        stopImgFile.setPath(imgPath);
                                    }
                                }
                            }
                        });
                        updateProgress(j + 1, clickPositionBeansSize);
                    }
                }
            }
        };
    }

    /**
     * 获取匹配到的同名图片地址
     *
     * @param imgMap 要进行匹配的图片 map
     * @param img    路径改变了的图片文件
     * @return 匹配到的图片地址
     */
    private static String getSameNameImgPath(Map<String, String> imgMap, File img) {
        String imgName = getFileName(img.getPath());
        return imgMap.entrySet()
                .stream()
                .filter(entry -> imgName.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 加载 PMC 文件
     *
     * @param taskBean 线程任务参数
     * @param file     要加载的文件
     * @return PMC 文件列表
     */
    public static Task<List<ClickPositionVO>> loadPMC(TaskBean<ClickPositionVO> taskBean, File file) {
        return new Task<>() {
            @Override
            protected List<ClickPositionVO> call() throws IOException {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                return loadPMCFile(file);
            }
        };
    }

    /**
     * 导出 PMC 文件
     *
     * @param taskBean    线程任务参数
     * @param fileName    要导出的文件名
     * @param outFilePath 导出文件夹路径
     * @return 导出文件路径
     */
    public static Task<String> exportPMC(TaskBean<ClickPositionVO> taskBean, String fileName, String outFilePath, boolean notOverwrite) {
        return new Task<>() {
            @Override
            protected String call() throws IOException {
                changeDisableNodes(taskBean, true);
                updateMessage(text_exportData());
                List<ClickPositionVO> tableViewItems = taskBean.getBeanList();
                String path = outFilePath + File.separator + fileName + PMC;
                if (notOverwrite) {
                    path = notOverwritePath(path);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                // 构建基类类型信息
                JavaType baseType = objectMapper.getTypeFactory().constructParametricType(List.class, ClickPositionBean.class);
                // 使用基类类型进行序列化
                objectMapper.writerFor(baseType).writeValue(new File(path), tableViewItems);
                updateMessage(text_saveSuccess() + path);
                return path;
            }
        };
    }

    /**
     * 批量加载图片文件
     *
     * @param taskBean 线程任务参数
     * @param files    图片文件列表
     */
    public static Task<Void> loadImg(TaskBean<ImgFileVO> taskBean, List<? extends File> files) {
        return new Task<>() {
            @Override
            protected Void call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                TableView<ImgFileVO> tableView = taskBean.getTableView();
                ObservableList<ImgFileVO> items = tableView.getItems();
                int size = files.size();
                updateProgress(0, size);
                for (int i = 0; i < size; i++) {
                    File file = files.get(i);
                    boolean isExist = items.stream().anyMatch(bean -> file.getPath().equals(bean.getPath()));
                    if (!isExist) {
                        ImgFileVO imgFileVO = new ImgFileVO();
                        imgFileVO.setTableView(tableView)
                                .setType(getExistsFileType(file))
                                .setName(file.getName())
                                .setPath(file.getPath());
                        items.add(imgFileVO);
                    }
                    updateProgress(i + 1, size);
                }
                return null;
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
    public static Task<List<ClickLogBean>> autoClick(AutoClickTaskBean taskBean, Robot robot) {
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
                dynamicQueue = new DynamicQueue<>();
                int maxLogNum = taskBean.getMaxLogNum();
                if (maxLogNum > 0) {
                    dynamicQueue.setMaxSize(maxLogNum);
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
                updateMassage(text_checkJumpSetting());
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
                updateMassage(text_checkingWindowInfo());
                List<String> errs = new ArrayList<>();
                updateProgress(0, tableSize);
                // 错误的窗口设置集合
                List<Integer> clickErrIndex = new ArrayList<>();
                List<Integer> stopErrIndex = new ArrayList<>();
                // 校验窗口信息设置是否有误
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
                // 更新窗口信息
                Map<String, WindowInfo> windowInfoMap = new HashMap<>();
                int pathSize = processPaths.size();
                updateMassage(text_gettingWindowInfo());
                updateProgress(0, pathSize);
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
                // 校验窗口是否存在
                if (!windowInfoMap.isEmpty()) {
                    updateMassage(text_updatingWindowInfo());
                    updateProgress(0, tableSize);
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
                                    stopWindowConfig.setWindowInfo(windowInfo);
                                    clickPositionVO.setStopWindowConfig(stopWindowConfig);
                                }
                            }
                        }
                    }
                }
                return errs;
            }

            /**
             * 执行操作流程
             *
             * @param tableViewItems 自动操作设置列表
             * @param loopTimeText 循环信息文案
             */
            private List<ClickLogBean> clicks(List<? extends ClickPositionVO> tableViewItems, String loopTimeText) throws Exception {
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
                int dataSize = backup.size();
                massageFloating = taskBean.getMassageFloating();
                massageLabel = taskBean.getMassageLabel();
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
                            text_progress() + progress + "/" + dataSize +
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
                    String clickText;
                    if (clickType >= ClickTypeEnum.MOVE_TRAJECTORY.ordinal()) {
                        clickText = "";
                    } else {
                        clickText = text_taskInfo() + clickPositionVO.getClickKey() + clickPositionVO.getClickType();
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
                    updateMassage(text);
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
                        dynamicQueue.add(waitLog);
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
                return dynamicQueue.getSnapshot();
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
                updateMassage(text);
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
                    dynamicQueue.add(waitLog);
                }
                openLink(clickPositionVO, taskBean);
                return false;
            }
        };
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
                .setClickKey(key)
                .setType(type)
                .setName(name);
        dynamicQueue.add(clickLogBean);
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
                fileName.set(getExistsFileName(new File(stopPath)));
                String text = loopTimeText + text_searchingStop() + fileName.get();
                // 更新操作信息
                updateMassage(text);
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
                    matchPointBean = findPosition(findPositionConfig, dynamicQueue);
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
                                .setResult(matchThreshold + percentage)
                                .setClickKey(mouseButton_none())
                                .setX(String.valueOf(x))
                                .setY(String.valueOf(y))
                                .setType(log_stopImg())
                                .setName(name);
                        dynamicQueue.add(clickLogBean);
                    }
                    if (matchThreshold >= stopMatchThreshold) {
                        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                        throw new RuntimeException(text_index() + clickPositionVO.getIndex() + text_taskStop() +
                                text_findStopImg() + fileName.get() +
                                text_matchThreshold() + matchThreshold + percentage +
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
        int clickType = clickPositionVO.getClickTypeEnum();
        AtomicReference<String> fileName = new AtomicReference<>();
        if (StringUtils.isNotBlank(clickPath)) {
            fileName.set(getExistsFileName(new File(clickPath)));
            String text = loopTimeText + text_searchingClick() + fileName.get();
            // 更新操作信息
            updateMassage(text);
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
            MatchPointBean matchPointBean = findPosition(findPositionConfig, dynamicQueue);
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
                    dynamicQueue.add(clickLogBean);
                }
                if (matchThreshold >= clickMatchThreshold) {
                    // 匹配成功后直接执行下一个操作步骤
                    if (MatchedTypeEnum.BREAK.ordinal() == matchedType) {
                        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                        return clickResultBean;
                        // 匹配成功后执行指定步骤
                    } else if (MatchedTypeEnum.STEP.ordinal() == matchedType) {
                        clickResultBean.setStepIndex(Integer.parseInt(clickPositionVO.getMatchedStep()))
                                .setClickLogs(dynamicQueue.getSnapshot());
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
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    return clickResultBean;
                    // 匹配失败后终止操作
                } else if (RetryTypeEnum.STOP.ordinal() == retryType) {
                    clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                    try {
                        throw new RuntimeException(text_index() + clickPositionVO.getIndex() + text_taskErr() +
                                text_maxRetry() + clickPositionVO.getClickRetryTimes() + " " + unit_times() +
                                text_notFound() + fileName.get() +
                                text_closestMatchThreshold() + matchPointBean.getMatchThreshold() + percentage +
                                "\n" + text_point() + " X：" + position.x() + " Y：" + position.y());
                    } catch (Exception e) {
                        clickResultBean.setClickLogs(dynamicQueue.getSnapshot());
                        throw new RuntimeException(e);
                    }
                    // 匹配失败后执行指定步骤
                } else if (RetryTypeEnum.STEP.ordinal() == retryType) {
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
                            .setClickKey(mouseButton_none())
                            .setType(log_wait())
                            .setName(name);
                    dynamicQueue.add(clickLogBean);
                }
            }
            MouseButton mouseButton = NativeMouseToMouseButton.get(clickPositionVO.getClickKeyEnum());
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
                            .setClickKey(mouseButton_none())
                            .setType(log_move())
                            .setName(name);
                    dynamicQueue.add(moveLog);
                }
                // 执行自动流程前点击第一个起始坐标
                if (firstClick.compareAndSet(true, false)) {
                    robot.mousePress(MouseButton.PRIMARY);
                    if (taskBean.isClickLog()) {
                        ClickLogBean pressLog = new ClickLogBean();
                        pressLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setClickKey(clickKey)
                                .setType(log_press())
                                .setName(name);
                        dynamicQueue.add(pressLog);
                    }
                    robot.mouseRelease(MouseButton.PRIMARY);
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
                if (ClickTypeEnum.CLICK.ordinal() == clickType && mouseButton != MouseButton.NONE) {
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
                } else if (ClickTypeEnum.WHEEL_DOWN.ordinal() == clickType) {
                    robot.mouseWheel(1);
                    if (taskBean.isClickLog()) {
                        ClickLogBean wheelLog = new ClickLogBean();
                        wheelLog.setX(String.valueOf((int) finalStartX))
                                .setY(String.valueOf((int) finalStartY))
                                .setType(clickType_wheelDown())
                                .setClickKey(clickKey)
                                .setName(name);
                        dynamicQueue.add(wheelLog);
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
                        dynamicQueue.add(wheelLog);
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
            if (ClickTypeEnum.DRAG.ordinal() != clickType &&
                    ClickTypeEnum.MOVE_TRAJECTORY.ordinal() != clickType) {
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
                if (taskBean.isClickLog() && !mouseButton_none().equals(clickKey)) {
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
                    if (ClickTypeEnum.CLICK.ordinal() == clickType && mouseButton != MouseButton.NONE) {
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
                        dynamicQueue.add(wheelLog);
                    }
                    if (CollectionUtils.isEmpty(pressButtons) && taskBean.isMoveLog()) {
                        ClickLogBean moveLog = new ClickLogBean();
                        moveLog.setX(String.valueOf((int) finalX))
                                .setY(String.valueOf((int) finalY))
                                .setClickKey(mouseButton_none())
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
                                .setClickKey(mouseButton_none())
                                .setType(log_wait())
                                .setName(name);
                        dynamicQueue.add(sleepLog);
                    }
                }
            }
        }
    }

    /**
     * 更新操作信息
     *
     * @param message 要更新的信息
     */
    private static void updateMassage(String message) {
        Platform.runLater(() -> {
            if (massageLabel != null) {
                massageLabel.setText(message);
            }
            if (massageFloating != null) {
                updateMassageLabel(massageFloating, message);
            }
        });
    }

    /**
     * 解除组件引用
     */
    public static void clearReferences() {
        massageFloating = null;
        massageLabel = null;
        dynamicQueue = null;
    }

    /**
     * 获取当前产生的日志（任务终止时使用）
     *
     * @return 当前产生的日志
     **/
    public static List<ClickLogBean> getNowLogs() {
        if (dynamicQueue == null) {
            return null;
        }
        return dynamicQueue.getSnapshot();
    }

    /**
     * 导入自动操作流程文件
     *
     * @param jsonFile 要解析的文件
     * @throws IOException 导入自动化流程文件内容格式不正确
     */
    private static List<ClickPositionVO> loadPMCFile(File jsonFile) throws IOException {
        // 读取 JSON 文件并转换为 List<ClickPositionBean>
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = jsonFile.getAbsolutePath();
        List<ClickPositionBean> clickPositionBeans;
        try {
            clickPositionBeans = objectMapper.readValue(jsonFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
        } catch (MismatchedInputException | JsonParseException e) {
            throw new RuntimeException(text_loadAutoClick() + filePath + text_formatError());
        }
        // 定时执行导入自动操作并执行时如果不立刻设置序号会导致运行时找不到序号
        int index = 0;
        List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(clickPositionBeans)) {
            for (ClickPositionBean bean : clickPositionBeans) {
                ClickPositionVO vo = new ClickPositionVO();
                try {
                    // 自动拷贝父类中的属性
                    copyAllProperties(bean, vo);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                // 初始化子类特有属性
                vo.setTableView(autoClickController.tableView_Click)
                        .setRemove(false)
                        .setUuid(UUID.randomUUID().toString());
                vo.setIndex(index++);
                if (!retryTypeMap.containsKey(vo.getRetryTypeEnum())
                        || !clickTypeMap.containsKey(vo.getClickTypeEnum())
                        || !matchedTypeMap.containsKey(vo.getMatchedTypeEnum())
                        || !recordClickTypeMap.containsKey(vo.getClickKeyEnum())
                        || !activationList.contains(vo.getRandomClick())
                        || !activationList.contains(vo.getRandomWaitTime())
                        || !activationList.contains(vo.getRandomClickTime())
                        || !activationList.contains(vo.getRandomTrajectory())
                        || !activationList.contains(vo.getRandomClickInterval())
                        || !isInIntegerRange(vo.getStartX(), 0, null)
                        || !isInIntegerRange(vo.getStartY(), 0, null)
                        || !isInIntegerRange(vo.getRandomX(), 0, null)
                        || !isInIntegerRange(vo.getRandomY(), 0, null)
                        || !isInIntegerRange(vo.getImgX(), null, null)
                        || !isInIntegerRange(vo.getImgY(), null, null)
                        || !isInIntegerRange(vo.getWaitTime(), 0, null)
                        || !isInIntegerRange(vo.getClickNum(), 0, null)
                        || !isInIntegerRange(vo.getClickTime(), 0, null)
                        || !isInIntegerRange(vo.getClickInterval(), 0, null)
                        || !isInIntegerRange(vo.getStopRetryTimes(), 0, null)
                        || !isInIntegerRange(vo.getClickRetryTimes(), 0, null)
                        || !isInIntegerRange(vo.getRandomClickTime(), 0, null)
                        || !isInIntegerRange(vo.getStopMatchThreshold(), 0, 100)
                        || !isInIntegerRange(vo.getClickMatchThreshold(), 0, 100)) {
                    throw new RuntimeException(text_missingKeyData());
                }
                vo.setUuid(UUID.randomUUID().toString());
                clickPositionVOS.add(vo);
            }
        }
        return clickPositionVOS;
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

}
