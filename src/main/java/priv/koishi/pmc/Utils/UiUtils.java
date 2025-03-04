package priv.koishi.pmc.Utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.MainApplication;
import priv.koishi.pmc.MessageBubble.MessageBubble;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * @param controls 需要显示提示框的组件
     * @param tip      提示卡信息
     */
    public static void addToolTip(String tip, Control... controls) {
        for (Control control : controls) {
            control.setTooltip(creatTooltip(tip));
        }
    }

    /**
     * 设置鼠标停留提示框参数
     *
     * @param tip 提示文案
     * @return 设置参数后的Tooltip对象
     */
    public static Tooltip creatTooltip(String tip) {
        Tooltip tooltip = new Tooltip(tip);
        tooltip.setWrapText(true);
        tooltip.setShowDuration(showDuration);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        tooltip.getStyleClass().add("tooltip-font-size");
        return tooltip;
    }

    /**
     * 输入框鼠标停留提示输入值
     *
     * @param textField 要添加提示的文本输入框
     * @param text      要展示的提示文案
     * @param valueText 当前所填值提示文案
     */
    public static void addValueToolTip(TextField textField, String text, String valueText) {
        String value = textField.getText();
        if (StringUtils.isNotEmpty(text)) {
            if (StringUtils.isNotEmpty(value)) {
                addToolTip(text + "\n" + valueText + value, textField);
            } else {
                addToolTip(text, textField);
            }
        } else {
            if (StringUtils.isNotEmpty(value)) {
                addToolTip(value, textField);
            } else {
                textField.setTooltip(null);
            }
        }
    }

    /**
     * 创建一个文件选择器
     *
     * @param event            交互事件
     * @param path             文件选择器初始路径
     * @param extensionFilters 要过滤的文件格式
     * @param title            文件选择器标题
     * @return 文件选择器选择的文件
     */
    public static File creatFileChooser(ActionEvent event, String path, List<FileChooser.ExtensionFilter> extensionFilters, String title) {
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
        return fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
    }

    /**
     * 创建一个文件夹选择器
     *
     * @param event 交互事件
     * @param path  文件夹选择器初始路径
     * @param title 文件夹选择器标题
     * @return 文件夹选择器选择的文件夹
     */
    public static File creatDirectoryChooser(ActionEvent event, String path, String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        // 设置初始目录
        if (StringUtils.isBlank(path) || !new File(path).isDirectory()) {
            directoryChooser.setInitialDirectory(new File(userHome));
        } else {
            directoryChooser.setInitialDirectory(new File(path));
        }
        return directoryChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
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
     * 设置默认字符串值
     *
     * @param textField    要设置默认字符串的文本输入框
     * @param defaultValue 默认字符串
     * @return 文本输入框不为空则返回所填值，为空则为默认值
     */
    public static String setDefaultStrValue(TextField textField, String defaultValue) {
        String valueStr = textField.getText();
        String value = defaultValue;
        if (StringUtils.isNotBlank(valueStr)) {
            value = valueStr;
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
        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource("icon/Tools.png")).toString()));
        // 创建展示异常信息的TextArea
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(errString);
        // 创建VBox并添加TextArea
        VBox details = new VBox();
        details.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> textArea.setPrefHeight(details.getHeight())));
        details.getChildren().add(textArea);
        alert.getDialogPane().setExpandableContent(details);
        return alert;
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
     * @param dataList  javafx表格要展示的数据
     * @param tabId     用于区分不同列表的id，要展示的数据bean属性名加上tabId即为javafx列表的列对应的id
     */
    public static <T> void autoBuildTableViewData(TableView<T> tableView, List<T> dataList, String tabId) {
        Class<?> beanClass = dataList.getFirst().getClass();
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
            Optional<? extends TableColumn<?, ?>> matched = columns.stream().filter(c -> c.getId().equals(finalFieldName)).findFirst();
            matched.ifPresent(m -> buildCellValue(m, fieldName));
        });
        ObservableList<T> data = FXCollections.observableArrayList(dataList);
        tableView.setItems(data);
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
        updateLabel(log, "");
        System.gc();
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
     * @param anchorPane       组件所在布局
     * @return 所选文件路径
     * @throws IOException io异常
     */
    public static String updatePathLabel(String selectedFilePath, String filePath, String pathKey, Label pathLabel, String configFile, AnchorPane anchorPane) throws IOException {
        // 只有跟上次选的路径不一样才更新
        if (StringUtils.isBlank(filePath) || !filePath.equals(selectedFilePath)) {
            updateProperties(configFile, pathKey, selectedFilePath);
            filePath = selectedFilePath;
        }
        setPathLabel(pathLabel, selectedFilePath, false, anchorPane);
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
     * 所选行上移一行选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static <T> void buildUpMoveDataMenuItem(TableView<T> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("所选行上移一行");
        menuItem.setOnAction(event -> {
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
        });
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
        menuItem.setOnAction(event -> {
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
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 修改操作类型
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildEditClickType(TableView<ClickPositionBean> tableView, ContextMenu contextMenu) {
        Menu menuItem = new Menu("更改操作类型");
        // 创建二级菜单项
        MenuItem primary = new MenuItem("鼠标左键点击");
        MenuItem secondary = new MenuItem("鼠标右键点击");
        MenuItem middle = new MenuItem("鼠标中键点击");
        MenuItem forward = new MenuItem("鼠标前侧键点击");
        MenuItem back = new MenuItem("鼠标后侧键点击");
        MenuItem none = new MenuItem("鼠标仅移动");
        // 为每个菜单项添加事件处理
        primary.setOnAction(event -> updateClickType(tableView, "鼠标左键点击"));
        secondary.setOnAction(event -> updateClickType(tableView, "鼠标右键点击"));
        middle.setOnAction(event -> updateClickType(tableView, "鼠标中键点击"));
        forward.setOnAction(event -> updateClickType(tableView, "鼠标前侧键点击"));
        back.setOnAction(event -> updateClickType(tableView, "鼠标后侧键点击"));
        none.setOnAction(event -> updateClickType(tableView, "鼠标仅移动"));
        menuItem.getItems().addAll(primary, secondary, middle, forward, back, none);
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 修改操作类型二级菜单选项
     *
     * @param tableView 要添加右键菜单的列表
     * @param clickType 操作类型
     */
    private static void updateClickType(TableView<ClickPositionBean> tableView, String clickType) {
        List<ClickPositionBean> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            selectedItem.forEach(bean -> {
                bean.setType(clickType);
                tableView.refresh();
            });
        }
    }

    /**
     * 删除所选数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param label       列表对应的统计信息展示栏
     * @param contextMenu 右键菜单集合
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
     * 改变要防重复点击的组件状态
     *
     * @param taskBean 包含防重复点击组件列表的taskBean
     * @param disable  可点击状态，true设置为不可点击，false设置为可点击
     */
    public static void changeDisableControls(TaskBean<?> taskBean, boolean disable) {
        List<Control> disableControls = taskBean.getDisableControls();
        changeDisableControls(disableControls, disable);
    }

    /**
     * 改变要防重复点击的组件状态
     *
     * @param disableControls 防重复点击组件列表
     * @param disable         可点击状态，true设置为不可点击，false设置为可点击
     */
    public static void changeDisableControls(List<Control> disableControls, boolean disable) {
        if (CollectionUtils.isNotEmpty(disableControls)) {
            disableControls.forEach(dc -> dc.setDisable(disable));
        }
    }

    /**
     * 为配置组件设置上次配置值
     *
     * @param control    需要处理的组件
     * @param prop       配置文件
     * @param key        要读取的key
     * @param canBlank   组件所填文本是否可为空格，ture可填写空格，false不可填写空格
     * @param anchorPane 组件所在布局
     */
    @SuppressWarnings("unchecked")
    public static void setControlLastConfig(Control control, Properties prop, String key, boolean canBlank, AnchorPane anchorPane) {
        String lastValue = prop.getProperty(key);
        if (StringUtils.isNotBlank(lastValue)) {
            if (control instanceof ChoiceBox) {
                ChoiceBox<String> choiceBox = (ChoiceBox<String>) control;
                choiceBox.setValue(lastValue);
            } else if (control instanceof CheckBox checkBox) {
                checkBox.setSelected(activation.equals(lastValue));
            } else if (control instanceof Label label) {
                if (isValidPath(lastValue)) {
                    setPathLabel(label, lastValue, false, anchorPane);
                } else {
                    label.setText(lastValue);
                }
            } else if (control instanceof TextField textField) {
                textField.setText(lastValue);
            }
        } else if (StringUtils.isNotEmpty(lastValue) && canBlank) {
            if (control instanceof TextField textField) {
                textField.setText(lastValue);
            }
        }
    }

    /**
     * 显示可打开的文件类路径
     *
     * @param pathLabel  文件路径文本栏
     * @param path       文件路径
     * @param openFile   点击是否打开文件，true打开文件，false打开文件所在文件夹
     * @param anchorPane 组件所在布局
     * @throws RuntimeException io异常
     */
    public static void setPathLabel(Label pathLabel, String path, boolean openFile, AnchorPane anchorPane) {
        pathLabel.setText(path);
        pathLabel.getStyleClass().add("label-button-style");
        File file = new File(path);
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
        addToolTip(path + "\n鼠标左键点击打开 " + openPath, pathLabel);
        // 设置右键菜单
        setPathLabelContextMenu(pathLabel, anchorPane);
    }

    /**
     * 给路径Label设置右键菜单
     *
     * @param valueLabel 要处理的文本栏
     * @param anchorPane 组件所在布局
     * @throws RuntimeException io异常
     */
    public static void setPathLabelContextMenu(Label valueLabel, AnchorPane anchorPane) {
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
        copyValueMenuItem.setOnAction(event -> copyText(valueLabel.getText(), anchorPane));
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
     * @param anchorPane 组件所在布局
     */
    public static void setCopyValueContextMenu(Label valueLabel, String text, AnchorPane anchorPane) {
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
        copyValueMenuItem.setOnAction(event -> copyText(valueLabel.getText(), anchorPane));
    }

    /**
     * 复制文本
     *
     * @param value      要复制的文本
     * @param anchorPane 组件所在布局
     */
    public static void copyText(String value, AnchorPane anchorPane) {
        // 获取当前系统剪贴板
        Clipboard clipboard = Clipboard.getSystemClipboard();
        // 创建剪贴板内容对象
        ClipboardContent content = new ClipboardContent();
        // 将文本区域选中的文本放入剪贴板内容中
        content.putString(value);
        // 设置剪贴板内容
        clipboard.setContent(content);
        // 复制成功消息气泡
        buildMessageBubble(anchorPane, text_copySuccess, 1);
    }

    /**
     * 创建消息弹窗
     *
     * @param anchorPane 组件所在布局
     * @param text       消息弹窗提示文案
     * @param time       显示弹窗时间
     */
    public static void buildMessageBubble(AnchorPane anchorPane, String text, double time) {
        MessageBubble bubble = new MessageBubble(text);
        anchorPane.getChildren().add(bubble);
        // 列表中无法监控鼠标位置需要判断是否监控到鼠标移动
        AtomicBoolean getMouseMoved = new AtomicBoolean(false);
        anchorPane.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            // 获取鼠标位置
            getMouseMoved.set(true);
            double mouseX = mouseEvent.getX();
            double mouseY = mouseEvent.getY();
            bubble.setLayoutX(mouseX + 30);
            bubble.setLayoutY(mouseY);
        });
        // 鼠标在列表时设置初位置
        if (!getMouseMoved.get()) {
            bubble.setLayoutX(anchorPane.getWidth() * 0.5);
            bubble.setLayoutY(anchorPane.getHeight() * 0.5);
        }
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(time), ae -> anchorPane.getChildren().remove(bubble));
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
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
     * 将Tab按照id排序
     *
     * @param tabs 要排序的tab
     * @param ids  tab对应的id列表
     * @return 排序后的tab
     */
    public static ObservableList<Tab> sortTabsByIds(ObservableList<Tab> tabs, List<String> ids) {
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            orderMap.put(ids.get(i), i);
        }
        tabs.sort(Comparator.comparingInt(tab -> orderMap.getOrDefault(tab.getId(), Integer.MAX_VALUE)));
        return tabs;
    }

}
