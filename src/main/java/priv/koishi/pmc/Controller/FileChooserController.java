package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FileChooserConfig;
import priv.koishi.pmc.Bean.Config.FileConfig;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.FileVO;
import priv.koishi.pmc.Callback.FileChooserCallback;
import priv.koishi.pmc.Utils.FileUtils;
import priv.koishi.pmc.Utils.UiUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonKeys.key_fileChooserHeight;
import static priv.koishi.pmc.Finals.CommonKeys.key_fileChooserWidth;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.MainApplication.isDarkTheme;
import static priv.koishi.pmc.Service.ReadDataService.readAllFilesTask;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ListenerUtils.textFieldValueListener;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.ToolTipUtils.addValueToolTip;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 自定义文件选择器控制器
 *
 * @author KOISHI
 * Date:2025-08-04
 * Time:22:12
 */
public class FileChooserController extends ManuallyChangeThemeController {

    /**
     * 页面标识符
     */
    private final String tabId = "_FC";

    /**
     * 文件查询设置
     */
    private FileChooserConfig fileChooserConfig;

    /**
     * 带鼠标悬停提示的内容变化删除器
     */
    private final List<Runnable> listenerRemovers = new ArrayList<>();

    /**
     * 文件查询任务
     */
    private Task<List<File>> readAllFilesTask;

    /**
     * 要防重复点击的组件
     */
    private final List<Node> disableNodes = new ArrayList<>();

    /**
     * 回调函数
     */
    @Setter
    private FileChooserCallback fileChooserCallback;

    /**
     * 文件选择页面舞台
     */
    private Stage stage;

    @FXML
    public ScrollPane scrollPane_FC;

    @FXML
    public VBox progressBarVBox_FC;

    @FXML
    public HBox fileTypeHBox_FC;

    @FXML
    public ProgressBar progressBar_FC;

    @FXML
    public TextField fileNameFilter_FC, filterFileType_FC;

    @FXML
    public CheckBox reverse_FC, filterNameCase_FC, reverseFileType_FC;

    @FXML
    public Label filePath_FC, fileNumber_FC, log_FC, tip_FC;

    @FXML
    public ChoiceBox<String> fileFilter_FC, hideFileType_FC, fileNameType_FC;

    @FXML
    public Button selectPathButton_FC, gotoParentButton_FC, refreshButton_FC, confirm_FC, close_FC;

    @FXML
    public TableView<FileVO> tableView_FC;

    @FXML
    public TableColumn<FileVO, Integer> index_FC;

    @FXML
    public TableColumn<FileVO, ImageView> thumb_FC;

    @FXML
    public TableColumn<FileVO, String> name_FC, path_FC, size_FC, fileType_FC,
            creatDate_FC, updateDate_FC, showStatus_FC;

    /**
     * 初始化数据
     *
     * @param fileChooserConfig 文件查询设置
     */
    public void initData(FileChooserConfig fileChooserConfig) {
        String path = fileChooserConfig.getPath();
        if (StringUtils.isBlank(path)) {
            path = defaultFileChooserPath;
        }
        this.fileChooserConfig = fileChooserConfig;
        String showDirectory = fileChooserConfig.getShowDirectory();
        if (StringUtils.isNotBlank(showDirectory)) {
            fileFilter_FC.setValue(showDirectory);
        }
        String fileNameType = fileChooserConfig.getFileNameType();
        if (StringUtils.isNotBlank(fileNameType)) {
            fileNameType_FC.setValue(fileNameType);
        }
        String showHideFile = fileChooserConfig.getShowHideFile();
        if (StringUtils.isNotBlank(showHideFile)) {
            hideFileType_FC.setValue(showHideFile);
        }
        reverse_FC.setSelected(fileChooserConfig.isReverseFileName());
        fileNameFilter_FC.setText(fileChooserConfig.getFileNameFilter());
        filterNameCase_FC.setSelected(fileChooserConfig.isFilterNameCase());
        List<String> filterExtensionList = fileChooserConfig.getFilterExtensionList();
        if (CollectionUtils.isNotEmpty(filterExtensionList)) {
            String filterExtension = String.join(" ", filterExtensionList);
            filterFileType_FC.setText(filterExtension);
        }
        reverseFileType_FC.setSelected(fileChooserConfig.isReverseFileType());
        // 设置鼠标悬停提示
        setToolTip();
        // 设置默认选中的文件
        selectFile(new File(path));
    }

