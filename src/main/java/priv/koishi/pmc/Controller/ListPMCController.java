package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FileChooserConfig;
import priv.koishi.pmc.Bean.PMCListBean;
import priv.koishi.pmc.Bean.PMCLogBean;
import priv.koishi.pmc.Bean.Result.PMCSLoadResult;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Event.AutoClickLoadedEvent;
import priv.koishi.pmc.Event.EventBus;
import priv.koishi.pmc.UI.CustomEditingCell.EditingCell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import static priv.koishi.pmc.Controller.AutoClickController.isNativeHookException;
import static priv.koishi.pmc.Controller.AutoClickController.noScreenCapturePermission;
import static priv.koishi.pmc.Controller.FileChooserController.chooserFiles;
import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.*;
import static priv.koishi.pmc.Service.PMCFileService.*;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.cancelKey;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ListenerUtils.integerRangeTextField;
import static priv.koishi.pmc.Utils.ListenerUtils.textFieldValueListener;
import static priv.koishi.pmc.Utils.NodeDisableUtils.setNodeDisable;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.ToolTipUtils.creatTooltip;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 批量执行 PMC 文件页面控制器
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:21
 */
public class ListPMCController extends RootController {

    /**
     * 导入文件路径
     */
    private String inFilePath;

    /**
     * 操作记录
     */
    public List<PMCLogBean> clickLogs = new CopyOnWriteArrayList<>();

    /**
     * 记录页高度
     */
    private int logHeight;

    /**
     * 记录页宽度
     */
    private int logWidth;

    /**
     * 要防重复点击的组件
     */
    public final List<Node> disableNodes = new ArrayList<>();

    /**
     * 批量导入 PMC 文件任务
     */
    public Task<PMCSLoadResult> loadPMCFilsTask;

    /**
     * 人口参数导入 PMCS 文件任务
     */
    public Task<List<PMCListBean>> loadedPMCSTask;

    /**
     * 导出 PMC 文件任务
     */
    public Task<String> exportPMCTask;

    /**
     * 页面标识符
     */
    private final String tabId = "_List";

    @FXML
    public AnchorPane anchorPane_List;

    @FXML
    public HBox logHBox_List;

    @FXML
    public ProgressBar progressBar_List;

    @FXML
    public Label dataNumber_List, log_List, tip_List, cancelTip_List, outPath_List, err_List;

    @FXML
    public CheckBox openDirectory_List, notOverwrite_List, loadFolder_List;

    @FXML
    public Button clearButton_List, runClick_List, addPosition_List, loadAutoClick_List, exportAutoClick_List,
            addOutPath_List, clickLog_List;

    @FXML
    public TextField loopTime_List, outFileName_List, preparationRunTime_List;

    @FXML
    public TableView<PMCListBean> tableView_List;

    @FXML
    public TableColumn<PMCListBean, Integer> index_List;

