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
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.Finals.Enum.RetryTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.Queue.DynamicQueue;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
import static priv.koishi.pmc.Utils.CommonUtils.copyAllProperties;
import static priv.koishi.pmc.Utils.FileUtils.*;
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
    public static Task<String> exportPMC(TaskBean<ClickPositionVO> taskBean, String fileName, String outFilePath) {
        return new Task<>() {
            @Override
            protected String call() throws IOException {
                changeDisableNodes(taskBean, true);
                updateMessage(text_exportData());
                List<ClickPositionVO> tableViewItems = taskBean.getBeanList();
                String path = notOverwritePath(outFilePath + File.separator + fileName + PMC);
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
                // 校验并更新识别范围设置
                List<String> errs = updateWindowInfos(tableViewItems);
                if (CollectionUtils.isNotEmpty(errs)) {
                    Platform.runLater(() -> showStageAlert(errs, text_windowInfoErr(), mainStage));
                    return null;
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
                for (int i = 0; i < tableSize; i++) {
                    updateProgress(i + 1, tableSize);
                    ClickPositionVO clickPositionVO = tableViewItems.get(i);
                    String err = text_checkIndex() + clickPositionVO.getIndex() + text_taskErr();
                    if (StringUtils.isNotBlank(clickPositionVO.getClickImgPath())) {
                        FloatingWindowConfig clickWindowConfig = clickPositionVO.getClickWindowConfig();
                        if (clickWindowConfig != null && clickWindowConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                            WindowInfo clickInfo = clickWindowConfig.getWindowInfo();
                            if (clickInfo == null || StringUtils.isBlank(clickInfo.getProcessPath())) {
                                errs.add(err + text_noClickWindowInfo());
                            } else {
                                processPaths.add(clickInfo.getProcessPath());
                            }
                        }
                    }
                    if (CollectionUtils.isNotEmpty(clickPositionVO.getStopImgFiles())) {
                        FloatingWindowConfig stopWindowConfig = clickPositionVO.getStopWindowConfig();
                        if (stopWindowConfig != null && stopWindowConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                            WindowInfo stopInfo = stopWindowConfig.getWindowInfo();
                            if (stopInfo == null || StringUtils.isBlank(stopInfo.getProcessPath())) {
                                errs.add(err + text_noStopWindowInfo());
                            } else {
                                processPaths.add(stopInfo.getProcessPath());
                            }
                        }
                    }
                }
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
                updateMassage(text_updatingWindowInfo());
                updateProgress(0, tableSize);
                for (int i = 0; i < tableSize; i++) {
                    updateProgress(i + 1, tableSize);
                    ClickPositionVO clickPositionVO = tableViewItems.get(i);
                    String err = text_checkIndex() + clickPositionVO.getIndex() + text_taskErr();
                    if (StringUtils.isNotBlank(clickPositionVO.getClickImgPath())) {
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
                    }
                    if (CollectionUtils.isNotEmpty(clickPositionVO.getStopImgFiles())) {
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
                floatingLabel = taskBean.getFloatingLabel();
                massageLabel = taskBean.getMassageLabel();
                firstClick.set(taskBean.isFirstClick());
                updateProgress(0, dataSize);
                int currentStep = 0;
                while (currentStep < dataSize) {
                    int progress = currentStep + 1;
                    updateProgress(progress, dataSize);
                    ClickPositionVO clickPositionVO = backup.get(currentStep);
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
                        clickText = text_taskInfo() + clickKey + clickType;
                    }
                    Platform.runLater(() -> {
                        String text = loopTimeText +
                                text_progress() + progress + "/" + dataSize +
                                text_willBe() + waitTime + text_msWillBe() + name +
                                text_point() + " X：" + startX + " Y：" + startY + clickText +
                                text_clickTime() + clickTime + " " + unit_ms() +
                                text_repeat() + clickNum + text_interval() + interval + " " + unit_ms();
                        if (StringUtils.isNotBlank(clickImgPath)) {
                            text = loopTimeText +
                                    text_progress() + progress + "/" + dataSize +
                                    text_willBe() + waitTime + text_msWillBe() + name +
                                    text_picTarget() + "\n" + getExistsFileName(new File(clickImgPath)) +
                                    text_afterMatch() + matchType;
                        }
                        // 更新操作信息
                        updateMassage(text);
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
        };
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
                Platform.runLater(() -> {
                    String text = loopTimeText + text_searchingStop() + fileName.get();
                    // 更新操作信息
                    updateMassage(text);
                });
                long start = System.currentTimeMillis();
                double stopMatchThreshold = Double.parseDouble(clickPositionVO.getStopMatchThreshold());
                FloatingWindowConfig stopWindowConfig = clickPositionVO.getStopWindowConfig();
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
        String clickType = clickPositionVO.getClickType();
        AtomicReference<String> fileName = new AtomicReference<>();
        if (StringUtils.isNotBlank(clickPath)) {
            fileName.set(getExistsFileName(new File(clickPath)));
            Platform.runLater(() -> {
                String text = loopTimeText + text_searchingClick() + fileName.get();
                // 更新操作信息
                updateMassage(text);
            });
            long start = System.currentTimeMillis();
            String retryType = clickPositionVO.getRetryType();
            double clickMatchThreshold = Double.parseDouble(clickPositionVO.getClickMatchThreshold());
            FloatingWindowConfig clickWindowConfig = clickPositionVO.getClickWindowConfig();
            FindPositionConfig findPositionConfig = new FindPositionConfig()
                    .setMaxRetry(Integer.parseInt(clickPositionVO.getClickRetryTimes()))
                    .setContinuously(retryType_continuously().equals(retryType))
                    .setFloatingWindowConfig(clickWindowConfig)
                    .setMatchThreshold(clickMatchThreshold)
                    .setRetryWait(retrySecondValue)
                    .setOverTime(overTimeValue)
                    .setTemplatePath(clickPath)
                    .setName(name);
            MatchPointBean matchPointBean = findPosition(findPositionConfig, dynamicQueue);
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
                            .setResult(matchThreshold + percentage)
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
     * 更新操作信息
     *
     * @param message 要更新的信息
     */
    private static void updateMassage(String message) {
        if (massageLabel != null) {
            massageLabel.setText(message);
        }
        if (floatingLabel != null) {
            floatingLabel.setText(message);
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
                clickPositionVOS.add(vo);
            }
        }
        return clickPositionVOS;
    }

}
