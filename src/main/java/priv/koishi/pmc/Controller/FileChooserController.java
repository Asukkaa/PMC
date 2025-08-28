package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.defaultFileChooserPath;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Service.ReadDataService.readAllFilesTask;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.bindingTaskNode;
import static priv.koishi.pmc.Utils.TaskUtils.taskUnbind;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2025-08-04
 * Time:22:12
 */
public class FileChooserController extends RootController {

    /**
     * 页面标识符
     */
    private final String tabId = "_FC";

    /**
     * 文件查询设置
     */
    private FileChooserConfig fileChooserConfig;

    /**
     * 文件名输入框监听器
     */
    private ChangeListener<String> textFieldChangeListener;

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
    public AnchorPane anchorPane_FC;

    @FXML
    public HBox fileNumberHBox_FC;

    @FXML
    public VBox progressBarVBox_FC;

    @FXML
    public ProgressBar progressBar_FC;

    @FXML
    public TextField fileNameFilter_FC;

    @FXML
    public CheckBox reverse_FC, filterNameCase_FC;

    @FXML
    public Label filePath_FC, fileNumber_FC, log_FC, tip_FC;

    @FXML
    public ChoiceBox<String> fileFilter_FC, hideFileType_FC, fileNameType_FC;

    @FXML
    public Button selectPathButton_FC, gotoParentButton_FC, refreshButton_FC, confirm_FC, close_FC;

    @FXML
    public TableView<FileVO> tableView_FC;

    @FXML
    public TableColumn<FileVO, ImageView> thumb_FC;

