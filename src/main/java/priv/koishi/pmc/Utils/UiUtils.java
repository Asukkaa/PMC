package priv.koishi.pmc.Utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Interface.UsedByReflection;
import priv.koishi.pmc.MainApplication;
import priv.koishi.pmc.MessageBubble.MessageBubble;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.function.Function;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.CommonUtils.errToString;
import static priv.koishi.pmc.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.pmc.Utils.FileUtils.*;

/**
 * ui相关工具类
 *
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:38
 */
public class UiUtils {

    private static final DataFormat dataFormat = new DataFormat("application/x-java-serialized-object");

    private static final Logger logger = LogManager.getLogger(UiUtils.class);

    /**
     * 鼠标停留提示框
     *
     * @param nodes 需要显示提示框的组件
     * @param tip   提示卡信息
     */
    public static void addToolTip(String tip, Node... nodes) {
        for (Node node : nodes) {
            Tooltip.install(node, creatTooltip(tip));
        }
    }

    /**
     * 设置永久显示的鼠标停留提示框参数
     *
     * @param tip 提示文案
     * @return 设置参数后的Tooltip对象
     */
    public static Tooltip creatTooltip(String tip) {
        return creatTooltip(tip, Duration.INDEFINITE);
    }

    /**
     * 设置鼠标停留提示框参数
     *
     * @param tip      提示文案
     * @param duration 显示时长
     * @return 设置参数后的Tooltip对象
     */
    public static Tooltip creatTooltip(String tip, Duration duration) {
        Tooltip tooltip = new Tooltip(tip);
        tooltip.setWrapText(true);
        tooltip.setShowDuration(duration);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        tooltip.getStyleClass().add("tooltip-font-size");
        return tooltip;
    }

    /**
     * 文本输入框鼠标停留提示输入值
     *
     * @param textField 要添加提示的文本输入框
     * @param text      要展示的提示文案
     * @param valueText 当前所填值提示文案
     */
    public static void addValueToolTip(TextField textField, String text, String valueText) {
        String value = textField.getText();
        addValueToolTip(textField, text, valueText, value);
    }

    /**
     * 为组件添加鼠标悬停提示框
     *
     * @param node      要添加提示的组件
     * @param text      提示文案
     * @param valueText 当前所填值提示文案
     * @param value     当前所填值
     */
    public static void addValueToolTip(Node node, String text, String valueText, String value) {
        if (StringUtils.isNotEmpty(text)) {
            if (StringUtils.isNotEmpty(value)) {
                addToolTip(text + "\n" + valueText + value, node);
            } else {
                addToolTip(text, node);
            }
        } else {
            if (StringUtils.isNotEmpty(value)) {
                addToolTip(value, node);
            } else {
                addToolTip(null, node);
            }
        }
    }

    /**
     * 创建一个文件选择器
     *
     * @param window           文件选择器窗口
     * @param path             文件选择器初始路径
     * @param extensionFilters 要过滤的文件格式
     * @param title            文件选择器标题
     * @return 文件选择器选择的文件
     */
    public static File creatFileChooser(Window window, String path, List<FileChooser.ExtensionFilter> extensionFilters, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        // 设置初始目录
        if (StringUtils.isBlank(path)) {
            fileChooser.setInitialDirectory(new File(userHome));
        } else {
            File file = new File(path);
            // 设置初始目录
            if (file.isDirectory()) {
                fileChooser.setInitialDirectory(file);
            } else if (file.isFile()) {
                file = new File(file.getParent());
                fileChooser.setInitialDirectory(file);
            }
        }
        // 设置过滤条件
        if (CollectionUtils.isNotEmpty(extensionFilters)) {
            fileChooser.getExtensionFilters().addAll(extensionFilters);
        }
        return fileChooser.showOpenDialog(window);
    }

    /**
     * 创建一个文件夹选择器
     *
     * @param window 文件夹选择器窗口
     * @param path   文件夹选择器初始路径
     * @param title  文件夹选择器标题
     * @return 文件夹选择器选择的文件夹
     */
    public static File creatDirectoryChooser(Window window, String path, String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        // 设置初始目录
        if (StringUtils.isBlank(path) || !new File(path).isDirectory()) {
            directoryChooser.setInitialDirectory(new File(userHome));
        } else {
            directoryChooser.setInitialDirectory(new File(path));
        }
        return directoryChooser.showDialog(window);
    }

    /**
     * 创建一个图片选择器（只支持png、jpg、jpeg格式）
     *
     * @param window            文件选择器窗口
     * @param stopImgSelectPath 默认路径
     * @return 选择的图片
     */
    public static File creatImgChooser(Window window, String stopImgSelectPath) {
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
        extensionFilters.add(new FileChooser.ExtensionFilter("图片", allImageType));
        extensionFilters.add(new FileChooser.ExtensionFilter(png, allPng));
        extensionFilters.add(new FileChooser.ExtensionFilter(jpg, allJpg));
        extensionFilters.add(new FileChooser.ExtensionFilter(jpeg, allJpeg));
        return creatFileChooser(window, stopImgSelectPath, extensionFilters, text_selectTemplateImg);
    }