    /**
     * 选择文件
     *
     * @param file 列表选中的数据
     */
    private void selectFile(File file) {
        String path = file.getPath();
        if (StringUtils.isNotBlank(path)) {
            String selectedPath;
            if (file.isFile()) {
                selectedPath = file.getParent();
            } else {
                selectedPath = path;
            }
            removeAll();
            FileConfig fileConfig = new FileConfig();
            fileConfig.setFilterExtensionList(getFilterExtensionList(filterFileType_FC))
                    .setReverseFileType(reverseFileType_FC.isSelected())
                    .setFilterNameCase(filterNameCase_FC.isSelected())
                    .setFileNameFilter(fileNameFilter_FC.getText())
                    .setShowHideFile(hideFileType_FC.getValue())
                    .setFileNameType(fileNameType_FC.getValue())
                    .setReverseFileName(reverse_FC.isSelected())
                    .setShowDirectory(fileFilter_FC.getValue())
                    .setSortType(sort_type())
                    .setPath(selectedPath)
                    .setReverseSort(true);
            TaskBean<FileVO> taskBean = new TaskBean<>();
            taskBean.setProgressBar(progressBar_FC)
                    .setMassageLabel(fileNumber_FC)
                    .setDisableNodes(disableNodes)
                    .setTableView(tableView_FC);
            readAllFilesTask = readAllFilesTask(taskBean, fileConfig);
            bindingTaskNode(readAllFilesTask, taskBean);
            readAllFilesTask.setOnSucceeded(_ -> {
                taskUnbind(taskBean);
                try {
                    addRemoveSameFile(readAllFilesTask.getValue(), false, tableView_FC);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // 获取所选文件路径
                setPathLabel(filePath_FC, selectedPath);
                updateTableViewSizeText(tableView_FC, fileNumber_FC, text_file());
            });
            if (!readAllFilesTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readAllFilesTask-vThread" + tabId)
                        .start(readAllFilesTask);
            }
        }
    }

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = stage.getHeight();
        tableView_FC.setPrefHeight(stageHeight * 0.53);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_FC.setMaxWidth(tableWidth);
        tableView_FC.setPrefWidth(tableWidth);
        progressBarVBox_FC.setMaxWidth(tableWidth * 0.4);
        progressBarVBox_FC.setPrefWidth(tableWidth * 0.4);
        bindPrefWidthProperty();
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        index_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.05));
        thumb_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.1));
        name_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.13));
        fileType_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.08));
        path_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.2));
        size_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.08));
        showStatus_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.08));
        creatDate_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.14));
        updateDate_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.14));
    }

    /**
     * 清空列表
     */
    private void removeAll() {
        if (readAllFilesTask != null) {
            readAllFilesTask.cancel();
            readAllFilesTask = null;
        }
        tableView_FC.getItems().stream().parallel().forEach(FileVO::clearResources);
        removeTableViewData(tableView_FC, fileNumber_FC);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_close(), close_FC);
        addToolTip(tip_confirm(), confirm_FC);
        addToolTip(tip_FC.getText(), tip_FC);
        addToolTip(reverse_FC.getText(), reverse_FC);
        addToolTip(tip_selectPath(), selectPathButton_FC);
        addToolTip(tip_gotoParent(), gotoParentButton_FC);
        addToolTip(tip_reselectButton(), refreshButton_FC);
        addToolTip(tip_fileNameFilter(), fileNameFilter_FC);
        addToolTip(tip_filterFileType(), filterFileType_FC);
        addToolTip(filterNameCase_FC.getText(), filterNameCase_FC);
        addToolTip(reverseFileType_FC.getText(), reverseFileType_FC);
        addValueToolTip(fileNameType_FC, tip_fileNameType(), fileNameType_FC.getValue());
        addValueToolTip(hideFileType_FC, tip_hideFileType(), hideFileType_FC.getValue());
        addValueToolTip(fileFilter_FC, tip_directoryNameType(), fileFilter_FC.getValue());
    }

    /**
     * 处理要过滤的文件类型
     *
     * @param filterFileType 填有空格区分的要过滤的文件类型字符串的文本输入框
     * @return 要过滤的文件类型 list
     */
    public static List<String> getFilterExtensionList(TextField filterFileType) {
        String filterFileTypeValue = filterFileType.getText();
        return getFilterExtensionList(filterFileTypeValue);
    }

    /**
     * 处理要过滤的文件类型
     *
     * @param filterFileTypeValue 空格区分的要过滤的文件类型字符串
     * @return 要过滤的文件类型 list
     */
    public static List<String> getFilterExtensionList(String filterFileTypeValue) {
        List<String> filterExtensionList = new ArrayList<>();
        if (StringUtils.isNotBlank(filterFileTypeValue)) {
            filterExtensionList = Arrays.asList(filterFileTypeValue.toLowerCase().split(" "));
        }
        return filterExtensionList;
    }

    /**
     * 双击列表数据进入点击的目录
     *
     * @param fileVO 列表数据
     */
    private void handleFileDoubleClick(FileVO fileVO) {
        if (fileVO != null) {
            // 双击文件夹时进入下级目录
            if (new File(fileVO.getPath()).isDirectory()) {
                selectFile(new File(fileVO.getPath()));
            } else {
                openDirectory(fileVO.getPath());
            }
        }
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(close_FC);
        disableNodes.add(confirm_FC);
        disableNodes.add(fileFilter_FC);
        disableNodes.add(hideFileType_FC);
        disableNodes.add(fileNameType_FC);
        disableNodes.add(refreshButton_FC);
        disableNodes.add(fileNameFilter_FC);
        disableNodes.add(selectPathButton_FC);
        disableNodes.add(gotoParentButton_FC);
    }

    /**
     * 查看文件选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildFilePathItem(TableView<FileVO> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_viewFile());
        // 创建二级菜单项
        MenuItem openFile = new MenuItem(menuItem_openSelected());
        MenuItem openDirector = new MenuItem(menuItem_openDirectory());
        MenuItem copyFilePath = new MenuItem(menuItem_copyFilePath());
        // 为每个菜单项添加事件处理
        openFile.setOnAction(_ -> openFileMenuItem(tableView));
        openDirector.setOnAction(_ -> openDirectorMenuItem(tableView));
        copyFilePath.setOnAction(_ -> copyFilePathItem(tableView));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(openFile, openDirector, copyFilePath);
        contextMenu.getItems().add(menu);
    }

    /**
     * 打开所选文件选项
     *
     * @param tableView 文件列表
     */
    private static void openFileMenuItem(TableView<FileVO> tableView) {
        List<FileVO> fileBeans = tableView.getSelectionModel().getSelectedItems();
        fileBeans.forEach(fileBean -> openFile(fileBean.getPath()));
    }

    /**
     * 打开所选文件所在文件夹选项
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void openDirectorMenuItem(TableView<FileVO> tableView) {
        List<FileVO> fileBeans = tableView.getSelectionModel().getSelectedItems();
        List<String> pathList = fileBeans.stream().map(FileVO::getPath).distinct().toList();
        pathList.forEach(FileUtils::openDirectory);
    }

    /**
     * 复制文件路径选项
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void copyFilePathItem(TableView<? extends FileVO> tableView) {
        FileVO fileBean = tableView.getSelectionModel().getSelectedItem();
        copyText(fileBean.getPath());
    }

    /**
     * 构建右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     */
    public void tableViewContextMenu(TableView<FileVO> tableView) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查询所选文件选项
        buildSelectPathItem(tableView, contextMenu);
        // 查看文件选项
        buildFilePathItem(tableView, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView, contextMenu);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView);
    }

    /**
     * 查询所选第一行文件
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单
     */
    private void buildSelectPathItem(TableView<? extends FileVO> tableView, ContextMenu contextMenu) {
        MenuItem selectPathItem = new MenuItem(text_checkFirstFile());
        selectPathItem.setOnAction(_ -> {
            FileVO selectedItem = tableView.getSelectionModel().getSelectedItems().getFirst();
            handleFileDoubleClick(selectedItem);
        });
        contextMenu.getItems().add(selectPathItem);
    }

    /**
     * 页面关闭事件处理逻辑
     */
    private void closeRequest() {
        try {
            tableView_FC.getItems().stream().parallel().forEach(FileVO::unbindTableView);
            removeAll();
            updateProperties(fileChooserConfig.getConfigPath(), fileChooserConfig.getPathKey(), filePath_FC.getText());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        // 清理监听器引用
        tableView_FC.setRowFactory(_ -> null);
        removeAllListeners();
        ContextMenu contextMenu = tableView_FC.getContextMenu();
        if (contextMenu != null) {
            // 清除所有菜单项事件
            contextMenu.getItems().stream().parallel().forEach(item -> item.setOnAction(null));
            tableView_FC.setContextMenu(null);
        }
        removeController();
        disableNodes.clear();
        stage = null;
    }

    /**
     * 获取页面关闭清除资源函数
     *
     * @return 页面关闭清除资源函数
     */
    private Runnable getClearResources() {
        return this::closeRequest;
    }

    /**
     * 设置列表双击事件
     */
    private void setRowDoubleClick() {
        tableView_FC.setRowFactory(_ -> {
            TableRow<FileVO> row = new TableRow<>();
            // 双击事件
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    FileVO selectedFileVO = row.getItem();
                    handleFileDoubleClick(selectedFileVO);
                }
            });
            // 更新行样式方法
            row.itemProperty().addListener((_, _, _) ->
                    updateRowStyle(row));
            row.selectedProperty().addListener((_, _, _) ->
                    updateRowStyle(row));
            return row;
        });
    }

    /**
     * 更新行样式
     *
     * @param row 要更新的行
     */
    private void updateRowStyle(TableRow<? extends FileVO> row) {
        FileVO item = row.getItem();
        if (item == null) {
            row.setStyle("");
            return;
        }
        if (row.isSelected()) {
            // 选中行的高亮样式
            row.setStyle("");
        } else {
            // 根据文件类型设置样式
            String fileType = item.getFileType();
            if (isDarkTheme) {
                if (extension_folder().equals(fileType)) {
                    row.setStyle("-fx-background-color: #190800;");
                } else if (PMC.equals(fileType)) {
                    row.setStyle("-fx-background-color: #42074c;");
                } else if (imageType.contains(fileType)) {
                    row.setStyle("-fx-background-color: #4c3007;");
                } else {
                    row.setStyle("");
                }
            } else {
                if (extension_folder().equals(fileType)) {
                    row.setStyle("-fx-background-color: #e6f7ff;");
                } else if (PMC.equals(fileType)) {
                    row.setStyle("-fx-background-color: #bdf8b3;");
                } else if (imageType.contains(fileType)) {
                    row.setStyle("-fx-background-color: #b3cff8;");
                } else {
                    row.setStyle("");
                }
            }
        }
    }

    /**
     * 使用自定义文件选择器选择文件
     *
     * @param fileChooserConfig 文件查询配置
     * @return 文件选择器控制器
     * @throws IOException 页面加载失败、配置文件读取异常
     */
    public static FileChooserController chooserFiles(FileChooserConfig fileChooserConfig) throws IOException {
        URL fxmlLocation = UiUtils.class.getResource(resourcePath + "fxml/FileChooser-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent root = loader.load();
        FileChooserController controller = loader.getController();
        controller.initData(fileChooserConfig);
        Stage detailStage = new Stage();
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        double with = Double.parseDouble(prop.getProperty(key_fileChooserWidth, "1300"));
        double height = Double.parseDouble(prop.getProperty(key_fileChooserHeight, "700"));
        input.close();
        Scene scene = new Scene(root, with, height);
        detailStage.setScene(scene);
        detailStage.setTitle(fileChooserConfig.getTitle());
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
        return controller;
    }

    /**
     * 初始化下拉框
     */
    private void setChoiceBoxItems() {
        // 文件名查询设置
        initializeChoiceBoxItems(fileNameType_FC, name_contain(), nameSearchTypeList);
        // 文件与文件夹查询设置
        initializeChoiceBoxItems(fileFilter_FC, search_fileDirectory(), searchTypeList);
        // 隐藏文件查询设置
        initializeChoiceBoxItems(hideFileType_FC, hide_noHideFile(), hideSearchTypeList);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 鼠标悬停提示输入的文件名过滤
        Runnable fileNameFilterChangeListener = textFieldValueListener(fileNameFilter_FC, tip_fileNameFilter());
        listenerRemovers.add(fileNameFilterChangeListener);
        // 鼠标悬停提示输入的需要识别的文件后缀名
        Runnable filterFileTypeChangeListener = textFieldValueListener(filterFileType_FC, tip_filterFileType());
        listenerRemovers.add(filterFileTypeChangeListener);
    }

    /**
     * 移除所有监听器
     */
    private void removeAllListeners() {
        listenerRemovers.forEach(Runnable::run);
        listenerRemovers.clear();
    }

    /**
     * 手动处理深色主题
     */
    public void manuallyChangeTheme() {
        manuallyChangeThemePane(scrollPane_FC, getClass());
        setRowDoubleClick();
        setTextColorProperty(textColorProperty, isDarkTheme ? Color.WHITE : Color.BLACK);
    }

    /**
     * 设置列排序
     */
    private void columnComparator() {
        // 设置文件大小排序
        fileSizeComparator(size_FC);
        // 设置文件名称排序
        fileNameComparator(name_FC);
        // 设置文件路径排序
        fileNameComparator(path_FC);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        // 手动处理深色主题
        manuallyChangeTheme();
        // 初始化下拉框
        setChoiceBoxItems();
        Platform.runLater(() -> {
            stage = (Stage) scrollPane_FC.getScene().getWindow();
            // 设置页面关闭事件处理逻辑
            stage.setOnCloseRequest(_ -> startClearResourcesTask(getClearResources(), tabId));
            // 组件自适应宽高
            adaption();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_FC, FileVO.class, tabId, index_FC);
            // 设置列排序
            columnComparator();
            // 构建右键菜单
            tableViewContextMenu(tableView_FC);
            // 给输入框添加内容变化监听
            textFieldChangeListener();
        });
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        File file = files.getFirst();
        try {
            if (file.isFile()) {
                selectFile(file.getParentFile());
            } else if (file.isDirectory()) {
                selectFile(file);
            }
        } catch (Exception e) {
            showExceptionAlert(e);
        } finally {
            dragEvent.setDropCompleted(true);
            dragEvent.consume();
        }
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
     * 选择要查询的文件夹
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void selectFilePath(ActionEvent actionEvent) {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, filePath_FC.getText(), text_selectDirectory());
        if (selectedFile != null) {
            selectFile(selectedFile);
        }
    }

    /**
     * 前往上级文件夹
     */
    @FXML
    private void gotoParent() {
        File file = new File(filePath_FC.getText());
        File parentFile = file.getParentFile();
        selectFile(parentFile);
    }

    /**
     * 确认选择的文件按钮
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void confirmSelect() throws IOException {
        ObservableList<FileVO> selectedItems = tableView_FC.getSelectionModel().getSelectedItems();
        List<File> fileVOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(selectedItems)) {
            for (FileVO fileVO : selectedItems) {
                File file = new File(fileVO.getPath());
                String fileType = fileVO.getFileType();
                List<String> extensionFilter = fileChooserConfig.getExtensionFilter();
                if (CollectionUtils.isNotEmpty(extensionFilter)) {
                    if (extensionFilter.contains(fileType)) {
                        fileVOList.add(file);
                    }
                } else {
                    fileVOList.add(file);
                }
            }
        } else {
            // 列表为空时，选择当前目录
            String path = filePath_FC.getText();
            if (StringUtils.isNotBlank(path)) {
                File file = new File(path);
                fileVOList.add(file);
            }
        }
        closeStage(stage, this::closeRequest);
        // 触发列表刷新
        if (fileChooserCallback != null) {
            fileChooserCallback.onFileChooser(fileVOList);
        }
    }

    /**
     * 取消按钮
     */
    @FXML
    private void closeWindow() {
        closeStage(stage, this::closeRequest);
    }

    /**
     * 刷新列表按钮
     */
    @FXML
    private void refreshTable() {
        selectFile(new File(filePath_FC.getText()));
    }

    /**
     * 过滤条件单选框监听
     */
    @FXML
    private void fileFilterAction() {
        addValueToolTip(fileFilter_FC, tip_directoryNameType(), fileFilter_FC.getValue());
        String showDirectory = fileFilter_FC.getValue();
        fileTypeHBox_FC.setVisible(!search_onlyDirectory().equals(showDirectory));
        refreshTable();
    }

    /**
     * 隐藏文件查询设置单选框监听
     */
    @FXML
    private void hideFileTypeAction() {
        addValueToolTip(hideFileType_FC, tip_hideFileType(), hideFileType_FC.getValue());
        refreshTable();
    }

    /**
     * 文件名查询设置单选框监听
     */
    @FXML
    private void fileNameTypeAction() {
        addValueToolTip(fileNameType_FC, tip_fileNameType(), fileNameType_FC.getValue());
        refreshTable();
    }

    /**
     * 反向查询文件名开关
     */
    @FXML
    private void reverseAction() {
        refreshTable();
    }

    /**
     * 区分大小写开关
     */
    @FXML
    private void filterNameCaseAction() {
        refreshTable();
    }

}
