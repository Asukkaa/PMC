package priv.koishi.pmc.Service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Bean.Config.FileConfig;
import priv.koishi.pmc.Bean.DTO.PMCFileDTO;
import priv.koishi.pmc.Bean.DTO.PMCSFileDTO;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.PMCListBean;
import priv.koishi.pmc.Bean.Result.PMCLoadResult;
import priv.koishi.pmc.Bean.Result.PMCSLoadResult;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Service.TaskInterface.MessageUpdater;
import priv.koishi.pmc.Service.TaskInterface.ProgressUpdater;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.runPMCFile;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.recordClickTypeMap;
import static priv.koishi.pmc.Utils.CommonUtils.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.UiUtils.creatConfirmDialog;

/**
 * PMC 文件加载与导出任务类
 *
 * @author KOISHI
 * Date:2025-11-05
 * Time:14:40
 */
public class PMCFileService {

    /**
     * 批量加载 PMC 文件（批量运行多个文件）
     *
     * @param taskBean 线程任务参数
     * @param files    文件列表
     * @return PMC 文件列表
     */
    public static Task<PMCSLoadResult> loadPMCSFils(TaskBean<PMCListBean> taskBean, List<? extends File> files) {
        return new Task<>() {
            @Override
            protected PMCSLoadResult call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                List<PMCListBean> pmcListBeans = new ArrayList<>();
                Map<String, String> imgMap = new HashMap<>();
                String[] lastPMCPathRef = {""};
                // 读取文件
                List<ClickPositionVO> allClickPositions = loadFilesCommon(
                        files,
                        imgMap,
                        lastPMCPathRef,
                        (pmcFile, clickPositionVOS) -> {
                            PMCListBean pmcListBean = new PMCListBean()
                                    .setClickPositionVOS(clickPositionVOS)
                                    .setName(pmcFile.getName())
                                    .setPath(pmcFile.getPath());
                            pmcListBeans.add(pmcListBean);
                        },
                        (_, pmcsList) -> pmcListBeans.addAll(pmcsList),
                        this::updateProgress);
                // 匹配图片
                matchSameNameImg(imgMap, allClickPositions, this::updateMessage, this::updateProgress);
                return new PMCSLoadResult(pmcListBeans, lastPMCPathRef[0]);
            }
        };
    }

    /**
     * 批量加载 PMC 文件（合并多个文件到一个流程）
     *
     * @param taskBean 线程任务参数
     * @param files    文件列表
     * @return PMC 操作列表
     */
    public static Task<PMCLoadResult> loadPMCFils(TaskBean<ClickPositionVO> taskBean, List<? extends File> files) {
        return new Task<>() {
            @Override
            protected PMCLoadResult call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                List<ClickPositionVO> allClickPositions = new ArrayList<>();
                Map<String, String> imgMap = new HashMap<>();
                String[] lastPMCPathRef = {""};
                // 读取文件
                loadFilesCommon(
                        files,
                        imgMap,
                        lastPMCPathRef,
                        (_, clickPositionVOS) ->
                                allClickPositions.addAll(clickPositionVOS),
                        null,
                        this::updateProgress);
                // 匹配图片
                matchSameNameImg(imgMap, allClickPositions, this::updateMessage, this::updateProgress);
                taskBean.getTableView().refresh();
                return new PMCLoadResult(allClickPositions, lastPMCPathRef[0]);
            }
        };
    }

    /**
     * 通用 PMC 文件读取逻辑
     *
     * @param files              文件列表
     * @param imgMap             图片映射表（图片路径与名称映射）
     * @param lastPMCPathRef     最后一个 PMC 文件路径的引用
     * @param pmcFileProcessor   PMC 文件处理器回调
     * @param updateProgressFunc 进度更新函数
     * @return 所有读取到的点击位置列表
     */
    private static List<ClickPositionVO> loadFilesCommon(List<? extends File> files,
                                                         Map<? super String, ? super String> imgMap, String[] lastPMCPathRef,
                                                         BiConsumer<? super File, ? super List<ClickPositionVO>> pmcFileProcessor,
                                                         BiConsumer<? super File, ? super List<PMCListBean>> pmcsFileProcessor,
                                                         ProgressUpdater updateProgressFunc) {
        List<ClickPositionVO> allClickPositions = new ArrayList<>();
        int size = files.size();
        updateProgressFunc.update(0, size);
        for (int i = 0; i < size; i++) {
            File file = files.get(i);
            String fileType = getExistsFileType(file);
            if (PMC.equals(fileType)) {
                lastPMCPathRef[0] = file.getPath();
                List<ClickPositionVO> clickPositionVOS = loadPMCFile(file);
                if (CollectionUtils.isNotEmpty(clickPositionVOS)) {
                    allClickPositions.addAll(clickPositionVOS);
                }
                // 调用回调处理 PMC 文件
                if (pmcFileProcessor != null) {
                    pmcFileProcessor.accept(file, clickPositionVOS);
                }
            } else if (PMCS.equals(fileType)) {
                if (pmcsFileProcessor != null) {
                    List<PMCListBean> pmcsList = loadPMCSFile(file);
                    pmcsFileProcessor.accept(file, pmcsList);
                }
            } else if (file.isDirectory()) {
                processDirectory(file, imgMap, lastPMCPathRef, allClickPositions, pmcFileProcessor);
            } else if (imageType.contains(fileType)) {
                imgMap.put(file.getPath(), getExistsFileName(file));
            }
            updateProgressFunc.update(i + 1, size);
        }
        return allClickPositions;
    }

    /**
     * 处理目录
     *
     * @param directory         要处理的目录
     * @param imgMap            图片映射表（图片路径与名称映射）
     * @param allClickPositions 完整的操作流程列表
     * @param pmcFileProcessor  PMC 文件处理器回调
     */
    private static void processDirectory(File directory, Map<? super String, ? super String> imgMap,
                                         String[] lastPMCPathRef, List<? super ClickPositionVO> allClickPositions,
                                         BiConsumer<? super File, ? super List<ClickPositionVO>> pmcFileProcessor) {
        List<String> filterExtensionList = new ArrayList<>(imageType);
        filterExtensionList.add(PMC);
        FileConfig fileConfig = new FileConfig()
                .setFilterExtensionList(filterExtensionList)
                .setShowDirectory(search_fileDirectory())
                .setShowHideFile(hide_noHideFile())
                .setPath(directory.getPath())
                .setRecursion(true);
        for (File readFile : readAllFiles(fileConfig)) {
            String fileType = getExistsFileType(readFile);
            if (PMC.equals(fileType)) {
                lastPMCPathRef[0] = readFile.getPath();
                List<ClickPositionVO> clickPositionVOS = loadPMCFile(readFile);
                if (CollectionUtils.isNotEmpty(clickPositionVOS)) {
                    allClickPositions.addAll(clickPositionVOS);
                }
                // 调用回调处理 PMC 文件
                if (pmcFileProcessor != null) {
                    pmcFileProcessor.accept(readFile, clickPositionVOS);
                }
            } else {
                imgMap.put(readFile.getPath(), getExistsFileName(readFile));
            }
        }
    }

    /**
     * 匹配图片
     *
     * @param imgMap             文件夹中用于匹配的图片
     * @param clickPositionBeans 需要匹配的自动操作步骤
     * @param updateMessageFunc  消息更新函数
     * @param updateProgressFunc 进度更新函数
     */
    private static void matchSameNameImg(Map<String, String> imgMap,
                                         List<? extends ClickPositionVO> clickPositionBeans,
                                         MessageUpdater updateMessageFunc, ProgressUpdater updateProgressFunc) {
        if (!imgMap.isEmpty()) {
            updateMessageFunc.update(text_matchImg());
            int clickPositionBeansSize = clickPositionBeans.size();
            updateProgressFunc.update(0, clickPositionBeansSize);
            for (int j = 0; j < clickPositionBeansSize; j++) {
                ClickPositionVO clickPositionVO = clickPositionBeans.get(j);
                String clickImgPath = clickPositionVO.getClickImgPath();
                if (StringUtils.isNotBlank(clickImgPath)) {
                    File file = new File(clickImgPath);
                    if (!file.exists()) {
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
                updateProgressFunc.update(j + 1, clickPositionBeansSize);
            }
        }
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
    public static Task<List<ClickPositionVO>> buildPMC(TaskBean<ClickPositionVO> taskBean, File file) {
        return new Task<>() {
            @Override
            protected List<ClickPositionVO> call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                return loadPMCFile(file);
            }
        };
    }

    /**
     * 导出 PMCS 文件
     *
     * @param taskBean    线程任务参数
     * @param fileName    要导出的文件名
     * @param outFilePath 导出文件夹路径
     * @return 导出文件路径
     */
    public static Task<String> exportPMCS(TaskBean<PMCListBean> taskBean, String fileName,
                                          String outFilePath, boolean notOverwrite) {
        return new Task<>() {
            @Override
            protected String call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_exportData());
                List<PMCListBean> tableViewItems = taskBean.getBeanList();
                String path = outFilePath + File.separator + fileName + PMCS;
                if (notOverwrite) {
                    path = notOverwritePath(path);
                }
                List<PMCListBean> exportList = tableViewItems.stream()
                        .map(vo -> {
                            PMCListBean bean = new PMCListBean();
                            try {
                                // 复制所有属性
                                copyAllProperties(vo, bean);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            return bean;
                        }).toList();
                // 构建导出文件基本信息
                PMCSFileDTO pmcsFileDTO = new PMCSFileDTO()
                        .setPmcsList(exportList);
                // 序列化数据
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(new File(path), pmcsFileDTO);
                updateMessage(text_saveSuccess() + path);
                return path;
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
    public static Task<String> exportPMC(TaskBean<ClickPositionVO> taskBean, String fileName,
                                         String outFilePath, boolean notOverwrite) {
        return new Task<>() {
            @Override
            protected String call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_exportData());
                List<ClickPositionVO> tableViewItems = taskBean.getBeanList();
                String path = outFilePath + File.separator + fileName + PMC;
                if (notOverwrite) {
                    path = notOverwritePath(path);
                }
                List<ClickPositionBean> exportList = tableViewItems.stream()
                        .map(vo -> {
                            ClickPositionBean bean = new ClickPositionBean();
                            try {
                                // 复制所有属性
                                copyAllProperties(vo, bean);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            return bean;
                        }).toList();
                // 构建导出文件基本信息
                PMCFileDTO pmcFileDTO = new PMCFileDTO()
                        .setPmcList(exportList);
                // 序列化数据
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(new File(path), pmcFileDTO);
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
                Platform.runLater(() -> {
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
                });
                return null;
            }
        };
    }

    /**
     * 导入自动操作流程文件
     *
     * @param jsonFile 要解析的文件
     */
    public static List<ClickPositionVO> loadPMCFile(File jsonFile) {
        // 配置忽略未知字段
        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        String filePath = jsonFile.getAbsolutePath();
        List<ClickPositionBean> clickPositionBeans;
        boolean isErrFormat = false;
        String errText = text_fileName() + getFileName(filePath) + "\n" +
                text_filePath() + filePath + "\n";
        try {
            // 首先尝试解析为 PMCFileBean 格式
            PMCFileDTO pmcFileDTO = objectMapper.readValue(jsonFile, PMCFileDTO.class);
            if (pmcFileDTO != null && CollectionUtils.isNotEmpty(pmcFileDTO.getPmcList())) {
                String version = pmcFileDTO.getVersion();
                String app = pmcFileDTO.getAppName();
                if (!appName.equals(app)) {
                    isErrFormat = true;
                    errText += confirm_otherPMCConfirm();
                } else if (compareToConstant(version, PMCFileVersion) > 0) {
                    isErrFormat = true;
                    errText += confirm_isNewPMCConfirm();
                } else if (compareToConstant(version, PMCFileVersion) < 0) {
                    isErrFormat = true;
                    errText += confirm_isOldPMCConfirm();
                }
                clickPositionBeans = new ArrayList<>(pmcFileDTO.getPmcList());
            } else {
                // 如果 pmcList 为空，尝试旧格式
                clickPositionBeans = objectMapper.readValue(jsonFile,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
                isErrFormat = true;
                errText += confirm_isOldPMCConfirm();
            }
        } catch (MismatchedInputException e) {
            // PMCFileBean 解析失败，尝试旧格式
            try {
                clickPositionBeans = objectMapper.readValue(jsonFile,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
                isErrFormat = true;
                errText += confirm_isOldPMCConfirm();
            } catch (MismatchedInputException e2) {
                // 两种格式都失败，抛出异常
                throw new RuntimeException(text_loadAutoClick() + filePath + text_formatError());
            }
        }
        // 自动任务加载时不提示旧版本
        if (runPMCFile) {
            isErrFormat = false;
        }
        List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
        if (isErrFormat) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<List<ClickPositionVO>> resultRef = new AtomicReference<>();
            AtomicReference<Exception> exceptionRef = new AtomicReference<>();
            String finalErrText = errText;
            List<ClickPositionBean> finalClickPositionBeans = clickPositionBeans;
            Platform.runLater(() -> {
                try {
                    ButtonType result = creatConfirmDialog(
                            text_errPMCFile(),
                            finalErrText,
                            confirm_continue(),
                            confirm_cancel());
                    ButtonBar.ButtonData buttonData = result.getButtonData();
                    if (!buttonData.isCancelButton()) {
                        buildPMC(finalClickPositionBeans, clickPositionVOS);
                        resultRef.set(clickPositionVOS);
                    } else {
                        resultRef.set(new ArrayList<>());
                    }
                } catch (Exception e) {
                    exceptionRef.set(e);
                } finally {
                    latch.countDown();
                }
            });
            try {
                // 等待用户选择完成
                latch.await();
                Exception e = exceptionRef.get();
                if (e != null) {
                    throw new RuntimeException(e);
                }
                return resultRef.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        } else {
            buildPMC(clickPositionBeans, clickPositionVOS);
        }
        return clickPositionVOS;
    }

    /**
     * 构建 PMC 自动操作流程
     *
     * @param input  导入的数据
     * @param output 处理后的数据
     */
    private static void buildPMC(List<? extends ClickPositionBean> input, List<? super ClickPositionVO> output) {
        if (CollectionUtils.isNotEmpty(input)) {
            // 定时执行导入自动操作并执行时如果不立刻设置序号会导致运行时找不到序号
            int index = 0;
            for (ClickPositionBean bean : input) {
                if (!retryTypeMap.containsKey(bean.getRetryTypeEnum())
                        || !clickTypeMap.containsKey(bean.getClickTypeEnum())
                        || !matchedTypeMap.containsKey(bean.getMatchedTypeEnum())
                        || !recordClickTypeMap.containsKey(bean.getMouseKeyEnum())
                        || !activationList.contains(bean.getRandomClick())
                        || !activationList.contains(bean.getRandomWaitTime())
                        || !activationList.contains(bean.getRandomClickTime())
                        || !activationList.contains(bean.getRandomTrajectory())
                        || !activationList.contains(bean.getRandomClickInterval())
                        || !isInIntegerRange(bean.getStartX(), 0, null)
                        || !isInIntegerRange(bean.getStartY(), 0, null)
                        || !isInIntegerRange(bean.getRandomX(), 0, null)
                        || !isInIntegerRange(bean.getRandomY(), 0, null)
                        || !isInIntegerRange(bean.getImgX(), null, null)
                        || !isInIntegerRange(bean.getImgY(), null, null)
                        || !isInIntegerRange(bean.getWaitTime(), 0, null)
                        || !isInIntegerRange(bean.getClickNum(), 0, null)
                        || !isInIntegerRange(bean.getClickTime(), 0, null)
                        || !isInIntegerRange(bean.getClickInterval(), 0, null)
                        || !isInIntegerRange(bean.getStopRetryTimes(), 0, null)
                        || !isInIntegerRange(bean.getClickRetryTimes(), 0, null)
                        || !isInIntegerRange(bean.getRandomClickTime(), 0, null)
                        || !isInIntegerRange(bean.getStopMatchThreshold(), 0, 100)
                        || !isInIntegerRange(bean.getClickMatchThreshold(), 0, 100)) {
                    throw new RuntimeException(text_missingKeyData());
                }
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
                        .updateUuid();
                vo.setIndex(index++);
                output.add(vo);
            }
        }
    }

    /**
     * 加载 PMCS 文件
     *
     * @param jsonFile 要解析的 PMCS 文件
     * @return PMCListBean 列表
     */
    private static List<PMCListBean> loadPMCSFile(File jsonFile) {
        // 配置忽略未知字段
        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        String filePath = jsonFile.getAbsolutePath();
        List<PMCListBean> pmcsList;
        boolean isErrFormat = false;
        String errText = text_fileName() + getFileName(filePath) + "\n" +
                text_filePath() + filePath + "\n";
        try {
            // 首先尝试解析为 PMCFileBean 格式
            PMCSFileDTO pmcsFileDTO = objectMapper.readValue(jsonFile, PMCSFileDTO.class);
            if (pmcsFileDTO != null && CollectionUtils.isNotEmpty(pmcsFileDTO.getPmcsList())) {
                String version = pmcsFileDTO.getVersion();
                String app = pmcsFileDTO.getAppName();
                if (!appName.equals(app)) {
                    isErrFormat = true;
                    errText += confirm_otherPMCSConfirm();
                } else if (compareToConstant(version, PMCSFileVersion) > 0) {
                    isErrFormat = true;
                    errText += confirm_isNewPMCSConfirm();
                } else if (compareToConstant(version, PMCSFileVersion) < 0) {
                    isErrFormat = true;
                    errText += confirm_isOldPMCSConfirm();
                }
                pmcsList = new ArrayList<>(pmcsFileDTO.getPmcsList());
            } else {
                // 如果 pmcList 为空，尝试旧格式
                pmcsList = objectMapper.readValue(jsonFile,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, PMCListBean.class));
                isErrFormat = true;
                errText += confirm_isOldPMCSConfirm();
            }
        } catch (MismatchedInputException e) {
            // PMCFileBean 解析失败，尝试旧格式
            try {
                pmcsList = objectMapper.readValue(jsonFile,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, PMCListBean.class));
                isErrFormat = true;
                errText += confirm_isOldPMCSConfirm();
            } catch (MismatchedInputException e2) {
                // 两种格式都失败，抛出异常
                throw new RuntimeException(text_loadAutoClick() + filePath + text_formatError());
            }
        }
        // 自动任务加载时不提示旧版本
        if (runPMCFile) {
            isErrFormat = false;
        }
        List<PMCListBean> pmcListBeans = new ArrayList<>();
        if (isErrFormat) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<List<PMCListBean>> resultRef = new AtomicReference<>();
            AtomicReference<Exception> exceptionRef = new AtomicReference<>();
            String finalErrText = errText;
            List<PMCListBean> finalPmcsList = pmcsList;
            Platform.runLater(() -> {
                try {
                    ButtonType result = creatConfirmDialog(
                            text_errPMCSFile(),
                            finalErrText,
                            confirm_continue(),
                            confirm_cancel());
                    ButtonBar.ButtonData buttonData = result.getButtonData();
                    if (!buttonData.isCancelButton()) {
                        buildPMCS(finalPmcsList, pmcListBeans);
                        resultRef.set(pmcListBeans);
                    } else {
                        resultRef.set(new ArrayList<>());
                    }
                } catch (Exception e) {
                    exceptionRef.set(e);
                } finally {
                    latch.countDown();
                }
            });
            try {
                // 等待用户选择完成
                latch.await();
                Exception e = exceptionRef.get();
                if (e != null) {
                    throw new RuntimeException(e);
                }
                return resultRef.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        } else {
            buildPMCS(pmcsList, pmcListBeans);
        }
        return pmcListBeans;
    }

    /**
     * 构建 PMCS 自动操作流程集合
     *
     * @param input  导入的数据
     * @param output 处理后的数据
     */
    private static void buildPMCS(List<? extends PMCListBean> input, List<? super PMCListBean> output) {
        if (CollectionUtils.isNotEmpty(input)) {
            int index = 0;
            for (PMCListBean bean : input) {
                File file = new File(bean.getPath());
                if (!file.exists()
                        || !isInIntegerRange(bean.getRunNum(), 1, null)
                        || !isInIntegerRange(bean.getWaitTime(), 0, null)) {
                    throw new RuntimeException(text_missingKeyData());
                }
                List<ClickPositionVO> clickPositionVOS = loadPMCFile(file);
                PMCListBean pmcListBean = new PMCListBean();
                try {
                    // 自动拷贝父类中的属性
                    copyAllProperties(bean, pmcListBean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                pmcListBean.setClickPositionVOS(clickPositionVOS)
                        .setIndex(index++);
                output.add(pmcListBean);
            }
        }
    }

}