    /**
     * 设置默认数值
     *
     * @param textField    要设置默认值的文本输入框
     * @param defaultValue 默认值
     * @param min          文本输入框可填写的最小值，为空则不限制最小值
     * @param max          文本输入框可填写的最大值，为空则不限制最大值
     * @return 文本输入框所填值如果在规定范围内则返回所填值，否则返回默认值
     */
    public static int setDefaultIntValue(TextField textField, int defaultValue, Integer min, Integer max) {
        String valueStr = textField.getText();
        int value = defaultValue;
        if (isInIntegerRange(valueStr, min, max)) {
            value = Integer.parseInt(valueStr);
        }
        return value;
    }

    /**
     * 设置默认文件名
     *
     * @param textField    要设置默认文件名的文本输入框
     * @param defaultValue 默认文件名
     * @return 如果文本输入框填的是合法文件名则返回所填值，不合法则返回默认值
     */
    public static String setDefaultFileName(TextField textField, String defaultValue) {
        // 去掉开头的空字符
        String valueStr = textField.getText().replaceFirst("^\\s+", "");
        String value = defaultValue;
        if (isValidFileName(valueStr)) {
            value = valueStr;
        }
        return value;
    }

    /**
     * 处理异常的统一弹窗
     *
     * @param ex 要处理的异常
     */
    public static void showExceptionAlert(Throwable ex) {
        logger.error(ex, ex);
        Alert alert = creatErrorAlert(errToString(ex));
        Throwable cause = ex.getCause().getCause();
        if (cause != null) {
            if (cause instanceof Exception) {
                alert.setHeaderText(cause.getMessage());
            } else {
                alert.setHeaderText(ex.getMessage());
            }
        } else {
            alert.setHeaderText(ex.getMessage());
        }
        // 展示弹窗
        alert.showAndWait();
    }