    @FXML
    public TableColumn<PMCListBean, String> name_List, runNum_List, waitTime_List, path_List;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_List.setPrefHeight(stageHeight * 0.5);
        // 设置组件宽度
        double tableWidth = mainStage.getWidth() * 0.95;
        tableView_List.setMaxWidth(tableWidth);
        tableView_List.setPrefWidth(tableWidth);
        bindPrefWidthProperty();
    }

    /**
     * 设置 JavaFX 单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_List.prefWidthProperty().bind(tableView_List.widthProperty().multiply(0.1));
        name_List.prefWidthProperty().bind(tableView_List.widthProperty().multiply(0.2));
        path_List.prefWidthProperty().bind(tableView_List.widthProperty().multiply(0.4));
        runNum_List.prefWidthProperty().bind(tableView_List.widthProperty().multiply(0.1));
        waitTime_List.prefWidthProperty().bind(tableView_List.widthProperty().multiply(0.2));
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException 配置文件保存异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_List != null) {
            InputStream input = checkRunningInputStream(configFile_List);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_lastLoopTime, loopTime_List.getText());
            prop.put(key_lastOutFileName, outFileName_List.getText());
            String lastOpenDirectoryValue = openDirectory_List.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, lastOpenDirectoryValue);
            String lastNotOverwriteValue = notOverwrite_List.isSelected() ? activation : unActivation;
            prop.put(key_lastNotOverwrite, lastNotOverwriteValue);
            String loadFolderValue = loadFolder_List.isSelected() ? activation : unActivation;
            prop.put(key_loadFolder, loadFolderValue);
            prop.put(key_lastPreparationRunTime, preparationRunTime_List.getText());
            String outPathValue = outPath_List.getText();
            prop.put(key_outFilePath, outPathValue);
            OutputStream output = checkRunningOutputStream(configFile_List);
            prop.store(output, null);
            input.close();
            output.close();
            CheckBox autoSave = settingController.autoSavePMCS_Set;
            // 自动保存
            autoSave(autoSave, outPathValue);
        }
    }

    /**
     * 自动保存操作流程
     *
     * @param autoSave 自动保存开关
     */
    private void autoSave(CheckBox autoSave, String outPath) {
        if (autoSave.isSelected()) {
            List<PMCListBean> tableViewItems = new ArrayList<>(tableView_List.getItems());
            if (CollectionUtils.isNotEmpty(tableViewItems)) {
                TaskBean<PMCListBean> taskBean = creatTaskBean();
                taskBean.setMessageLabel(log_List)
                        .setBeanList(tableViewItems);
                exportPMCTask = exportPMCS(taskBean, autoSavePMCSFileName(), outPath, notOverwrite_List.isSelected());
                bindingTaskNode(exportPMCTask, taskBean);
                exportPMCTask.setOnSucceeded(_ -> {
                    taskUnbind(taskBean);
                    log_List.setTextFill(Color.GREEN);
                    exportPMCTask = null;
                });
                exportPMCTask.setOnFailed(event -> {
                    exportPMCTask = null;
                    taskNotSuccess(taskBean, text_taskFailed());
                    throw new RuntimeException(event.getSource().getException());
                });
                Thread.ofVirtual()
                        .name("exportPMCSTask-vThread" + tabId)
                        .start(exportPMCTask);
            }
        }
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException 配置文件保存异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_List);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig, activation))) {
            setControlLastConfig(outPath_List, prop, key_outFilePath);
            setControlLastConfig(loadFolder_List, prop, key_loadFolder, unActivation);
            setControlLastConfig(loopTime_List, prop, key_lastLoopTime, defaultLoopTime);
            setControlLastConfig(notOverwrite_List, prop, key_lastNotOverwrite, activation);
            setControlLastConfig(openDirectory_List, prop, key_lastOpenDirectory, activation);
            setControlLastConfig(outFileName_List, prop, key_lastOutFileName, defaultPMCSFileName());
            setControlLastConfig(preparationRunTime_List, prop, key_lastPreparationRunTime, defaultPreparationRun);
        }
        if (StringUtils.isBlank(outPath_List.getText())) {
            setPathLabel(outPath_List, defaultFileChooserPath);
        }
        input.close();
    }

    /**
     * 读取配置文件
     *
     * @throws IOException 配置文件读取异常
     */
    private void getProperties() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_List);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        logWidth = Integer.parseInt(prop.getProperty(key_logWidth, defaultLogWidth));
        logHeight = Integer.parseInt(prop.getProperty(key_logHeight, defaultLogHeight));
        input.close();
    }

    /**
     * 设置单元格可编辑
     */
    private void makeCellCanEdit() {
        // 设置表格可编辑
        tableView_List.setEditable(true);
        // 设置等待时间可编辑
        waitTime_List.setCellFactory((_) ->
                new EditingCell<>(PMCListBean::setWaitTime, true, 0, null));
        // 设置执行次数可编辑
        runNum_List.setCellFactory((_) ->
                new EditingCell<>(PMCListBean::setRunNum, true, 1, null));
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 添加列表右键菜单
        ContextMenu tableMenu = new ContextMenu();
        // 查看详情选项
        buildDetailMenuItem(tableMenu);
        // 将所选项全部添加到操作列表
        buildAddAllMenuItem(tableMenu);
        // 更换所选第一行文件地址选项
        buildSetPathMenuItem(tableMenu);
        // 移动数据选项
        buildMoveDataMenu(tableView_List, tableMenu);
        // 查看文件选项
        buildFilePathItem(tableView_List, tableMenu);
        // 复制数据选项
        buildCopyDataMenu(tableView_List, tableMenu, dataNumber_List, unit_files());
        // 取消选中选项
        buildClearSelectedData(tableView_List, tableMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_List, dataNumber_List, tableMenu, unit_files());
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(tableMenu, tableView_List);
    }

    /**
     * 查看所选项第一行详情选项
     *
     * @param contextMenu 右键菜单集合
     */
    private void buildDetailMenuItem(ContextMenu contextMenu) {
        MenuItem detailItem = new MenuItem(menu_detailMenu());
        detailItem.setOnAction(_ -> {
            updateLabel(log_List, "");
            PMCListBean selected = tableView_List.getSelectionModel().getSelectedItems().getFirst();
            if (selected != null) {
                String path = selected.getPath();
                if (!new File(path).exists()) {
                    String text = text_noExistsPMC(selected.getName()) + "\n" + text_filePath() + path;
                    ButtonType result = creatConfirmDialog(
                            text_fileNotExists(),
                            text,
                            confirm_continue(),
                            confirm_cancel());
                    ButtonBar.ButtonData buttonData = result.getButtonData();
                    if (buttonData.isCancelButton()) {
                        return;
                    }
                }
                List<ClickPositionVO> clickPositionVOS = selected.getClickPositionVOS();
                if (CollectionUtils.isNotEmpty(clickPositionVOS)) {
                    ObservableList<ClickPositionVO> items = autoClickController.tableView_Click.getItems();
                    if (CollectionUtils.isNotEmpty(items)) {
                        ButtonType result = creatConfirmDialog(
                                text_listNotNull(),
                                text_isClearList(),
                                confirm_continue(),
                                confirm_cancel());
                        ButtonBar.ButtonData buttonData = result.getButtonData();
                        if (!buttonData.isCancelButton()) {
                            items.clear();
                        }
                    }
                    mainController.tabPane.getSelectionModel().select(mainController.autoClickTab);
                    TaskBean<ClickPositionVO> taskBean = autoClickController.creatTaskBean();
                    Task<List<ClickPositionVO>> copyPMCTask = copyPMC(clickPositionVOS, taskBean);
                    bindingTaskNode(copyPMCTask, taskBean);
                    copyPMCTask.setOnSucceeded(_ -> {
                        taskUnbind(taskBean);
                        List<ClickPositionVO> copy = copyPMCTask.getValue();
                        autoClickController.addAutoClickPositions(copy, path);
                        autoClickController.outFileName_Click.setText(getFileName(path));
                    });
                    Thread.ofVirtual()
                            .name("copyPMCSTask-vThread" + tabId)
                            .start(copyPMCTask);
                }
            }
        });
        contextMenu.getItems().add(detailItem);
    }

    /**
     * 将所选项全部添加到操作列表选项
     *
     * @param contextMenu 右键菜单集合
     */
    private void buildAddAllMenuItem(ContextMenu contextMenu) {
        MenuItem addAllItem = new MenuItem(menu_addAllMenu());
        addAllItem.setOnAction(_ -> {
            updateLabel(log_List, "");
            List<PMCListBean> selected = tableView_List.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selected)) {
                selected.forEach(pmcListBean -> {
                    List<ClickPositionVO> clickPositionVOS = pmcListBean.getClickPositionVOS();
                    if (CollectionUtils.isNotEmpty(clickPositionVOS)) {
                        autoClickController.addAutoClickPositions(clickPositionVOS, pmcListBean.getPath());
                    }
                });
                mainController.tabPane.getSelectionModel().select(mainController.autoClickTab);
            }
        });
        contextMenu.getItems().add(addAllItem);
    }

    /**
     * 更换所选第一行文件地址选项
     *
     * @param contextMenu 右键菜单集合
     */
    private void buildSetPathMenuItem(ContextMenu contextMenu) {
        MenuItem detailItem = new MenuItem(menu_setPathMenu());
        detailItem.setOnAction(_ -> {
            updateLabel(log_List, "");
            PMCListBean selected = tableView_List.getSelectionModel().getSelectedItems().getFirst();
            if (selected != null) {
                Window window = tableView_List.getScene().getWindow();
                FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(appName, allPMC);
                List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(filter));
                AutoClickController.isSonOpening = true;
                File file = creatFileChooser(window, inFilePath, extensionFilters, text_selectAutoFile());
                if (file != null) {
                    TaskBean<PMCListBean> taskBean = creatTaskBean();
                    loadPMCFilsTask = loadPMCSFils(taskBean, Collections.singletonList(file));
                    bindingTaskNode(loadPMCFilsTask, taskBean);
                    loadPMCFilsTask.setOnSucceeded(_ -> {
                        taskUnbind(taskBean);
                        PMCSLoadResult value = loadPMCFilsTask.getValue();
                        List<PMCListBean> clickPositionVOS = value.pmcListBeans();
                        List<ClickPositionVO> vos = clickPositionVOS.getFirst().getClickPositionVOS();
                        selected.setPath(file.getAbsolutePath())
                                .setClickPositionVOS(vos)
                                .setName(file.getName());
                        updateTableViewSizeText(tableView_List, dataNumber_List, unit_files());
                        tableView_List.refresh();
                        loadPMCFilsTask = null;
                    });
                    loadPMCFilsTask.setOnFailed(event -> {
                        taskUnbind(taskBean);
                        taskNotSuccess(taskBean, text_taskFailed());
                        loadPMCFilsTask = null;
                        throw new RuntimeException(event.getSource().getException());
                    });
                    Thread.ofVirtual()
                            .name("loadPMCSFilsTask-vThread" + tabId)
                            .start(loadPMCFilsTask);
                }
                AutoClickController.isSonOpening = false;
            }
        });
        contextMenu.getItems().add(detailItem);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 导出自动流程文件名称文本输入框鼠标悬停提示
        textFieldValueListener(outFileName_List, tip_autoClickFileName() + defaultPMCSFileName());
        // 限制循环次数文本输入框内容
        integerRangeTextField(loopTime_List, 0, null, tip_loopTime());
        // 限制运行准备时间文本输入框内容
        integerRangeTextField(preparationRunTime_List, 0, null, tip_preparationRunTime() + defaultPreparationRun);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_clickLog(), clickLog_List);
        addToolTip(tip_List.getText(), tip_List);
        addToolTip(tip_runClick(), runClick_List);
        addToolTip(tip_loopTime(), loopTime_List);
        addToolTip(tip_learButton(), clearButton_List);
        addToolTip(tip_addPosition(), addPosition_List);
        addToolTip(tip_notOverwrite(), notOverwrite_List);
        addToolTip(tip_loadFolder_Click(), loadFolder_List);
        addToolTip(tip_loadAutoClick(), loadAutoClick_List);
        addToolTip(tip_outAutoClickPath(), addOutPath_List);
        addToolTip(tip_openDirectory(), openDirectory_List);
        addToolTip(tip_exportAutoClick(), exportAutoClick_List);
        addToolTip(tip_autoClickFileName() + defaultPMCSFileName(), outFileName_List);
        addToolTip(tip_preparationRunTime() + defaultPreparationRun, preparationRunTime_List);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        loopTime_List.setPromptText(defaultLoopTime);
        outFileName_List.setPromptText(defaultPMCSFileName());
        preparationRunTime_List.setPromptText(defaultPreparationRun);
    }

    /**
     * 创建任务参数对象
     *
     * @return 任务参数对象
     */
    private TaskBean<PMCListBean> creatTaskBean() {
        TaskBean<PMCListBean> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_List)
                .setMessageLabel(dataNumber_List)
                .setTableView(tableView_List)
                .setDisableNodes(disableNodes);
        return taskBean;
    }

    /**
     * 将自动流程添加到列表中
     *
     * @param clickPositionVOS 自动流程集合
     * @param filePath         要导入的文件路径
     */
    public void addPMCSFile(List<? extends PMCListBean> clickPositionVOS, String filePath) {
        // 向列表添加数据
        addData(clickPositionVOS, append, tableView_List, dataNumber_List, unit_files());
        if (CollectionUtils.isNotEmpty(clickPositionVOS)) {
            updateLabel(log_List, text_loadSuccess() + filePath);
            Platform.runLater(() -> log_List.setTextFill(Color.GREEN));
        }
    }

    /**
     * 启动加载自动操作文件任务
     *
     * @param files 要加载的文件
     */
    private void startLoadPMCTask(List<? extends File> files) {
        TaskBean<PMCListBean> taskBean = creatTaskBean();
        loadPMCFilsTask = loadPMCSFils(taskBean, files);
        bindingTaskNode(loadPMCFilsTask, taskBean);
        loadPMCFilsTask.setOnSucceeded(_ -> {
            taskUnbind(taskBean);
            PMCSLoadResult value = loadPMCFilsTask.getValue();
            String lastPMCPath = value.lastPMCPath();
            List<PMCListBean> clickPositionVOS = value.pmcListBeans();
            addPMCSFile(clickPositionVOS, lastPMCPath);
            loadPMCFilsTask = null;
        });
        loadPMCFilsTask.setOnFailed(event -> {
            taskUnbind(taskBean);
            taskNotSuccess(taskBean, text_taskFailed());
            loadPMCFilsTask = null;
            throw new RuntimeException(event.getSource().getException());
        });
        Thread.ofVirtual()
                .name("loadPMCSFilsTask-vThread" + tabId)
                .start(loadPMCFilsTask);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(clickLog_List);
        disableNodes.add(runClick_List);
        disableNodes.add(tableView_List);
        disableNodes.add(addPosition_List);
        disableNodes.add(clearButton_List);
        disableNodes.add(loadAutoClick_List);
        disableNodes.add(exportAutoClick_List);
        Node aboutTab = mainScene.lookup("#aboutTab");
        disableNodes.add(aboutTab);
        Node listPMCTab = mainScene.lookup("#listPMCTab");
        disableNodes.add(listPMCTab);
        Node settingTab = mainScene.lookup("#settingTab");
        disableNodes.add(settingTab);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
        Node timedStartTab = mainScene.lookup("#timedStartTab");
        disableNodes.add(timedStartTab);
    }

    /**
     * 设置快捷键提示
     */
    private void setShortcutText() {
        if (cancelKey == noKeyboard) {
            cancelTip_List.setText(text_noCancelKey());
        } else {
            cancelTip_List.setText(text_cancelTip_Click());
        }
    }

    /**
     * 获取选择的文件
     *
     * @throws IOException 配置文件保存异常
     */
    private void getSelectFile(List<? extends File> selectedFile) throws IOException {
        if (CollectionUtils.isNotEmpty(selectedFile)) {
            inFilePath = selectedFile.getFirst().getPath();
            updateProperties(configFile_List, key_inFilePath, new File(inFilePath).getParent());
            startLoadPMCTask(selectedFile);
        }
    }

    /**
     * 页面加载完毕后的执行逻辑
     *
     * @param event 设置页加载完成事件
     */
    private void autoClickLoaded(AutoClickLoadedEvent event) {
        if (noScreenCapturePermission) {
            setNodeDisable(runClick_List, true, autoClick_noPermissions());
            err_List.setText(tip_noScreenCapturePermission());
            err_List.setTooltip(creatTooltip(tip_noScreenCapturePermission()));
            adaption();
        }
        if (isNativeHookException) {
            String errorMessage = appName + autoClick_noPermissions();
            if (isMac) {
                errorMessage = tip_NativeHookException();
            }
            setNodeDisable(runClick_List, true, autoClick_noPermissions());
            err_List.setText(errorMessage);
            err_List.setTooltip(creatTooltip(tip_NativeHookException()));
            adaption();
        }
        if (StringUtils.isBlank(err_List.getText())) {
            logHBox_List.getChildren().remove(err_List);
        }
        // 运行定时任务
        if (StringUtils.isNotBlank(loadPMCSPath)) {
            TaskBean<PMCListBean> taskBean = creatTaskBean();
            loadedPMCSTask = buildPMCS(taskBean, new File(loadPMCSPath));
            loadedPMCSTask.setOnSucceeded(_ -> {
                taskUnbind(taskBean);
                List<PMCListBean> beans = loadedPMCSTask.getValue();
                addPMCSFile(beans, loadPMCSPath);
                loadedPMCSTask = null;
                try {
                    // 运行自动操作
                    if (runPMCSFile) {
                        runClick();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                // 清空启动参数
                clearArgs();
            });
            loadedPMCSTask.setOnFailed(e -> {
                taskNotSuccess(taskBean, text_taskFailed());
                loadedPMCSTask = null;
                throw new RuntimeException(e.getSource().getException());
            });
            Thread.ofVirtual()
                    .name("loadedPMCSTask-vThread" + tabId)
                    .start(loadedPMCSTask);
        }
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() throws IOException {
        // 读取配置文件
        getProperties();
        Platform.runLater(() -> {
            // 组件自适应宽高
            adaption();
            // 设置鼠标悬停提示
            setToolTip();
            // 设置文本输入框提示
            setPromptText();
            // 设置快捷键提示
            setShortcutText();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 给输入框添加内容变化监听
            textFieldChangeListener();
            // 设置初始配置值为上次配置值
            try {
                setLastConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 自动填充 JavaFX 表格
            autoBuildTableViewData(tableView_List, PMCListBean.class, tabId, index_List);
            // 监听列表数据变化
            tableView_List.getItems().addListener((ListChangeListener<PMCListBean>) _ ->
                    updateLabel(log_List, ""));
            // 表格设置为可编辑
            makeCellCanEdit();
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_List);
            // 构建右键菜单
            buildContextMenu();
            // 等待设置加载完毕
            EventBus.subscribe(AutoClickLoadedEvent.class, this::autoClickLoaded);
        });
    }

    /**
     * 运行自动点击按钮
     *
     */
    @FXML
    public void runClick() throws IOException {
        ObservableList<PMCListBean> tableViewItems = tableView_List.getItems();
        if (CollectionUtils.isEmpty(tableViewItems)) {
            throw new RuntimeException(text_noAutoClickToRun());
        }
        int loopTimes = setDefaultIntValue(loopTime_List, 1, 0, null);
        autoClickController.launchClickTask(null, loopTimes, true);
    }

    /**
     * 清空操作列表按钮
     */
    @FXML
    public void removeAll() {
        removeTableViewData(tableView_List, dataNumber_List);
    }

    /**
     * 导入操作流程按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException 配置文件读取异常、配置文件保存异常、页面加载失败
     */
    @FXML
    public void loadAutoClick(ActionEvent actionEvent) throws IOException {
        if (loadFolder_List.isSelected()) {
            List<String> extensionFilter = new ArrayList<>();
            extensionFilter.add(PMC);
            extensionFilter.add(PMCS);
            extensionFilter.add(extension_folder());
            FileChooserConfig fileConfig = new FileChooserConfig();
            fileConfig.setExtensionFilter(extensionFilter)
                    .setTitle(text_selectDirectory())
                    .setConfigPath(configFile_List)
                    .setPathKey(key_inFilePath)
                    .setShowDirectory(search_onlyDirectory())
                    .setShowHideFile(hide_noHideFile())
                    .setPath(inFilePath);
            FileChooserController controller = chooserFiles(fileConfig);
            // 设置回调
            controller.setFileChooserCallback(this::getSelectFile);
        } else {
            AutoClickController.isSonOpening = true;
            Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
            List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
            extensionFilters.add(new FileChooser.ExtensionFilter(appName, allPMC, allPMCS));
            extensionFilters.add(new FileChooser.ExtensionFilter(appName, allPMC));
            extensionFilters.add(new FileChooser.ExtensionFilter(appName, allPMCS));
            List<File> selectedFile = creatFilesChooser(window, inFilePath, extensionFilters, text_selectAutoFile());
            AutoClickController.isSonOpening = false;
            getSelectFile(selectedFile);
        }
    }

    /**
     * 导出操作流程按钮
     */
    @FXML
    public void exportAutoClick() {
        List<PMCListBean> tableViewItems = new ArrayList<>(tableView_List.getItems());
        if (CollectionUtils.isEmpty(tableViewItems)) {
            throw new RuntimeException(text_noAutoClickList());
        }
        String outFilePath = outPath_List.getText();
        if (StringUtils.isBlank(outFilePath)) {
            throw new RuntimeException(text_outPathNull());
        }
        TaskBean<PMCListBean> taskBean = creatTaskBean();
        taskBean.setMessageLabel(log_List)
                .setBeanList(tableViewItems);
        String fileName = setDefaultFileName(outFileName_List, defaultPMCSFileName());
        exportPMCTask = exportPMCS(taskBean, fileName, outFilePath, notOverwrite_List.isSelected());
        bindingTaskNode(exportPMCTask, taskBean);
        exportPMCTask.setOnSucceeded(_ -> {
            taskUnbind(taskBean);
            String path = exportPMCTask.getValue();
            log_List.setTextFill(Color.GREEN);
            if (openDirectory_List.isSelected()) {
                openDirectory(path);
            }
            exportPMCTask = null;
        });
        exportPMCTask.setOnFailed(event -> {
            exportPMCTask = null;
            taskNotSuccess(taskBean, text_taskFailed());
            throw new RuntimeException(event.getSource().getException());
        });
        Thread.ofVirtual()
                .name("exportPMCTask-vThread" + tabId)
                .start(exportPMCTask);
    }

    /**
     * 设置操作流程导出文件夹地址按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException 配置文件读取异常、配置文件保存异常
     */
    @FXML
    private void addOutPath(ActionEvent actionEvent) throws IOException {
        // 读取配置文件
        getProperties();
        AutoClickController.isSonOpening = true;
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        String outFilePath = outPath_List.getText();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory());
        AutoClickController.isSonOpening = false;
        if (selectedFile != null) {
            // 更新所选文件路径显示
            updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_List, configFile_List);
        }
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        startLoadPMCTask(files);
        dragEvent.setDropCompleted(true);
        dragEvent.consume();
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        // 接受拖放
        dragEvent.acceptTransferModes(TransferMode.COPY);
        dragEvent.consume();
    }

    /**
     * 查看运行记录
     *
     * @throws IOException 页面加载失败
     */
    @FXML
    private void clickLog() throws IOException {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/PMCLog-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent root = loader.load();
        PMCLogController controller = loader.getController();
        controller.initData(clickLogs);
        controller.setRefreshCallback(() -> {
            List<PMCLogBean> logs = controller.getClickLogs();
            if (CollectionUtils.isEmpty(logs)) {
                clickLogs.clear();
            }
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, logWidth, logHeight);
        detailStage.setScene(scene);
        detailStage.setTitle(clickLog_title());
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindowLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        // 设置 css 样式
        setWindowCss(scene, stylesCss);
        detailStage.show();
        AutoClickController.isSonOpening = true;
    }

}