    @FXML
    public TableColumn<FileVO, Integer> index_FC;

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
        removeAll();
        FileConfig fileConfig = new FileConfig();
        fileConfig.setFilterNameCase(filterNameCase_FC.isSelected())
                .setFileNameFilter(fileNameFilter_FC.getText())
                .setShowHideFile(hideFileType_FC.getValue())
                .setFileNameType(fileNameType_FC.getValue())
                .setReverseFileName(reverse_FC.isSelected())
                .setShowDirectory(fileFilter_FC.getValue())
                .setSortType(sort_type())
                .setPath(file.getPath())
                .setReverseSort(true);
        TaskBean<FileVO> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_FC)
                .setMassageLabel(fileNumber_FC)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_FC);
        readAllFilesTask = readAllFilesTask(taskBean, fileConfig);
        bindingTaskNode(readAllFilesTask, taskBean);
        readAllFilesTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            try {
                addRemoveSameFile(readAllFilesTask.getValue(), false, tableView_FC);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 获取所选文件路径
            setPathLabel(filePath_FC, file.getPath());
            updateTableViewSizeText(tableView_FC, fileNumber_FC, text_file());
        });
        if (!readAllFilesTask.isRunning()) {
            Thread.ofVirtual()
                    .name("readAllFilesTask-vThread" + tabId)
                    .start(readAllFilesTask);
        }
    }

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = stage.getHeight();
        tableView_FC.setPrefHeight(stageHeight * 0.6);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_FC.setMaxWidth(tableWidth);
        tableView_FC.setPrefWidth(tableWidth);
        progressBarVBox_FC.setMaxWidth(tableWidth * 0.4);
        progressBarVBox_FC.setPrefWidth(tableWidth * 0.4);
        regionRightAlignment(fileNumberHBox_FC, tableWidth, fileNumber_FC);
        bindPrefWidthProperty();
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        index_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.04));
        thumb_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.1));
        name_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.14));
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
        if (readAllFilesTask != null && readAllFilesTask.isRunning()) {
            readAllFilesTask.cancel();
            readAllFilesTask = null;
        }
        tableView_FC.getItems().stream().parallel().forEach(FileVO::clearResources);
        removeTableViewData(tableView_FC, fileNumber_FC, null);
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
        addToolTip(filterNameCase_FC.getText(), filterNameCase_FC);
        addValueToolTip(fileNameType_FC, tip_fileNameType(), fileNameType_FC.getValue());
        addValueToolTip(hideFileType_FC, tip_hideFileType(), hideFileType_FC.getValue());
        addValueToolTip(fileFilter_FC, tip_directoryNameType(), fileFilter_FC.getValue());
    }

    /**
     * 双击列表数据进入点击的目录
     *
     * @param fileVO 列表数据
     * @throws IOException io异常
     */
    private void handleFileDoubleClick(FileVO fileVO) throws IOException {
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
        openFile.setOnAction(event -> openFileMenuItem(tableView));
        openDirector.setOnAction(event -> openDirectorMenuItem(tableView));
        copyFilePath.setOnAction(event -> copyFilePathItem(tableView));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(openFile, openDirector, copyFilePath);
        contextMenu.getItems().add(menu);
    }

    /**
     * 打开所选文件选项
     *
     * @param tableView 文件列表
     * @throws RuntimeException io异常
     */
    private static void openFileMenuItem(TableView<FileVO> tableView) {
        List<FileVO> fileBeans = tableView.getSelectionModel().getSelectedItems();
        fileBeans.forEach(fileBean -> openFile(fileBean.getPath()));
    }

    /**
     * 打开所选文件所在文件夹选项
     *
     * @param tableView 要添加右键菜单的列表
     * @throws RuntimeException io异常
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
        selectPathItem.setOnAction(event -> {
            FileVO selectedItem = tableView.getSelectionModel().getSelectedItems().getFirst();
            try {
                handleFileDoubleClick(selectedItem);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        contextMenu.getItems().add(selectPathItem);
    }

    /**
     * 页面关闭事件处理逻辑
     */
    private void closeRequest() {
        try {
            removeAll();
            updateProperties(fileChooserConfig.getConfigPath(), fileChooserConfig.getPathKey(), filePath_FC.getText());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        // 清理监听器引用
        tableView_FC.setRowFactory(tv -> null);
        fileNameFilter_FC.textProperty().removeListener(textFieldChangeListener);
        ContextMenu contextMenu = tableView_FC.getContextMenu();
        if (contextMenu != null) {
            // 清除所有菜单项事件
            contextMenu.getItems().stream().parallel().forEach(item -> item.setOnAction(null));
            tableView_FC.setContextMenu(null);
        }
    }

    /**
     * 设置列表双击事件
     */
    private void setRowDoubleClick() {
        tableView_FC.setRowFactory(tv -> {
            TableRow<FileVO> row = new TableRow<>();
            // 双击事件
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    FileVO selectedFileVO = row.getItem();
                    try {
                        handleFileDoubleClick(selectedFileVO);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            // 更新行样式方法
            row.itemProperty().addListener((obs, oldItem, newItem) ->
                    updateRowStyle(row));
            row.selectedProperty().addListener((obs, wasSelected, isSelected) ->
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
            if (extension_folder().equals(fileType)) {
                row.setStyle("-fx-background-color: #e6f7ff;");
            } else {
                row.setStyle("");
            }
        }
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
     * 界面初始化
     */
    @FXML
    private void initialize() {
        // 初始化下拉框
        setChoiceBoxItems();
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_FC.getScene().getWindow();
            // 设置页面关闭事件处理逻辑
            stage.setOnCloseRequest(e -> closeRequest());
            // 组件自适应宽高
            adaption();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_FC, FileVO.class, tabId, index_FC);
            // 设置文件大小排序
            fileSizeColum(size_FC);
            // 设置列表双击事件
            setRowDoubleClick();
            // 构建右键菜单
            tableViewContextMenu(tableView_FC);
            // 给输入框添加内容变化监听
            textFieldChangeListener = textFieldValueListener(fileNameFilter_FC, tip_fileNameFilter());
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
     * @throws IOException io异常
     */
    @FXML
    private void confirmSelect() throws IOException {
        ObservableList<FileVO> selectedItems = tableView_FC.getSelectionModel().getSelectedItems();
        List<File> fileVOList = new ArrayList<>();
        String showDirectory = fileChooserConfig.getShowDirectory();
        if (CollectionUtils.isNotEmpty(selectedItems)) {
            for (FileVO fileVO : selectedItems) {
                File file = new File(fileVO.getPath());
                String fileType = fileVO.getFileType();
                if (search_onlyDirectory().equals(showDirectory)) {
                    if (extension_folder().equals(fileType)) {
                        fileVOList.add(file);
                    }
                } else if (search_onlyFile().equals(showDirectory)) {
                    if (!extension_folder().equals(fileType)) {
                        List<String> filterExtensionList = fileChooserConfig.getFilterExtensionList();
                        if (CollectionUtils.isEmpty(filterExtensionList) || filterExtensionList.contains(fileType)) {
                            fileVOList.add(file);
                        }
                    }
                }
            }
        } else {
            // 列表为空时，选择当前目录
            File file = new File(filePath_FC.getText());
            fileVOList.add(file);
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