    /**
     * 创建一个错误弹窗
     *
     * @param errString 要展示的异常信息
     * @return Alert弹窗对象
     */
    public static Alert creatErrorAlert(String errString) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("异常信息");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        setWindLogo(stage, logoPath);
        // 创建展示异常信息的TextArea
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(errString);
        // 创建VBox并添加TextArea
        VBox details = new VBox();
        details.heightProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> textArea.setPrefHeight(details.getHeight())));
        details.getChildren().add(textArea);
        alert.getDialogPane().setExpandableContent(details);
        return alert;
    }

    /**
     * 创建一个确认弹窗
     *
     * @param confirm 确认框文案
     * @param ok      确认按钮文案
     * @param cancel  取消按钮文案
     * @return 被点击的按钮
     */
    public static ButtonType creatConfirmDialog(String confirm, String ok, String cancel) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setHeaderText(confirm);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        setWindLogo(stage, logoPath);
        ButtonType cancelButton = new ButtonType(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType okButton = new ButtonType(ok, ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
        return dialog.showAndWait().orElse(cancelButton);
    }

    /**
     * 给窗口设置logo
     *
     * @param stage 要设置logo的窗口
     * @param path  logo路径
     */
    public static void setWindLogo(Stage stage, String path) {
        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource(path)).toString()));
    }

    /**
     * 为javafx单元格赋值并添加鼠标悬停提示
     *
     * @param tableColumn 要处理的javafx列表列
     * @param param       javafx列表列对应的数据属性名
     */
    public static void buildCellValue(TableColumn<?, ?> tableColumn, String param) {
        tableColumn.setCellValueFactory(new PropertyValueFactory<>(param));
        addTableColumnToolTip(tableColumn);
    }

    /**
     * 自定义单元格工厂，为单元格添加Tooltip
     *
     * @param column 要处理的javafx表格单元格
     */
    public static <S, T> void addTableColumnToolTip(TableColumn<S, T> column) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || StringUtils.isEmpty(item.toString())) {
                            setText(null);
                            setTooltip(null);
                        } else if (!item.toString().isEmpty()) {
                            setText(item.toString());
                            setTooltip(creatTooltip(item.toString()));
                        }
                    }
                };
            }
        });
    }

    /**
     * 根据bean属性名自动填充javafx表格
     *
     * @param tableView 要处理的javafx表格
     * @param beanClass 要处理的javafx表格的数据bean类
     * @param tabId     用于区分不同列表的id，要展示的数据bean属性名加上tabId即为javafx列表的列对应的id
     */
    @SuppressWarnings("unchecked")
    public static <T> void autoBuildTableViewData(TableView<T> tableView, Class<?> beanClass, String tabId) {
        // 获取对象的所有字段
        List<Field> fields = List.of(beanClass.getDeclaredFields());
        ObservableList<? extends TableColumn<?, ?>> columns = tableView.getColumns();
        fields.forEach(f -> {
            String fieldName = f.getName();
            String finalFieldName;
            if (StringUtils.isNotEmpty(tabId)) {
                finalFieldName = fieldName + tabId;
            } else {
                finalFieldName = fieldName;
            }
            Optional<? extends TableColumn<?, ?>> matched = columns.stream().filter(c ->
                    c.getId().equals(finalFieldName)).findFirst();
            matched.ifPresent(m -> {
                if (f.getType() == Image.class) {
                    try {
                        Method getter = beanClass.getMethod("getThumb");
                        // 显式标记方法调用（解决IDE误报）
                        if (getter.isAnnotationPresent(UsedByReflection.class)) {
                            Function<T, Image> supplier = bean -> {
                                try {
                                    return (Image) getter.invoke(bean);
                                } catch (Exception e) {
                                    return null;
                                }
                            };
                            buildThumbnailCell((TableColumn<T, Image>) m, supplier);
                        }
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    buildCellValue(m, fieldName);
                }
            });
        });
    }

    /**
     * 创建图片表格
     *
     * @param column 要创建图片表格的列
     */
    public static <T> void buildThumbnailCell(TableColumn<T, Image> column, Function<T, Image> thumbSupplier) {
        column.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(thumbSupplier.apply(cellData.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);
                if (empty || image == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(image);
                    setGraphic(imageView);
                }
            }
        });
    }

    /**
     * 获取表格中的缩略图
     *
     * @param path 缩略图路径
     * @return 表格中的缩略图对象
     */
    public static Service<Image> tableViewImageService(String path) {
        return new Service<>() {
            @Override
            protected Task<Image> createTask() {
                return new Task<>() {
                    @Override
                    protected Image call() {
                        if (StringUtils.isNotBlank(path)) {
                            System.out.println("tableViewImageService:" + path);
                            return new Image("file:" + path, 200, 200, true, true, true);
                        } else {
                            return null;
                        }
                    }
                };
            }
        };
    }

    /**
     * 清空javafx列表数据
     *
     * @param tableView  要清空的javafx列表
     * @param fileNumber 用于展示列表数据数量的文本框
     * @param log        用于展示任务消息的文本框
     */
    public static <T> void removeTableViewData(TableView<T> tableView, Label fileNumber, Label log) {
        tableView.getItems().clear();
        updateLabel(fileNumber, text_dataListNull);
        if (log != null) {
            updateLabel(log, "");
        }
        System.gc();
    }

    /**
     * 向列表指定位置添加数据
     *
     * @param data           要添加的数据
     * @param addType        添加位置类型
     * @param tableView      要添加数据的列表
     * @param dataNumber     用于展示列表数据数量的文本框
     * @param dataNumberUnit 数据数量单位
     */
    public static <T> void addData(List<T> data, int addType, TableView<T> tableView, Label dataNumber, String dataNumberUnit) {
        ObservableList<T> tableViewItems = tableView.getItems();
        List<T> selectedItem = tableView.getSelectionModel().getSelectedItems();
        switch (addType) {
            // 在列表所选行第一行上方插入
            case upAdd: {
                // 获取首个选中行的索引
                int selectedIndex = tableViewItems.indexOf(selectedItem.getFirst());
                // 在选中行上方插入数据
                tableView.getItems().addAll(selectedIndex, data);
                // 滚动到插入位置
                tableView.scrollTo(selectedIndex);
                // 选中新插入的数据
                tableView.getSelectionModel().selectRange(selectedIndex, selectedIndex + data.size());
                // 插入后重新选中
                tableView.getSelectionModel().selectIndices(selectedIndex, selectedIndex + data.size());
                break;
            }
            // 在列表所选行最后一行下方插入
            case downAdd: {
                // 获取最后一个选中行的索引
                int selectedIndex = tableViewItems.indexOf(selectedItem.getLast()) + 1;
                // 在选中行下方插入数据
                tableView.getItems().addAll(selectedIndex, data);
                // 滚动到插入位置
                tableView.scrollTo(selectedIndex);
                // 选中新插入的数据
                tableView.getSelectionModel().selectRange(selectedIndex, selectedIndex + data.size());
                // 插入后重新选中
                tableView.getSelectionModel().selectIndices(selectedIndex, selectedIndex + data.size() - 1);
                break;
            }
            // 向列表第一行上方插入
            case topAdd: {
                // 向列表第一行追加数据
                tableView.getItems().addAll(0, data);
                // 滚动到插入位置
                tableView.scrollTo(0);
                break;
            }
            // 向列表最后一行追加
            case append: {
                // 向列表最后一行追加数据
                tableViewItems.addAll(data);
                // 滚动到插入位置
                tableView.scrollTo(tableViewItems.size());
                break;
            }
        }
        // 同步表格数据量
        dataNumber.setText(text_allHave + tableViewItems.size() + dataNumberUnit);
    }

    /**
     * 限制输入框只能输入指定范围内的整数
     *
     * @param textField 要处理的文本输入框
     * @param min       可输入的最小值，为空则不限制
     * @param max       可输入的最大值，为空则不限制
     * @param tip       鼠标悬停提示文案
     */
    public static void integerRangeTextField(TextField textField, Integer min, Integer max, String tip) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // 这里处理文本变化的逻辑
            if (!isInIntegerRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                textField.setText(oldValue);
            }
            addValueToolTip(textField, tip, text_nowValue);
        });
    }

    /**
     * 监听输入框内容变化
     *
     * @param textField 要监听的文本输入框
     * @param tip       鼠标悬停提示文案
     */
    public static void textFieldValueListener(TextField textField, String tip) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> addValueToolTip(textField, tip, text_nowValue));
    }

    /**
     * 修改label信息
     *
     * @param label 要修改的文本栏
     * @param text  要修改的文本
     */
    public static void updateLabel(Label label, String text) {
        label.textProperty().unbind();
        label.setText(text);
        label.setTextFill(Color.BLACK);
    }

    /**
     * 更新所选文件路径显示
     *
     * @param selectedFilePath 本次所选的文件路径
     * @param filePath         上次选的文件路径
     * @param pathKey          配置文件中路径的key
     * @param pathLabel        要展示路径的文本框
     * @param configFile       要更新的配置文件
     * @return 所选文件路径
     * @throws IOException io异常
     */
    public static String updatePathLabel(String selectedFilePath, String filePath, String pathKey, Label pathLabel, String configFile) throws IOException {
        // 只有跟上次选的路径不一样才更新
        if (StringUtils.isBlank(filePath) || !filePath.equals(selectedFilePath)) {
            updateProperties(configFile, pathKey, selectedFilePath);
            filePath = selectedFilePath;
        }
        if (pathLabel != null) {
            setPathLabel(pathLabel, selectedFilePath, false);
        }
        return filePath;
    }

    /**
     * 设置列表通过拖拽排序行
     *
     * @param tableView 要处理的列表
     */
    public static <T> void tableViewDragRow(TableView<T> tableView) {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            // 拖拽-检测
            row.setOnDragDetected(e -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(dataFormat, index);
                    db.setContent(cc);
                    e.consume();
                }
            });
            // 释放-验证
            row.setOnDragOver(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dataFormat)) {
                    if (row.getIndex() != (Integer) db.getContent(dataFormat) && row.getIndex() < tableView.getItems().size()) {
                        e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        e.consume();
                    }
                }
            });
            // 释放-执行
            row.setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dataFormat)) {
                    int draggedIndex = (Integer) db.getContent(dataFormat);
                    int dropIndex;
                    if (row.isEmpty()) {
                        dropIndex = tableView.getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }
                    tableView.getItems().add(dropIndex, tableView.getItems().remove(draggedIndex));
                    e.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    e.consume();
                }
            });
            return row;
        });
    }

    /**
     * 移动所选行选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static <T> void buildMoveDataMenu(TableView<T> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu("移动所选数据");
        // 创建二级菜单项
        MenuItem up = new MenuItem("所选行上移一行");
        MenuItem down = new MenuItem("所选行下移一行");
        // 为每个菜单项添加事件处理
        up.setOnAction(event -> upMoveDataMenuItem(tableView));
        down.setOnAction(event -> downMoveDataMenuItem(tableView));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(up, down);
        contextMenu.getItems().add(menu);
    }

    /**
     * 所选行上移一行选项
     *
     * @param tableView 要处理的数据列表
     */
    private static <T> void upMoveDataMenuItem(TableView<T> tableView) {
        // getSelectedCells处理上移操作有bug，通过getSelectedItems拿到的数据是实时变化的，需要一个新的list来存
        List<T> selectionList = tableView.getSelectionModel().getSelectedItems();
        List<T> selections = new ArrayList<>(selectionList);
        List<T> fileList = tableView.getItems();
        List<T> tempList = new ArrayList<>(fileList);
        // 上移所选数据位置
        for (int i = 0; i < selectionList.size(); i++) {
            T t = selectionList.get(i);
            int index = fileList.indexOf(t);
            if (index - i > 0) {
                tempList.set(index, tempList.get(index - 1));
                tempList.set(index - 1, t);
            }
        }
        fileList.clear();
        fileList.addAll(tempList);
        // 重新选中移动后的数据
        for (T t : selections) {
            int index = fileList.indexOf(t);
            if (index != -1) {
                tableView.getSelectionModel().select(index);
            }
        }
    }

    /**
     * 所选行下移一行选项
     *
     * @param tableView 要处理的数据列表
     */
    private static <T> void downMoveDataMenuItem(TableView<T> tableView) {
        var selectedCells = tableView.getSelectionModel().getSelectedCells();
        int loopTime = 0;
        for (int i = selectedCells.size(); i > 0; i--) {
            int row = selectedCells.get(i - 1).getRow();
            List<T> fileList = tableView.getItems();
            loopTime++;
            if (row + loopTime < fileList.size()) {
                fileList.add(row, fileList.remove(row + 1));
            }
        }
    }

    /**
     * 所选行上移一行选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static <T> void buildUpMoveDataMenuItem(TableView<T> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("所选行上移一行");
        menuItem.setOnAction(event -> upMoveDataMenuItem(tableView));
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 所选行下移一行选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static <T> void buildDownMoveDataMenuItem(TableView<T> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("所选行下移一行");
        menuItem.setOnAction(event -> downMoveDataMenuItem(tableView));
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 查看文件选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildFilePathItem(TableView<ImgFileBean> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu("查看文件");
        // 创建二级菜单项
        MenuItem openFile = new MenuItem("打开所选文件");
        MenuItem openDirector = new MenuItem("打开所选文件所在文件夹");
        MenuItem copyFilePath = new MenuItem("复制文件路径");
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
    private static void openFileMenuItem(TableView<ImgFileBean> tableView) {
        List<ImgFileBean> fileBeans = tableView.getSelectionModel().getSelectedItems();
        fileBeans.forEach(fileBean -> {
            try {
                openFile(fileBean.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 打开所选文件所在文件夹选项
     *
     * @param tableView 要添加右键菜单的列表
     * @throws RuntimeException io异常
     */
    private static void openDirectorMenuItem(TableView<ImgFileBean> tableView) {
        List<ImgFileBean> fileBeans = tableView.getSelectionModel().getSelectedItems();
        List<String> pathList = fileBeans.stream().map(ImgFileBean::getPath).distinct().toList();
        pathList.forEach(path -> {
            try {
                openDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 复制文件路径选项
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void copyFilePathItem(TableView<ImgFileBean> tableView) {
        ImgFileBean fileBean = tableView.getSelectionModel().getSelectedItem();
        copyText(fileBean.getPath());
    }

    /**
     * 复制所选数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param dataNumber  列表数据数量文本框
     */
    public static void buildCopyDataMenu(TableView<ClickPositionBean> tableView, ContextMenu contextMenu, Label dataNumber) {
        Menu menu = new Menu("复制所选数据");
        // 创建二级菜单项
        MenuItem upCopy = new MenuItem(menuItem_upCopy);
        MenuItem downCopy = new MenuItem(menuItem_downCopy);
        MenuItem appendCopy = new MenuItem(menuItem_appendCopy);
        MenuItem topCopy = new MenuItem(menuItem_topCopy);
        // 为每个菜单项添加事件处理
        upCopy.setOnAction(event -> copyDataMenuItem(tableView, menuItem_upCopy, dataNumber));
        downCopy.setOnAction(event -> copyDataMenuItem(tableView, menuItem_downCopy, dataNumber));
        appendCopy.setOnAction(event -> copyDataMenuItem(tableView, menuItem_appendCopy, dataNumber));
        topCopy.setOnAction(event -> copyDataMenuItem(tableView, menuItem_topCopy, dataNumber));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(upCopy, downCopy, appendCopy, topCopy);
        contextMenu.getItems().add(menu);
    }

    /**
     * 复制所选数据二级菜单选项
     *
     * @param tableView  要处理的数据列表
     * @param copyType   复制类型
     * @param dataNumber 列表数据数量文本框
     */
    private static void copyDataMenuItem(TableView<ClickPositionBean> tableView, String copyType, Label dataNumber) {
        List<ClickPositionBean> copiedList = getCopyList(tableView.getSelectionModel().getSelectedItems());
        switch (copyType) {
            case menuItem_upCopy: {
                addData(copiedList, upAdd, tableView, dataNumber, text_process);
                break;
            }
            case menuItem_downCopy: {
                addData(copiedList, downAdd, tableView, dataNumber, text_process);
                break;
            }
            case menuItem_appendCopy: {
                addData(copiedList, append, tableView, dataNumber, text_process);
                break;
            }
            case menuItem_topCopy: {
                addData(copiedList, topAdd, tableView, dataNumber, text_process);
                break;
            }
        }
    }

    /**
     * 获取复制的数据
     *
     * @param selectedItem 选中的数据
     * @return 复制的数据
     */
    private static List<ClickPositionBean> getCopyList(List<ClickPositionBean> selectedItem) {
        List<ClickPositionBean> copiedList = new ArrayList<>();
        selectedItem.forEach(clickPositionBean -> {
            ClickPositionBean copyClickPositionBean = new ClickPositionBean();
            copyClickPositionBean.setName(clickPositionBean.getName())
                    .setStartX(clickPositionBean.getStartX())
                    .setStartY(clickPositionBean.getStartY())
                    .setEndX(clickPositionBean.getEndX())
                    .setEndY(clickPositionBean.getEndY())
                    .setClickTime(clickPositionBean.getClickTime())
                    .setClickNum(clickPositionBean.getClickNum())
                    .setClickInterval(clickPositionBean.getClickInterval())
                    .setWaitTime(clickPositionBean.getWaitTime())
                    .setType(clickPositionBean.getType());
            copiedList.add(copyClickPositionBean);
        });
        return copiedList;
    }

    /**
     * 修改操作类型
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildEditClickTypeMenu(TableView<ClickPositionBean> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu("更改操作类型");
        // 创建二级菜单项
        MenuItem primary = new MenuItem(mouseButton_primary);
        MenuItem secondary = new MenuItem(mouseButton_secondary);
        MenuItem middle = new MenuItem(mouseButton_middle);
        MenuItem forward = new MenuItem(mouseButton_forward);
        MenuItem back = new MenuItem(mouseButton_back);
        MenuItem none = new MenuItem(mouseButton_none);
        // 为每个菜单项添加事件处理
        primary.setOnAction(event -> updateClickTypeMenuItem(tableView, mouseButton_primary));
        secondary.setOnAction(event -> updateClickTypeMenuItem(tableView, mouseButton_secondary));
        middle.setOnAction(event -> updateClickTypeMenuItem(tableView, mouseButton_middle));
        forward.setOnAction(event -> updateClickTypeMenuItem(tableView, mouseButton_forward));
        back.setOnAction(event -> updateClickTypeMenuItem(tableView, mouseButton_back));
        none.setOnAction(event -> updateClickTypeMenuItem(tableView, mouseButton_none));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(primary, secondary, middle, forward, back, none);
        contextMenu.getItems().add(menu);
    }

    /**
     * 修改操作类型二级菜单选项
     *
     * @param tableView 要添加右键菜单的列表
     * @param clickType 操作类型
     */
    private static void updateClickTypeMenuItem(TableView<ClickPositionBean> tableView, String clickType) {
        List<ClickPositionBean> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            selectedItem.forEach(bean -> {
                bean.setType(clickType);
                tableView.refresh();
            });
        }
    }

    /**
     * 修改所选项图片地址
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param dataNumber  列表数据数量文本框
     * @param unit        列表数据数量单位
     */
    public static void buildEditImgPathMenu(TableView<ImgFileBean> tableView, ContextMenu contextMenu, Label dataNumber, String unit) {
        MenuItem upMoveDataMenuItem = new MenuItem("更改所选项第一行的图片");
        upMoveDataMenuItem.setOnAction(event -> {
            ObservableList<ImgFileBean> selectedItems = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItems)) {
                ImgFileBean selectedItem = selectedItems.getFirst();
                Window window = tableView.getScene().getWindow();
                File file = creatImgChooser(window, selectedItem.getPath());
                if (file != null) {
                    List<ImgFileBean> allImg = tableView.getItems();
                    List<ImgFileBean> checkList = new ArrayList<>(allImg);
                    checkList.remove(selectedItem);
                    boolean isExist = checkList.stream().anyMatch(bean ->
                            file.getPath().equals(bean.getPath()));
                    if (isExist) {
                        ButtonType buttonType = creatConfirmDialog("图片已存在，是否删除这张选中图片？", "删除", "不删除");
                        if (!buttonType.getButtonData().isCancelButton()) {
                            tableView.getItems().remove(selectedItem);
                        }
                    } else {
                        selectedItem.setPath(file.getPath());
                        selectedItem.updateThumb();
                        selectedItem.setPath(file.getPath());
                        selectedItem.updateThumb();
                    }
                    tableView.refresh();
                    dataNumber.setText(text_allHave + allImg.size() + unit);
                }
            }
        });
        contextMenu.getItems().add(upMoveDataMenuItem);
    }

    /**
     * 取消选中选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static <T> void buildClearSelectedData(TableView<T> tableView, ContextMenu contextMenu) {
        MenuItem clearSelectedDataMenuItem = new MenuItem("取消选中");
        clearSelectedDataMenuItem.setOnAction(event -> tableView.getSelectionModel().clearSelection());
        contextMenu.getItems().add(clearSelectedDataMenuItem);
    }

    /**
     * 删除所选数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param label       列表对应的统计信息展示栏
     * @param contextMenu 右键菜单集合
     * @param unit        统计信息展示栏数量单位
     */
    public static <T> void buildDeleteDataMenuItem(TableView<T> tableView, Label label, ContextMenu contextMenu, String unit) {
        MenuItem deleteDataMenuItem = new MenuItem("删除所选数据");
        deleteDataMenuItem.setOnAction(event -> {
            List<T> ts = tableView.getSelectionModel().getSelectedItems();
            ObservableList<T> items = tableView.getItems();
            items.removeAll(ts);
            label.setText(text_allHave + items.size() + unit);
        });
        contextMenu.getItems().add(deleteDataMenuItem);
    }

    /**
     * 为列表添加右键菜单并设置可选择多行
     *
     * @param contextMenu 右键菜单
     * @param tableView   要处理的列表
     */
    public static <T> void setContextMenu(ContextMenu contextMenu, TableView<T> tableView) {
        setContextMenu(contextMenu, tableView, SelectionMode.MULTIPLE);
    }

    /**
     * 为列表添加右键菜单
     *
     * @param contextMenu   右键菜单
     * @param tableView     要处理的列表
     * @param selectionMode 选中模式
     */
    public static <T> void setContextMenu(ContextMenu contextMenu, TableView<T> tableView, SelectionMode selectionMode) {
        // 设置是否可以选中多行
        tableView.getSelectionModel().setSelectionMode(selectionMode);
        tableView.setOnMousePressed(event -> {
            // 点击位置判断
            Node source = event.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || ((TableRow<?>) source).isEmpty()) {
                tableView.getSelectionModel().clearSelection();
                tableView.setContextMenu(null);
            } else if (event.isSecondaryButtonDown()) {
                if (CollectionUtils.isNotEmpty(tableView.getSelectionModel().getSelectedItems())) {
                    tableView.setContextMenu(contextMenu);
                } else {
                    tableView.setContextMenu(null);
                }
            }
        });
    }

    /**
     * 改变要防重复点击的组件状态
     *
     * @param taskBean 包含防重复点击组件列表的taskBean
     * @param disable  可点击状态，true设置为不可点击，false设置为可点击
     */
    public static void changeDisableNodes(TaskBean<?> taskBean, boolean disable) {
        List<Node> disableNodes = taskBean.getDisableNodes();
        changeDisableNodes(disableNodes, disable);
    }

    /**
     * 改变要防重复点击的组件状态
     *
     * @param disableNodes 防重复点击组件列表
     * @param disable      可点击状态，true设置为不可点击，false设置为可点击
     */
    public static void changeDisableNodes(List<Node> disableNodes, boolean disable) {
        if (CollectionUtils.isNotEmpty(disableNodes)) {
            disableNodes.forEach(dc -> dc.setDisable(disable));
        }
    }

    /**
     * 为配置组件设置上次配置值
     *
     * @param control 需要处理的组件
     * @param prop    配置文件
     * @param key     要读取的key
     */
    @SuppressWarnings("unchecked")
    public static void setControlLastConfig(Control control, Properties prop, String key) {
        String lastValue = prop.getProperty(key);
        if (StringUtils.isNotBlank(lastValue)) {
            if (control instanceof ChoiceBox) {
                ChoiceBox<String> choiceBox = (ChoiceBox<String>) control;
                choiceBox.setValue(lastValue);
            } else if (control instanceof CheckBox checkBox) {
                checkBox.setSelected(activation.equals(lastValue));
            } else if (control instanceof Label label) {
                label.setText(lastValue);
            } else if (control instanceof TextField textField) {
                textField.setText(lastValue);
            } else if (control instanceof Slider slider) {
                slider.setValue(Double.parseDouble(lastValue));
            }
        }
    }

    /**
     * 为路径文本框设置上次配置值
     *
     * @param label 需要处理的文本框
     * @param prop  配置文件
     * @param key   要读取的key
     */
    public static void setControlLastConfig(Label label, Properties prop, String key) {
        String lastValue = prop.getProperty(key);
        if (FilenameUtils.getPrefixLength(lastValue) != 0) {
            setPathLabel(label, lastValue, false);
        }
    }

    /**
     * 为颜色选择器设置上次配置值
     *
     * @param colorPicker    颜色选择器
     * @param prop           配置文件
     * @param colorKey       选中的颜色key
     * @param colorCustomKey 保存的自定义颜色key
     */
    public static void setColorPickerConfig(ColorPicker colorPicker, Properties prop, String colorKey, String colorCustomKey) {
        String selectColor = prop.getProperty(colorKey);
        if (StringUtils.isNotBlank(selectColor)) {
            colorPicker.setValue(Color.web(selectColor));
        }
        String colorCustom = prop.getProperty(colorCustomKey);
        if (StringUtils.isNotBlank(colorCustom)) {
            String[] colors = colorCustom.split(" ");
            for (String color : colors) {
                colorPicker.getCustomColors().add(Color.web(color));
            }
        }
    }

    /**
     * 显示可打开的文件类路径
     *
     * @param pathLabel 文件路径文本栏
     * @param path      文件路径
     * @param openFile  点击是否打开文件，true打开文件，false打开文件所在文件夹
     * @throws RuntimeException io异常
     */
    public static void setPathLabel(Label pathLabel, String path, boolean openFile) {
        pathLabel.setText(path);
        File file = new File(path);
        String openText = "\n鼠标左键点击打开 ";
        if (!file.exists()) {
            pathLabel.getStyleClass().add("label-err-style");
            openText = "\n文件不存在，鼠标左键点击打开 ";
        } else {
            pathLabel.getStyleClass().add("label-button-style");
        }
        String openPath;
        // 判断是否打开文件
        if (!openFile && file.isFile()) {
            openPath = file.getParent();
        } else {
            openPath = path;
        }
        pathLabel.setOnMouseClicked(event -> {
            // 只接受左键点击
            if (event.getButton() == MouseButton.PRIMARY) {
                try {
                    // 判断是否打开文件
                    if (!openFile) {
                        openDirectory(path);
                    } else {
                        openFile(path);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        addToolTip(path + openText + openPath, pathLabel);
        // 设置右键菜单
        setPathLabelContextMenu(pathLabel);
    }

    /**
     * 给路径Label设置右键菜单
     *
     * @param valueLabel 要处理的文本栏
     * @throws RuntimeException io异常
     */
    public static void setPathLabelContextMenu(Label valueLabel) {
        String path = valueLabel.getText();
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openDirectoryMenuItem = new MenuItem("打开文件夹");
        openDirectoryMenuItem.setOnAction(event -> {
            try {
                openDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        contextMenu.getItems().add(openDirectoryMenuItem);
        if (new File(path).isFile()) {
            MenuItem openFileMenuItem = new MenuItem("打开文件");
            openFileMenuItem.setOnAction(event -> {
                try {
                    openFile(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            contextMenu.getItems().add(openFileMenuItem);
        }
        MenuItem copyValueMenuItem = new MenuItem("复制路径");
        contextMenu.getItems().add(copyValueMenuItem);
        valueLabel.setContextMenu(contextMenu);
        copyValueMenuItem.setOnAction(event -> copyText(valueLabel.getText()));
        valueLabel.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(valueLabel, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 添加复制Label值右键菜单
     *
     * @param valueLabel 要处理的文本栏
     * @param text       右键菜单文本
     */
    public static void setCopyValueContextMenu(Label valueLabel, String text) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyValueMenuItem = new MenuItem(text);
        contextMenu.getItems().add(copyValueMenuItem);
        valueLabel.setContextMenu(contextMenu);
        valueLabel.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(valueLabel, event.getScreenX(), event.getScreenY());
            }
        });
        // 设置右键菜单行为
        copyValueMenuItem.setOnAction(event -> copyText(valueLabel.getText()));
    }

    /**
     * 复制文本
     *
     * @param value 要复制的文本
     */
    public static void copyText(String value) {
        // 获取当前系统剪贴板
        Clipboard clipboard = Clipboard.getSystemClipboard();
        // 创建剪贴板内容对象
        ClipboardContent content = new ClipboardContent();
        // 将文本区域选中的文本放入剪贴板内容中
        content.putString(value);
        // 设置剪贴板内容
        clipboard.setContent(content);
        // 复制成功消息气泡
        new MessageBubble(text_copySuccess, 2);
    }

    /**
     * 指定组件设置右对齐
     *
     * @param hBox           组件所在hBox
     * @param alignmentWidth 右对齐参考组件宽度
     * @param control        要设置右对齐的组件
     */
    public static void nodeRightAlignment(HBox hBox, double alignmentWidth, Control control) {
        ObservableList<Node> nodes = hBox.getChildren();
        double spacing = hBox.getSpacing();
        double prefWidth = alignmentWidth - spacing;
        for (Node node : nodes) {
            if (!control.getId().equals(node.getId())) {
                prefWidth = prefWidth - node.getLayoutBounds().getWidth() - spacing;
            }
        }
        control.setPrefWidth(prefWidth);
    }

    /**
     * 保存多选框选择设置
     *
     * @param checkBox   更改配置的选项框
     * @param configFile 要更新的配置文件相对路径
     * @param key        要更新的配置
     * @throws IOException io异常
     */
    public static void setLoadLastConfigCheckBox(CheckBox checkBox, String configFile, String key) throws IOException {
        if (checkBox.isSelected()) {
            updateProperties(configFile, key, activation);
        } else {
            updateProperties(configFile, key, unActivation);
        }
    }

    /**
     * 获取当前所在屏幕
     *
     * @return 当前所在屏幕
     */
    public static Screen getCurrentScreen(Stage floatingStage) {
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getBounds();
            if (bounds.contains(floatingStage.getX(), floatingStage.getY())) {
                return screen;
            }
        }
        // 默认返回主屏幕
        return Screen.getPrimary();
    }

    /**
     * 设置浮窗跟随鼠标移动
     *
     * @param floatingStage 浮窗
     * @param mousePoint    鼠标位置
     * @param offsetX       x轴偏移量
     * @param offsetY       y轴偏移量
     */
    public static void floatingMove(Stage floatingStage, Point mousePoint, int offsetX, int offsetY) {
        // 获取当前所在屏幕
        Screen currentScreen = getCurrentScreen(floatingStage);
        Rectangle2D screenBounds = currentScreen.getBounds();
        double width = floatingStage.getWidth();
        double height = floatingStage.getHeight();
        double mousePointX = mousePoint.getX();
        double mousePointY = mousePoint.getY();
        double x = mousePointX + offsetX;
        double borderX = x + width;
        if (borderX > screenBounds.getMaxX()) {
            x = mousePointX - offsetX - width;
        }
        if (offsetX < 0) {
            x = mousePointX - offsetX - width;
            if (x < screenBounds.getMinX()) {
                x = mousePointX + offsetX;
            }
        }
        double y = mousePointY + offsetY;
        double borderY = y + height;
        if (borderY > screenBounds.getMaxY()) {
            y = mousePointY - offsetY - height;
        }
        if (offsetY < 0) {
            y = mousePointY - offsetY - height;
            if (y < screenBounds.getMinY()) {
                y = mousePointY + offsetY + height;
            }
        }
        floatingStage.setX(x);
        floatingStage.setY(y);
    }

    /**
     * 将程序窗口弹出
     *
     * @param stage 程序主舞台
     */
    public static void showStage(Stage stage) {
        stage.setIconified(false);
        stage.setAlwaysOnTop(true);
        stage.setAlwaysOnTop(false);
        stage.requestFocus();
    }

}
