package priv.koishi.pmc.Utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.Annotate.UsedByReflection;
import priv.koishi.pmc.Bean.CheckUpdateBean;
import priv.koishi.pmc.Bean.Config.FileChooserConfig;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.FileVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Bean.VO.Indexable;
import priv.koishi.pmc.Controller.FileChooserController;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor;
import priv.koishi.pmc.MainApplication;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static priv.koishi.pmc.Controller.SettingController.windowInfoFloating;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.showFloatingWindow;
import static priv.koishi.pmc.Utils.CommonUtils.*;
import static priv.koishi.pmc.Utils.FileUtils.*;

/**
 * ui相关工具类
 *
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:38
 */
public class UiUtils {

    /**
     * 拖拽数据格式
     */
    private static final DataFormat dataFormat = new DataFormat("application/x-java-serialized-object");

    /**
     * 日志记录器
     */
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
     * @return 设置参数后的 Tooltip 对象
     */
    public static Tooltip creatTooltip(String tip) {
        return creatTooltip(tip, Duration.INDEFINITE);
    }

    /**
     * 设置鼠标停留提示框参数
     *
     * @param tip      提示文案
     * @param duration 显示时长
     * @return 设置参数后的 Tooltip 对象
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
     */
    public static void addValueToolTip(TextField textField, String text) {
        addValueToolTip(textField, text, text_nowValue());
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
     * @param node  要添加提示的组件
     * @param text  提示文案
     * @param value 当前所填值
     */
    public static void addValueToolTip(Node node, String text, String value) {
        addValueToolTip(node, text, text_nowValue(), value);
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
     * @param path             文件选择器初始路径
     * @param extensionFilters 要过滤的文件格式
     * @param title            文件选择器标题
     * @return 文件选择器选择的文件
     */
    public static FileChooser creatFileChooser(String path, List<FileChooser.ExtensionFilter> extensionFilters, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        // 设置初始目录
        File file = getExistsFile(path);
        // 设置初始目录
        if (file.isDirectory()) {
            fileChooser.setInitialDirectory(file);
        } else if (file.isFile()) {
            file = new File(file.getParent());
            fileChooser.setInitialDirectory(file);
        }
        // 设置过滤条件
        if (CollectionUtils.isNotEmpty(extensionFilters)) {
            fileChooser.getExtensionFilters().addAll(extensionFilters);
        }
        return fileChooser;
    }

    /**
     * 创建一个单文件选择器
     *
     * @param window           文件选择器窗口
     * @param path             文件选择器初始路径
     * @param extensionFilters 要过滤的文件格式
     * @param title            文件选择器标题
     * @return 文件选择器选择的文件
     */
    public static File creatFileChooser(Window window, String path, List<FileChooser.ExtensionFilter> extensionFilters, String title) {
        FileChooser fileChooser = creatFileChooser(path, extensionFilters, title);
        return fileChooser.showOpenDialog(window);
    }

    /**
     * 创建一个多文件选择器
     *
     * @param window           文件选择器窗口
     * @param path             文件选择器初始路径
     * @param extensionFilters 要过滤的文件格式
     * @param title            文件选择器标题
     * @return 文件选择器选择的文件
     */
    public static List<File> creatFilesChooser(Window window, String path, List<FileChooser.ExtensionFilter> extensionFilters, String title) {
        FileChooser fileChooser = creatFileChooser(path, extensionFilters, title);
        return fileChooser.showOpenMultipleDialog(window);
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
        File file = getExistsFile(path);
        // 设置初始目录
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }
        directoryChooser.setInitialDirectory(file);
        return directoryChooser.showDialog(window);
    }

    /**
     * 创建一个多图片选择器（只支持 png、jpg、jpeg 格式）
     *
     * @param window        文件选择器窗口
     * @param imgSelectPath 默认路径
     * @return 选择的图片
     */
    public static List<File> creatImgFilesChooser(Window window, String imgSelectPath) {
        List<FileChooser.ExtensionFilter> extensionFilters = creatImgExtensionFilter();
        return creatFilesChooser(window, imgSelectPath, extensionFilters, text_selectTemplateImg());
    }

    /**
     * 创建一个单图片选择器（只支持 png、jpg、jpeg 格式）
     *
     * @param window        文件选择器窗口
     * @param imgSelectPath 默认路径
     * @return 选择的图片
     */
    public static File creatImgFileChooser(Window window, String imgSelectPath) {
        List<FileChooser.ExtensionFilter> extensionFilters = creatImgExtensionFilter();
        return creatFileChooser(window, imgSelectPath, extensionFilters, text_selectTemplateImg());
    }

    /**
     * 创建图片格式过滤器
     *
     * @return 图片格式过滤器
     */
    private static List<FileChooser.ExtensionFilter> creatImgExtensionFilter() {
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
        extensionFilters.add(new FileChooser.ExtensionFilter(text_image(), allImageType));
        extensionFilters.add(new FileChooser.ExtensionFilter(png, allPng));
        extensionFilters.add(new FileChooser.ExtensionFilter(jpg, allJpg));
        extensionFilters.add(new FileChooser.ExtensionFilter(jpeg, allJpeg));
        return extensionFilters;
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
        Throwable cause = ex.getCause();
        String message;
        if (cause instanceof RuntimeException) {
            message = cause.getMessage();
        } else {
            if (cause != null) {
                cause = cause.getCause();
            }
            if (cause != null) {
                if (cause instanceof Exception) {
                    message = cause.getMessage();
                } else {
                    message = ex.getMessage();
                }
            } else {
                message = ex.getMessage();
            }
        }
        if (message.length() > 200 && !message.contains("\n")) {
            message = message.substring(0, 200) + " ...";
        }
        alert.setHeaderText(message);
        // 展示弹窗
        Platform.runLater(alert::show);
    }

    /**
     * 创建一个错误弹窗
     *
     * @param errString 要展示的异常信息
     * @return Alert弹窗对象
     */
    public static Alert creatErrorAlert(String errString) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(text_abnormal());
        DialogPane dialogPane = alert.getDialogPane();
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        setWindowLogo(stage, logoPath);
        // 创建展示异常信息的TextArea
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(errString);
        // 创建VBox并添加TextArea
        VBox details = new VBox();
        VBox.setVgrow(textArea, Priority.ALWAYS);
        textArea.setMaxHeight(Double.MAX_VALUE);
        details.getChildren().add(textArea);
        dialogPane.setExpandableContent(details);
        return alert;
    }

    /**
     * 创建一个确认弹窗
     *
     * @param title   确认弹窗标题
     * @param confirm 确认框文案
     * @param ok      确认按钮文案
     * @param cancel  取消按钮文案
     * @return 被点击的按钮
     */
    public static ButtonType creatConfirmDialog(String title, String confirm, String ok, String cancel) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(confirm);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        setWindowLogo(stage, logoPath);
        ButtonType okButton = new ButtonType(ok, ButtonBar.ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
        return dialog.showAndWait().orElse(cancelButton);
    }

    /**
     * 给窗口设置 logo
     *
     * @param stage 要设置 logo 的窗口
     * @param path  logo 路径
     */
    public static void setWindowLogo(Stage stage, String path) {
        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource(path)).toString()));
    }

    /**
     * 设置窗口 css 样式
     *
     * @param scene     要设置样式的场景
     * @param stylesCss css 文件路径
     */
    public static void setWindowCss(Scene scene, String stylesCss) {
        scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource(stylesCss)).toExternalForm());
    }

    /**
     * 为 javaFX 单元格赋值并添加鼠标悬停提示
     *
     * @param tableColumn 要处理的 javaFX 列表列
     * @param param       javaFX 列表列对应的数据属性名
     */
    public static void buildCellValue(TableColumn<?, ?> tableColumn, String param) {
        tableColumn.setCellValueFactory(new PropertyValueFactory<>(param));
        // 为 javaFX 单元格和表头添加鼠标悬停提示
        addTableCellToolTip(tableColumn);
    }

    /**
     * 自定义单元格工厂，为单元格添加 Tooltip
     *
     * @param column 要处理的 javaFX 表格单元格
     * @param <S>    表格单元格数据类型
     * @param <T>    表格单元格类型
     */
    public static <S, T> void addTableCellToolTip(TableColumn<S, T> column) {
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
     * 为表头添加鼠标悬停提示
     *
     * @param column 要处理的 javaFX 表格列
     * @param <S>    表格单元格数据类型
     * @param <T>    表格单元格类型
     */
    public static <S, T> void addTableColumnToolTip(TableColumn<S, T> column) {
        addTableColumnToolTip(column, column.getText());
    }

    /**
     * 为表头添加鼠标悬停提示
     *
     * @param column  要处理的 javaFX 表格列
     * @param tooltip 要展示的提示文案
     * @param <S>     表格单元格数据类型
     * @param <T>     表格单元格类型
     */
    public static <S, T> void addTableColumnToolTip(TableColumn<S, T> column, String tooltip) {
        String columnText = column.getText();
        if (StringUtils.isNotBlank(columnText)) {
            Label label = new Label(columnText);
            label.setMaxWidth(column.getMaxWidth());
            addToolTip(tooltip, label);
            column.setGraphic(label);
            column.setText(null);
        }
    }

    /**
     * 根据 bean 属性名自动填充 javaFX 表格
     *
     * @param tableView   要处理的 javaFX 表格
     * @param beanClass   要处理的 javaFX 表格的数据 bean 类
     * @param tabId       用于区分不同列表的 id，要展示的数据 bean 属性名加上 tabId 即为 javaFX 列表的列对应的 id
     * @param indexColumn 序号列
     * @param <T>         要处理的 javaFX 表格的数据 bean 类
     */
    @SuppressWarnings("unchecked")
    public static <T> void autoBuildTableViewData(TableView<T> tableView, Class<?> beanClass, String tabId, TableColumn<T, Integer> indexColumn) {
        // 递归获取类及其父类的所有字段
        List<Field> fields = getAllFields(beanClass);
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
                    finalFieldName.equals(c.getId())).findFirst();
            matched.ifPresent(m -> {
                // 添加列名Tooltip
                addTableColumnToolTip(m);
                if (f.getType() == Image.class) {
                    try {
                        Method getter = beanClass.getMethod("loadThumb");
                        // 显式标记方法调用（解决IDE误报）
                        if (getter.isAnnotationPresent(UsedByReflection.class)) {
                            Function<T, Image> supplier = bean -> {
                                try {
                                    return (Image) getter.invoke(bean);
                                } catch (Exception e) {
                                    return null;
                                }
                            };
                            // 创建图片表格
                            buildThumbnailCell((TableColumn<T, Image>) m, supplier);
                        }
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    if (indexColumn != null && m.getId().equals(indexColumn.getId())) {
                        // 设置列为序号列
                        buildIndexCellValue(indexColumn);
                    } else if (beanClass == ImgFileVO.class && "path".equals(fieldName)) {
                        TableColumn<ImgFileVO, String> pathColumn = (TableColumn<ImgFileVO, String>) m;
                        pathColumn.setCellValueFactory(cellData ->
                                cellData.getValue().pathProperty());
                        // 为 javaFX 单元格和表头添加鼠标悬停提示
                        addTableCellToolTip(pathColumn);
                    } else {
                        // 为 javaFX 单元格赋值并添加鼠标悬停提示
                        buildCellValue(m, fieldName);
                    }
                }
            });
        });
    }

    /**
     * 设置列为序号列
     *
     * @param column 要处理的列
     * @param <T>    列对应的数据类型
     */
    public static <T> void buildIndexCellValue(TableColumn<T, Integer> column) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<T, Integer> call(TableColumn<T, Integer> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            // 获取当前行的索引并加1（行号从1开始）
                            int rowIndex = getIndex() + 1;
                            T itemData = getTableRow().getItem();
                            if (itemData instanceof Indexable indexable) {
                                indexable.setIndex(rowIndex);
                            }
                            setText(String.valueOf(rowIndex));
                            setTooltip(creatTooltip(String.valueOf(rowIndex)));
                        }
                    }
                };
            }
        });
    }

    /**
     * 递归获取类及其父类的所有字段
     *
     * @param clazz 要获取字段的类
     * @return 当前类和父类所有字段
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 创建图片表格
     *
     * @param column        要创建图片表格的列
     * @param thumbSupplier 获取图片的函数
     * @param <T>           列对应的数据类型
     */
    public static <T> void buildThumbnailCell(TableColumn<T, Image> column, Function<? super T, ? extends Image> thumbSupplier) {
        column.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(thumbSupplier.apply(cellData.getValue())));
        column.setCellFactory(_ -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);
                setTextFill(Color.BLACK);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (image == null) {
                    TableRow<T> tableRow = getTableRow();
                    if (isRedText(tableRow)) {
                        setText(text_badImg());
                        setTextFill(Color.RED);
                        setTooltip(creatTooltip(text_badImg()));
                    } else {
                        setText(text_noImg());
                        setTooltip(creatTooltip(text_noImg()));
                    }
                    setGraphic(null);
                } else {
                    setText(null);
                    imageView.setImage(image);
                    setGraphic(imageView);
                    setTooltip(creatTooltip(image.getUrl().replace("file:", text_imgPath())));
                }
            }

            // 判断字体是否变红，true 变红，false 不变红
            private boolean isRedText(TableRow<? extends T> tableRow) {
                T bean = tableRow.getItem();
                String imgPath = null;
                if (bean instanceof ImgFileVO imgFileVO) {
                    imgPath = imgFileVO.getPath();
                } else if (bean instanceof ClickPositionVO clickPositionVO) {
                    imgPath = clickPositionVO.getClickImgPath();
                }
                // 只有在有图片路径但图片不存在时，才让缩略图提示文字变红
                if (StringUtils.isBlank(imgPath)) {
                    return false;
                }
                return !new File(imgPath).exists();
            }
        });
    }

    /**
     * 清空 javaFX 列表数据
     *
     * @param tableView  要清空的 javaFX 列表
     * @param fileNumber 用于展示列表数据数量的文本框
     * @param <T>        数据类型
     */
    public static <T> void removeTableViewData(TableView<T> tableView, Label fileNumber) {
        tableView.getItems().clear();
        updateLabel(fileNumber, listText_dataListNull());
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
     * @param <T>            数据类型
     */
    public static <T> void addData(List<? extends T> data, int addType, TableView<T> tableView, Label dataNumber, String dataNumberUnit) {
        addData(data, addType, tableView, dataNumber, dataNumberUnit, true);
    }

    /**
     * 向列表指定位置添加数据
     *
     * @param data           要添加的数据
     * @param addType        添加位置类型
     * @param tableView      要添加数据的列表
     * @param dataNumber     用于展示列表数据数量的文本框
     * @param dataNumberUnit 数据数量单位
     * @param selected       true 表示选中添加的数据，false 表示不选中添加的数据
     * @param <T>            数据类型
     */
    public static <T> void addData(List<? extends T> data, int addType, TableView<T> tableView, Label dataNumber,
                                   String dataNumberUnit, boolean selected) {
        ObservableList<T> tableViewItems = tableView.getItems();
        TableView.TableViewSelectionModel<T> selectionModel = tableView.getSelectionModel();
        List<T> selectedItem = selectionModel.getSelectedItems();
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
                if (selected) {
                    selectionModel.selectRange(selectedIndex, selectedIndex + data.size());
                }
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
                if (selected) {
                    selectionModel.selectRange(selectedIndex, selectedIndex + data.size());
                }
                break;
            }
            // 向列表第一行上方插入
            case topAdd: {
                // 向列表第一行追加数据
                tableView.getItems().addAll(0, data);
                // 滚动到插入位置
                tableView.scrollTo(0);
                // 选中新插入的数据
                if (selected) {
                    selectionModel.selectRange(0, data.size());
                }
                break;
            }
            // 向列表最后一行追加
            case append: {
                int lastIndex = tableViewItems.size();
                // 向列表最后一行追加数据
                tableViewItems.addAll(data);
                // 滚动到插入位置
                tableView.scrollTo(tableViewItems.size());
                // 选中新插入的数据
                if (selected) {
                    selectionModel.selectRange(lastIndex, lastIndex + data.size());
                }
                break;
            }
        }
        // 同步表格数据量
        updateTableViewSizeText(tableView, dataNumber, dataNumberUnit);
        tableView.refresh();
    }

    /**
     * 限制滑动条只能输入整数
     *
     * @param slider 要处理的滑动条
     * @param tip    鼠标悬停提示文案
     * @return 监听器
     */
    public static ChangeListener<Number> integerSliderValueListener(Slider slider, String tip) {
        ChangeListener<Number> listener = (_, _, newValue) -> {
            int rounded = newValue.intValue();
            slider.setValue(rounded);
            addValueToolTip(slider, tip, String.valueOf(rounded));
        };
        slider.valueProperty().addListener(listener);
        return listener;
    }

    /**
     * 限制输入框只能输入指定范围内的整数
     *
     * @param textField 要处理的文本输入框
     * @param min       可输入的最小值，为空则不限制
     * @param max       可输入的最大值，为空则不限制
     * @param tip       鼠标悬停提示文案
     * @return 监听器
     */
    public static ChangeListener<Boolean> integerRangeTextField(TextField textField, Integer min, Integer max, String tip) {
        ChangeListener<Boolean> listener = (_, oldFocused, newFocused) -> {
            if (oldFocused && !newFocused) {
                String newValue = textField.getText();
                if (!isInIntegerRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                    textField.setText("");
                    new MessageBubble(text_errRange(), 1);
                }
                addValueToolTip(textField, tip);
            }
        };
        textField.focusedProperty().addListener(listener);
        return listener;
    }

    /**
     * 限制输入框只能输入指定范围内的小数
     *
     * @param textField     要处理的文本输入框
     * @param min           可输入的最小值，为空则不限制
     * @param max           可输入的最大值，为空则不限制
     * @param decimalDigits 小数位数，0表示整数
     * @param tip           鼠标悬停提示文案
     * @return 监听器
     */
    public static ChangeListener<Boolean> DoubleRangeTextField(TextField textField, Double min, Double max,
                                                               int decimalDigits, String tip) {
        ChangeListener<Boolean> listener = (_, oldFocused, newFocused) -> {
            if (oldFocused && !newFocused) {
                String newValue = textField.getText();
                if (!isInDecimalRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                    textField.setText("");
                    new MessageBubble(text_errRange(), 1);
                } else if (newValue.contains(".")) {
                    textField.setText(newValue.substring(0, newValue.lastIndexOf(".") + decimalDigits + 1));
                }
                addValueToolTip(textField, tip);
            }
        };
        textField.focusedProperty().addListener(listener);
        return listener;
    }

    /**
     * 限制输入框只能输入指定范围内的正整数（范围外显示警告信息）
     *
     * @param textField   要处理的文本输入框
     * @param min         可输入的最小值
     * @param max         可输入的最大值，为空则不限制
     * @param tip         鼠标悬停提示文案
     * @param warningNode 警告节点
     * @return 监听器
     */
    public static ChangeListener<String> warnIntegerRangeTextField(TextField textField, Integer min, Integer max, String tip, Node warningNode) {
        ChangeListener<String> listener = (_, oldValue, newValue) -> {
            // 这里处理文本变化的逻辑
            if (!isInIntegerRange(newValue, 1, null) && StringUtils.isNotBlank(newValue)) {
                textField.setText(oldValue);
            } else if (!isInIntegerRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                textField.setStyle("-fx-text-fill: red;");
                warningNode.setVisible(true);
            } else {
                textField.setStyle("-fx-text-fill: black;");
                warningNode.setVisible(false);
            }
            addValueToolTip(textField, tip);
        };
        textField.textProperty().addListener(listener);
        return listener;
    }

    /**
     * 监听输入框内容变化
     *
     * @param textField 要监听的文本输入框
     * @param tip       鼠标悬停提示文案
     * @return 监听器
     */
    public static ChangeListener<String> textFieldValueListener(TextField textField, String tip) {
        ChangeListener<String> listener = (_, _, _) ->
                addValueToolTip(textField, tip);
        textField.textProperty().addListener(listener);
        return listener;
    }

    /**
     * 修改 label 信息
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
     * @param pathKey          配置文件中路径的 key
     * @param pathLabel        要展示路径的文本框
     * @param configFile       要更新的配置文件
     * @return 所选文件路径
     * @throws IOException 配置文件保存异常
     */
    public static String updatePathLabel(String selectedFilePath, String filePath, String pathKey, Label pathLabel, String configFile) throws IOException {
        // 只有跟上次选的路径不一样才更新
        if (StringUtils.isBlank(filePath) || !filePath.equals(selectedFilePath)) {
            updateProperties(configFile, pathKey, selectedFilePath);
            filePath = selectedFilePath;
        }
        if (pathLabel != null) {
            setPathLabel(pathLabel, selectedFilePath);
        }
        return filePath;
    }

    /**
     * 设置列表通过拖拽排序行
     *
     * @param tableView 要处理的列表
     * @param <T>       列表数据类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void tableViewDragRow(TableView<T> tableView) {
        tableView.setRowFactory(_ -> {
            TableRow<T> row = new TableRow<>();
            ObservableList<Integer> draggedIndices = FXCollections.observableArrayList();
            // 拖拽检测
            row.setOnDragDetected(e -> {
                if (!row.isEmpty()) {
                    // 获取所有选中的行索引
                    draggedIndices.setAll(tableView.getSelectionModel().getSelectedIndices().stream()
                            .sorted().collect(Collectors.toList()));
                    // 只允许非空且选中多行时拖拽
                    if (!draggedIndices.isEmpty()) {
                        Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                        db.setDragView(row.snapshot(null, null));
                        // 使用自定义数据格式存储多个索引
                        ClipboardContent cc = new ClipboardContent();
                        cc.put(dataFormat, new ArrayList<>(draggedIndices));
                        db.setContent(cc);
                        e.consume();
                    }
                }
            });
            // 拖拽悬停验证
            row.setOnDragOver(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dataFormat)) {
                    // 禁止拖拽到选中行内部
                    List<?> indices = (List<?>) db.getContent(dataFormat);
                    int dropIndex = row.isEmpty() ? tableView.getItems().size() : row.getIndex();
                    if (!indices.contains(dropIndex)) {
                        e.acceptTransferModes(TransferMode.MOVE);
                        e.consume();
                    }
                }
            });
            // 拖拽释放处理
            row.setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dataFormat)) {
                    List<Integer> indices = (List<Integer>) db.getContent(dataFormat);
                    int maxIndex = tableView.getItems().size();
                    int dropIndex = row.isEmpty() ? maxIndex : row.getIndex();
                    // 计算有效插入位置
                    int adjustedDropIndex = calculateAdjustedIndex(indices, dropIndex, maxIndex);
                    if (adjustedDropIndex != -1) {
                        // 批量移动数据
                        moveRows(tableView, indices, adjustedDropIndex);
                        // 更新选中状态
                        selectMovedRows(tableView, indices, adjustedDropIndex);
                        e.setDropCompleted(true);
                        e.consume();
                    } else {
                        // 确保拖拽失败
                        e.setDropCompleted(false);
                        // 消费事件以避免进一步传播
                        e.consume();
                    }
                }
            });
            return row;
        });
    }

    /**
     * 计算调整后的插入位置
     *
     * @param draggedIndices 被拖拽行的原始索引列表（需保证有序）
     * @param dropIndex      拖拽操作的目标放置位置原始索引
     * @param maxIndex       表格数据项总数
     * @return 调整后的有效插入位置，返回-1表示无效拖拽位置
     */
    private static int calculateAdjustedIndex(List<Integer> draggedIndices, int dropIndex, int maxIndex) {
        int firstDragged = draggedIndices.getFirst();
        int lastDragged = draggedIndices.getLast();
        if (dropIndex + 1 >= maxIndex) {
            return maxIndex - draggedIndices.size();
        }
        if (dropIndex >= firstDragged && dropIndex <= lastDragged) {
            return -1;
        }
        return dropIndex;
    }

    /**
     * 批量移动行数据
     *
     * @param tableView   目标表格视图对象
     * @param indices     需要移动的行索引列表（需保证有序）
     * @param targetIndex 移动的目标插入位置（经过调整后的有效位置）
     * @param <T>         表格数据项类型
     */
    private static <T> void moveRows(TableView<T> tableView, List<Integer> indices, int targetIndex) {
        ObservableList<T> items = tableView.getItems();
        List<T> movedItems = indices.stream().map(items::get).toList();
        // 批量操作减少刷新次数
        items.removeAll(movedItems);
        items.addAll(targetIndex, movedItems);
    }

    /**
     * 重新选中移动后的行
     *
     * @param tableView       目标表格视图对象
     * @param originalIndices 移动前的原始行索引列表
     * @param targetIndex     移动后的起始插入位置
     * @param <T>             表格数据项类型
     */
    private static <T> void selectMovedRows(TableView<T> tableView, List<Integer> originalIndices, int targetIndex) {
        tableView.getSelectionModel().clearSelection();
        for (int i = 0; i < originalIndices.size(); i++) {
            tableView.getSelectionModel().select(targetIndex + i);
        }
    }

    /**
     * 移动所选行选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param <T>         表格数据项类型
     */
    public static <T> void buildMoveDataMenu(TableView<T> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_moveSelected());
        // 创建二级菜单项
        MenuItem up = new MenuItem(menuItem_moveUp());
        MenuItem down = new MenuItem(menuItem_moveDown());
        MenuItem top = new MenuItem(menuItem_moveTop());
        MenuItem bottom = new MenuItem(menuItem_moveBottom());
        // 为每个菜单项添加事件处理
        up.setOnAction(_ -> upMoveDataMenuItem(tableView));
        down.setOnAction(_ -> downMoveDataMenuItem(tableView));
        top.setOnAction(_ -> topMoveDataMenuItem(tableView));
        bottom.setOnAction(_ -> bottomMoveDataMenuItem(tableView));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(up, down, top, bottom);
        contextMenu.getItems().add(menu);
    }

    /**
     * 所选行上移一行选项
     *
     * @param tableView 要处理的数据列表
     * @param <T>       表格数据项类型
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
     * @param <T>       表格数据项类型
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
     * 所选行置顶
     *
     * @param tableView 要处理的数据列表
     * @param <T>       表格数据项类型
     */
    private static <T> void topMoveDataMenuItem(TableView<T> tableView) {
        ObservableList<T> items = tableView.getItems();
        List<T> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
        if (!selectedItems.isEmpty()) {
            // 移除所有选中项
            items.removeAll(selectedItems);
            // 插入到列表顶部（保持原有顺序）
            items.addAll(0, selectedItems);
            // 刷新表格显示
            tableView.refresh();
            // 重新选中被移动的项
            tableView.getSelectionModel().clearSelection();
            tableView.getSelectionModel().selectRange(0, selectedItems.size());
        }
    }

    /**
     * 所选行置底
     *
     * @param tableView 要处理的数据列表
     * @param <T>       表格数据项类型
     */
    private static <T> void bottomMoveDataMenuItem(TableView<T> tableView) {
        ObservableList<T> items = tableView.getItems();
        List<T> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
        if (!selectedItems.isEmpty()) {
            // 移除所有选中项
            items.removeAll(selectedItems);
            // 插入到列表末尾（保持原有顺序）
            items.addAll(selectedItems);
            // 刷新表格显示
            tableView.refresh();
            // 重新选中被移动的项
            tableView.getSelectionModel().clearSelection();
            int lastIndex = items.size() - 1;
            int startIndex = lastIndex - selectedItems.size() + 1;
            if (startIndex >= 0 && lastIndex >= startIndex) {
                tableView.getSelectionModel().selectRange(startIndex, lastIndex + 1);
            }
        }
    }

    /**
     * 查看文件选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildFilePathItem(TableView<ImgFileVO> tableView, ContextMenu contextMenu) {
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
    private static void openFileMenuItem(TableView<ImgFileVO> tableView) {
        List<ImgFileVO> fileBeans = tableView.getSelectionModel().getSelectedItems();
        fileBeans.forEach(fileBean -> openFile(fileBean.getPath()));
    }

    /**
     * 打开所选文件所在文件夹选项
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void openDirectorMenuItem(TableView<ImgFileVO> tableView) {
        List<ImgFileVO> fileBeans = tableView.getSelectionModel().getSelectedItems();
        List<String> pathList = fileBeans.stream().map(ImgFileVO::getPath).distinct().toList();
        pathList.forEach(FileUtils::openDirectory);
    }

    /**
     * 复制文件路径选项
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void copyFilePathItem(TableView<? extends ImgFileVO> tableView) {
        ImgFileVO fileBean = tableView.getSelectionModel().getSelectedItem();
        copyText(fileBean.getPath());
    }

    /**
     * 复制所选数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param dataNumber  列表数据数量文本框
     */
    public static void buildCopyDataMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu, Label dataNumber) {
        Menu menu = new Menu(menu_copy());
        // 创建二级菜单项
        MenuItem upCopy = new MenuItem(menuItem_upCopy());
        MenuItem downCopy = new MenuItem(menuItem_downCopy());
        MenuItem appendCopy = new MenuItem(menuItem_appendCopy());
        MenuItem topCopy = new MenuItem(menuItem_topCopy());
        // 为每个菜单项添加事件处理
        upCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_upCopy(), dataNumber));
        downCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_downCopy(), dataNumber));
        appendCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_appendCopy(), dataNumber));
        topCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_topCopy(), dataNumber));
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
    private static void copyDataMenuItem(TableView<ClickPositionVO> tableView, String copyType, Label dataNumber) {
        List<ClickPositionVO> copiedList = getCopyList(tableView.getSelectionModel().getSelectedItems());
        if (menuItem_upCopy().equals(copyType)) {
            addData(copiedList, upAdd, tableView, dataNumber, unit_process());
        } else if (menuItem_downCopy().equals(copyType)) {
            addData(copiedList, downAdd, tableView, dataNumber, unit_process());
        } else if (menuItem_appendCopy().equals(copyType)) {
            addData(copiedList, append, tableView, dataNumber, unit_process());
        } else if (menuItem_topCopy().equals(copyType)) {
            addData(copiedList, topAdd, tableView, dataNumber, unit_process());
        }
    }

    /**
     * 获取复制的数据
     *
     * @param selectedItem 选中的数据
     * @return 复制的数据
     */
    private static List<ClickPositionVO> getCopyList(List<ClickPositionVO> selectedItem) {
        List<ClickPositionVO> copiedList = new ArrayList<>();
        selectedItem.forEach(clickPositionBean -> {
            ClickPositionVO copyClickPositionVO = new ClickPositionVO();
            try {
                copyAllProperties(clickPositionBean, copyClickPositionVO);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            copyClickPositionVO.setUuid(UUID.randomUUID().toString());
            copiedList.add(copyClickPositionVO);
        });
        return copiedList;
    }

    /**
     * 更改点击按键
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildEditClickKeyMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_changeKey());
        // 创建二级菜单项
        MenuItem primary = new MenuItem(mouseButton_primary());
        MenuItem secondary = new MenuItem(mouseButton_secondary());
        MenuItem middle = new MenuItem(mouseButton_middle());
        MenuItem forward = new MenuItem(mouseButton_forward());
        MenuItem back = new MenuItem(mouseButton_back());
        // 为每个菜单项添加事件处理
        primary.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_primary()));
        secondary.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_secondary()));
        middle.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_middle()));
        forward.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_forward()));
        back.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_back()));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(primary, secondary, middle, forward, back);
        contextMenu.getItems().add(menu);
    }

    /**
     * 修改点击按键二级菜单选项
     *
     * @param tableView 要添加右键菜单的列表
     * @param clickKey  点击按键
     */
    private static void updateClickKeyMenuItem(TableView<ClickPositionVO> tableView, String clickKey) {
        List<ClickPositionVO> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            selectedItem.forEach(bean -> {
                bean.setClickKeyEnum(recordClickTypeMap.getKey(clickKey));
                tableView.refresh();
            });
        }
    }

    /**
     * 更改重试类型
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildEditRetryTypeMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_changeRetryType());
        // 创建二级菜单项
        MenuItem primary = new MenuItem(retryType_continuously());
        MenuItem secondary = new MenuItem(retryType_click());
        MenuItem middle = new MenuItem(retryType_stop());
        MenuItem forward = new MenuItem(retryType_break());
        // 为每个菜单项添加事件处理
        primary.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_continuously()));
        secondary.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_click()));
        middle.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_stop()));
        forward.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_break()));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(primary, secondary, middle, forward);
        contextMenu.getItems().add(menu);
    }

    /**
     * 更改重试类型二级菜单选项
     *
     * @param tableView 要添加右键菜单的列表
     * @param retryType 操作类型
     */
    private static void updateRetryTypeMenuItem(TableView<ClickPositionVO> tableView, String retryType) {
        List<ClickPositionVO> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            selectedItem.forEach(bean -> {
                bean.setRetryTypeEnum(retryTypeMap.getKey(retryType));
                tableView.refresh();
            });
        }
    }

    /**
     * 修改所选项终止操作图片地址
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param dataNumber  列表数据数量文本框
     * @param unit        列表数据数量单位
     */
    public static void buildEditStopImgPathMenu(TableView<ImgFileVO> tableView, ContextMenu contextMenu, Label dataNumber, String unit) {
        MenuItem upMoveDataMenuItem = new MenuItem(menu_changeFirstImg());
        upMoveDataMenuItem.setOnAction(_ -> {
            ObservableList<ImgFileVO> selectedItems = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItems)) {
                ImgFileVO selectedItem = selectedItems.getFirst();
                Window window = tableView.getScene().getWindow();
                File file = creatImgFileChooser(window, selectedItem.getPath());
                if (file != null) {
                    List<ImgFileVO> allImg = tableView.getItems();
                    List<ImgFileVO> checkList = new ArrayList<>(allImg);
                    checkList.remove(selectedItem);
                    boolean isExist = checkList.stream().anyMatch(bean ->
                            file.getPath().equals(bean.getPath()));
                    if (isExist) {
                        ButtonType buttonType = creatConfirmDialog(
                                confirm_imageExist(),
                                confirm_imageExistConfirm(),
                                confirm_delete(),
                                confirm_cancel());
                        if (!buttonType.getButtonData().isCancelButton()) {
                            tableView.getItems().remove(selectedItem);
                        }
                    } else {
                        selectedItem.setPath(file.getPath());
                        selectedItem.setType(getExistsFileType(file));
                        selectedItem.setName(getExistsFileName(file));
                    }
                    selectedItem.updateThumb();
                    updateTableViewSizeText(tableView, dataNumber, unit);
                }
            }
        });
        contextMenu.getItems().add(upMoveDataMenuItem);
    }

    /**
     * 修改所选项要点击的图片地址
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildEditClickImgPathMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        MenuItem upMoveDataMenuItem = new MenuItem(menu_changeFirstImg());
        upMoveDataMenuItem.setOnAction(_ -> {
            ObservableList<ClickPositionVO> selectedItems = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItems)) {
                ClickPositionVO selectedItem = selectedItems.getFirst();
                Window window = tableView.getScene().getWindow();
                File file = creatImgFileChooser(window, selectedItem.getClickImgPath());
                if (file != null) {
                    selectedItem.setClickImgPath(file.getAbsolutePath());
                    selectedItem.updateThumb();
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
     * @param <T>         列表数据类型
     */
    public static <T> void buildClearSelectedData(TableView<T> tableView, ContextMenu contextMenu) {
        MenuItem clearSelectedDataMenuItem = new MenuItem(menu_cancelSelected());
        clearSelectedDataMenuItem.setOnAction(_ -> tableView.getSelectionModel().clearSelection());
        contextMenu.getItems().add(clearSelectedDataMenuItem);
    }

    /**
     * 删除所选数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param dataNumber  列表对应的统计信息展示栏
     * @param contextMenu 右键菜单集合
     * @param unit        统计信息展示栏数量单位
     * @param <T>         列表数据类型
     */
    public static <T> void buildDeleteDataMenuItem(TableView<T> tableView, Label dataNumber, ContextMenu contextMenu, String unit) {
        MenuItem deleteDataMenuItem = new MenuItem(menu_deleteMenu());
        deleteDataMenuItem.setOnAction(_ -> {
            TableView.TableViewSelectionModel<T> selectionModel = tableView.getSelectionModel();
            // 要删除的选中项
            ObservableList<T> selectedItems = selectionModel.getSelectedItems();
            ObservableList<T> items = tableView.getItems();
            // 获取首个选中行的索引
            int selectedIndex = items.indexOf(selectedItems.getFirst());
            items.removeAll(selectedItems);
            if (selectedIndex > 0) {
                // 选中删除项的上一行
                tableView.getSelectionModel().clearSelection();
                tableView.getSelectionModel().select(selectedIndex - 1);
                // 滚动到插入位置
                tableView.scrollTo(selectedIndex - 1);
            }
            updateTableViewSizeText(tableView, dataNumber, unit);
        });
        contextMenu.getItems().add(deleteDataMenuItem);
    }

    /**
     * 为列表添加右键菜单并设置可选择多行
     *
     * @param contextMenu 右键菜单
     * @param tableView   要处理的列表
     * @param <T>         列表数据类型
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
     * @param <T>           列表数据类型
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
     * @param taskBean 包含防重复点击组件列表的 taskBean
     * @param disable  可点击状态，true 设置为不可点击，false 设置为可点击
     */
    public static void changeDisableNodes(TaskBean<?> taskBean, boolean disable) {
        List<Node> disableNodes = taskBean.getDisableNodes();
        changeDisableNodes(disableNodes, disable);
    }

    /**
     * 改变要防重复点击的组件状态
     *
     * @param disableNodes 防重复点击组件列表
     * @param disable      可点击状态，true 设置为不可点击，false 设置为可点击
     */
    public static void changeDisableNodes(List<? extends Node> disableNodes, boolean disable) {
        if (CollectionUtils.isNotEmpty(disableNodes)) {
            disableNodes.forEach(dc -> {
                if (dc != null) {
                    dc.setDisable(disable);
                }
            });
        }
    }

    /**
     * 为配置组件设置上次配置值
     *
     * @param control 需要处理的组件
     * @param prop    配置文件
     * @param key     要读取的 key
     */
    public static void setControlLastConfig(Control control, Properties prop, String key) {
        setControlLastConfig(control, prop, key, "");
    }

    /**
     * 为配置组件设置上次配置值
     *
     * @param control      需要处理的组件
     * @param prop         配置文件
     * @param key          要读取的 key
     * @param defaultValue 默认值
     */
    @SuppressWarnings("unchecked")
    public static void setControlLastConfig(Control control, Properties prop, String key, String defaultValue) {
        String lastValue = prop.getProperty(key, defaultValue);
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
     * 为终止操作列表配置上次设置的图片
     *
     * @param tableView  需要处理的列表
     * @param prop       配置文件
     * @param key        要读取的 key
     * @param dataNumber 列表数据数量
     */
    public static void setControlLastConfig(TableView<ImgFileVO> tableView, Properties prop, String key, Label dataNumber) {
        int index = 0;
        while (true) {
            String path = prop.getProperty(key + index);
            if (path == null) {
                break;
            }
            File file = new File(path);
            ImgFileVO bean = new ImgFileVO();
            bean.setTableView(tableView)
                    .setName(getFileName(path))
                    .setType(getExistsFileType(file))
                    .setPath(path);
            tableView.getItems().add(bean);
            index++;
        }
        updateTableViewSizeText(tableView, dataNumber, unit_img());
    }

    /**
     * 为路径文本框设置上次配置值
     *
     * @param label 需要处理的文本框
     * @param prop  配置文件
     * @param key   要读取的 key
     */
    public static void setControlLastConfig(Label label, Properties prop, String key) {
        String lastValue = prop.getProperty(key);
        if (FilenameUtils.getPrefixLength(lastValue) != -1) {
            setPathLabel(label, lastValue);
        }
    }

    /**
     * 为颜色选择器设置上次配置值
     *
     * @param colorPicker    颜色选择器
     * @param prop           配置文件
     * @param colorKey       选中的颜色 key
     * @param colorCustomKey 保存的自定义颜色 key
     */
    public static void setColorPickerConfig(ColorPicker colorPicker, Properties prop, String colorKey, String colorCustomKey) {
        String selectColor = prop.getProperty(colorKey, defaultColor);
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
     * 设置可打开的文件路径文本框
     *
     * @param pathLabel 文件路径文本栏
     * @param path      文件路径
     * @return 要展示路径的文件
     */
    public static File setPathLabel(Label pathLabel, String path) {
        pathLabel.setText(path);
        if (StringUtils.isBlank(path)) {
            pathLabel.getStyleClass().removeAll("label-button-style", "label-err-style");
            pathLabel.setOnMouseClicked(null);
            pathLabel.setContextMenu(null);
            pathLabel.setOnMousePressed(null);
            Tooltip.uninstall(pathLabel, pathLabel.getTooltip());
            return null;
        }
        File file = new File(path);
        String openText = text_mouseClickOpen();
        if (!file.exists()) {
            pathLabel.getStyleClass().remove("label-button-style");
            pathLabel.getStyleClass().add("label-err-style");
            openText = text_mouseClickOpenNull();
        } else {
            pathLabel.getStyleClass().remove("label-err-style");
            pathLabel.getStyleClass().add("label-button-style");
        }
        String openPath;
        // 判断打开方式
        boolean openParentDirectory;
        if (file.isDirectory()) {
            if (isMac && file.getName().contains(app)) {
                openPath = file.getParent();
                openParentDirectory = true;
            } else {
                openParentDirectory = false;
                openPath = path;
            }
        } else {
            openParentDirectory = true;
            openPath = file.getParent();
        }
        pathLabel.setOnMouseClicked(event -> {
            // 只接受左键点击
            if (event.getButton() == MouseButton.PRIMARY) {
                // 判断是否打开文件
                if (openParentDirectory) {
                    openParentDirectory(path);
                } else {
                    openDirectory(path);
                }
            }
        });
        if (StringUtils.isBlank(openPath)) {
            openPath = "";
            openText = text_mouseClickNull();
        }
        addToolTip(path + openText + openPath, pathLabel);
        // 设置右键菜单
        setPathLabelContextMenu(pathLabel);
        return file;
    }

    /**
     * 给路径 Label 设置右键菜单
     *
     * @param valueLabel 要处理的文本栏
     */
    public static void setPathLabelContextMenu(Label valueLabel) {
        String path = valueLabel.getText();
        ContextMenu contextMenu = new ContextMenu();
        File file = new File(path);
        if ((!file.getName().contains(app) && file.isDirectory())) {
            MenuItem openDirectoryMenuItem = new MenuItem(text_openDirectory());
            openDirectoryMenuItem.setOnAction(_ -> openDirectory(path));
            contextMenu.getItems().add(openDirectoryMenuItem);
        }
        MenuItem openParentDirectoryMenuItem = new MenuItem(text_openParentDirectory());
        openParentDirectoryMenuItem.setOnAction(_ -> openParentDirectory(path));
        contextMenu.getItems().add(openParentDirectoryMenuItem);
        if (file.isFile() && !file.getName().equals(appName + exe)) {
            MenuItem openFileMenuItem = new MenuItem(text_openFile());
            openFileMenuItem.setOnAction(_ -> openFile(path));
            contextMenu.getItems().add(openFileMenuItem);
        }
        MenuItem copyValueMenuItem = new MenuItem(text_copyPath());
        contextMenu.getItems().add(copyValueMenuItem);
        valueLabel.setContextMenu(contextMenu);
        copyValueMenuItem.setOnAction(_ -> copyText(valueLabel.getText()));
        valueLabel.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(valueLabel, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 添加复制 Label 值右键菜单
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
        copyValueMenuItem.setOnAction(_ -> copyText(valueLabel.getText()));
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
        new MessageBubble(text_copySuccess(), 2);
    }

    /**
     * 指定组件设置右对齐
     *
     * @param hBox           组件所在 hBox
     * @param alignmentWidth 右对齐参考组件宽度
     * @param region         要设置右对齐的组件
     */
    public static void regionRightAlignment(HBox hBox, double alignmentWidth, Region region) {
        ObservableList<Node> nodes = hBox.getChildren();
        double spacing = hBox.getSpacing();
        double prefWidth = alignmentWidth - spacing;
        for (Node node : nodes) {
            if (!region.getId().equals(node.getId())) {
                prefWidth = prefWidth - node.getLayoutBounds().getWidth() - spacing;
            }
        }
        region.setPrefWidth(prefWidth);
    }

    /**
     * 保存多选框选择设置
     *
     * @param checkBox   更改配置的选项框
     * @param configFile 要更新的配置文件相对路径
     * @param key        要更新的配置
     * @throws IOException 配置文件保存异常
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
     * @param floatingStage 要获取屏幕位置的窗口
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
     * @param offsetX       x 轴偏移量
     * @param offsetY       y 轴偏移量
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
        if (isWin) {
            // 保证浮窗一直置顶
            floatingStage.setAlwaysOnTop(true);
            floatingStage.setIconified(false);
        }
    }

    /**
     * 将程序窗口弹出
     *
     * @param stage 程序主舞台
     */
    public static void showStage(Stage stage) {
        Platform.runLater(() -> {
            stage.setAlwaysOnTop(true);
            stage.setIconified(false);
            stage.show();
            stage.toFront();
            stage.requestFocus();
            stage.setAlwaysOnTop(false);
        });
    }

    /**
     * 弹出界面和错误弹窗
     *
     * @param errs  错误详情
     * @param title 错误标题
     * @param stage 错误弹窗的父窗口
     */
    public static void showStageAlert(List<String> errs, String title, Stage stage) {
        if (stage.isIconified()) {
            stage.setIconified(false);
            stage.show();
            stage.toFront();
            stage.requestFocus();
        }
        Alert alert = creatErrorAlert(String.join("\n", errs));
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.initOwner(stage);
        alertStage.setAlwaysOnTop(true);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    /**
     * 图片列表拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    public static void acceptDropImg(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        files.forEach(file -> {
            if (imageType.contains(getExistsFileType(file))) {
                // 接受拖放
                dragEvent.acceptTransferModes(TransferMode.COPY);
                dragEvent.consume();
            }
        });
    }

    /**
     * 构建表格右键菜单
     *
     * @param tableView  要添加右键菜单的列表
     * @param dataNumber 数据数量信息栏
     */
    public static void buildTableViewContextMenu(TableView<ImgFileVO> tableView, Label dataNumber) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 修改图片路径选项
        buildEditStopImgPathMenu(tableView, contextMenu, dataNumber, unit_img());
        // 移动所选行选项
        buildMoveDataMenu(tableView, contextMenu);
        // 查看文件选项
        buildFilePathItem(tableView, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView, dataNumber, contextMenu, unit_img());
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView);
    }

    /**
     * 构建窗口信息栏右键菜单
     *
     * @param label         窗口信息栏
     * @param windowMonitor 窗口监视器
     * @param disableNodes  要防重复点击的组件
     * @param stages        需要隐藏的窗口
     */
    public static void buildWindowInfoMenu(Label label, WindowMonitor windowMonitor,
                                           List<? extends Node> disableNodes, List<? extends Stage> stages) {
        // 添加窗口信息右键菜单
        ContextMenu windowInfoMenu = new ContextMenu();
        // 更新窗口信息选项
        buildUpdateDataMenu(windowInfoMenu, windowMonitor);
        // 显示窗口位置信息
        buildShowDataMenu(windowInfoMenu, windowMonitor, disableNodes, stages);
        // 删除窗信息据选项
        buildDeleteDataMenu(windowInfoMenu, windowMonitor);
        // 为窗口信息栏添加右键菜单
        label.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                windowInfoMenu.show(label, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 更新窗口数据选项
     *
     * @param contextMenu   右键菜单集合
     * @param windowMonitor 窗口监视器
     */
    private static void buildUpdateDataMenu(ContextMenu contextMenu, WindowMonitor windowMonitor) {
        MenuItem menuItem = new MenuItem(findImgSet_updateWindow());
        menuItem.setOnAction(_ -> windowMonitor.updateWindowInfo());
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 显示窗口位置信息
     *
     * @param contextMenu   右键菜单集合
     * @param windowMonitor 窗口监视器
     * @param disableNodes  要防重复点击的组件
     * @param stages        需要隐藏的窗口
     */
    private static void buildShowDataMenu(ContextMenu contextMenu, WindowMonitor windowMonitor,
                                          List<? extends Node> disableNodes, List<? extends Stage> stages) {
        MenuItem menuItem = new MenuItem(findImgSet_showWindow());
        menuItem.setOnAction(_ -> {
            windowMonitor.updateWindowInfo();
            WindowInfo windowInfo = windowMonitor.getWindowInfo();
            if (windowInfo != null) {
                stages.forEach(stage -> stage.setIconified(true));
                String info = text_escCloseFloating() + "\n" +
                        findImgSet_PName() + windowInfo.getProcessName() + "\n" +
                        findImgSet_PID() + windowInfo.getPid() + "\n" +
                        findImgSet_windowPath() + windowInfo.getProcessPath() + "\n" +
                        findImgSet_windowTitle() + windowInfo.getTitle() + "\n" +
                        findImgSet_windowLocation() + " X: " + windowInfo.getX() + " Y: " + windowInfo.getY() + "\n" +
                        findImgSet_windowSize() + " W: " + windowInfo.getWidth() + " H: " + windowInfo.getHeight();
                windowInfoFloating.setMassage(info)
                        .getConfig()
                        .setHeight(windowInfo.getHeight())
                        .setWidth(windowInfo.getWidth())
                        .setX(windowInfo.getX())
                        .setY(windowInfo.getY());
                // 改变要防重复点击的组件状态
                changeDisableNodes(disableNodes, true);
                showFloatingWindow(windowInfoFloating);
                windowMonitor.startNativeKeyListener();
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 删除窗口信息
     *
     * @param contextMenu   右键菜单集合
     * @param windowMonitor 窗口监视器
     */
    private static void buildDeleteDataMenu(ContextMenu contextMenu, WindowMonitor windowMonitor) {
        MenuItem menuItem = new MenuItem(findImgSet_deleteWindow());
        menuItem.setOnAction(_ -> Platform.runLater(windowMonitor::removeWindowInfo));
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 更新列表数据数量提示框
     *
     * @param tableView      列表对象
     * @param dataNumber     提示框对象
     * @param dataNumberUnit 数据单位
     * @param <T>            列表数据类型
     */
    public static <T> void updateTableViewSizeText(TableView<T> tableView, Label dataNumber, String dataNumberUnit) {
        int tableSize = tableView.getItems().size();
        if (tableSize > 0) {
            dataNumber.setText(text_allHave() + tableSize + dataNumberUnit);
        } else {
            dataNumber.setText(listText_dataListNull());
        }
    }

    /**
     * 设置日期选择器显示格式
     *
     * @param datePicker    日期选择框
     * @param dateFormatter 日期格式
     */
    public static void setDatePickerFormatter(DatePicker datePicker, DateTimeFormatter dateFormatter) {
        // 设置日期转换器
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return LocalDate.parse(string, dateFormatter);
            }
        });
        // 给日期选择框添加鼠标点击事件
        datePicker.getEditor().setOnMouseClicked(_ -> {
            if (!datePicker.isShowing()) {
                datePicker.show();
            }
        });
    }

    /**
     * 初始化下拉框
     *
     * @param choiceBox    下拉框
     * @param defaultValue 默认值
     * @param values       可选值 List
     * @param <T>          可选值类型
     */
    public static <T> void initializeChoiceBoxItems(ChoiceBox<? super T> choiceBox, T defaultValue, List<? extends T> values) {
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(values);
        choiceBox.setValue(defaultValue);
    }

    /**
     * 初始化下拉框
     *
     * @param choiceBox    下拉框
     * @param defaultValue 默认值
     * @param values       可选值 Map
     * @param <T>          可选值类型
     */
    public static <T> void initializeChoiceBoxItems(ChoiceBox<? super T> choiceBox, T defaultValue, Map<?, ? extends T> values) {
        choiceBox.getItems().clear();
        values.forEach((_, value) -> choiceBox.getItems().add(value));
        choiceBox.setValue(defaultValue);
    }

    /**
     * 显示更新提示框
     *
     * @param updateInfo 更新信息
     * @return 用户选择的按钮类型
     */
    public static Optional<ButtonType> showUpdateDialog(CheckUpdateBean updateInfo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(update_checkUpdate_Abt());
        alert.setHeaderText(update_findNewVersion() + updateInfo.getVersion() + "        " +
                update_releaseDate() + updateInfo.getBuildDate());
        // 创建包含更新信息的文本区域
        TextArea textArea = new TextArea(updateInfo.getWhatsNew());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        alert.getDialogPane().setContent(expContent);
        ButtonType updateButton = new ButtonType(update_updateButton());
        ButtonType laterButton = new ButtonType(update_laterButton(), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(updateButton, laterButton);
        // 设置窗口图标
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        setWindowLogo(stage, logoPath);
        return alert.showAndWait();
    }

    /**
     * 向列表添加文件并根据文件路径去重
     *
     * @param files 文件列表
     * @throws IOException 获取文件属性异常、文件创建时间读取异常
     */
    public static void addRemoveSameFile(List<? extends File> files, boolean isAllDirectory, TableView<FileVO> tableView) throws IOException {
        if (CollectionUtils.isNotEmpty(files)) {
            List<FileVO> fileBeans = new ArrayList<>();
            // 如果是所有都是目录，只保留顶层目录
            if (isAllDirectory) {
                files = filterTopDirectories(files);
            }
            for (File file : files) {
                if (isPath(file.getPath()) && file.exists()) {
                    FileVO fileBean = creatFileVo(tableView, file);
                    fileBeans.add(fileBean);
                }
            }
            // 根据文件路径去重
            removeSameFilePath(tableView, fileBeans);
        }
    }

    /**
     * 创建文件对象
     *
     * @param tableView 列表对象
     * @param file      文件对象
     * @return 文件对象
     * @throws IOException 文件创建时间读取异常
     */
    public static FileVO creatFileVo(TableView<FileVO> tableView, File file) throws IOException {
        String showStatus = file.isHidden() ? text_hidden() : text_unhidden();
        FileVO fileBean = new FileVO();
        fileBean.setTableView(tableView)
                .setUpdateDate(getFileUpdateTime(file))
                .setCreatDate(getFileCreatTime(file))
                .setFileType(getExistsFileType(file))
                .setName(getExistsFileName(file))
                .setSize(getFileUnitSize(file))
                .setShowStatus(showStatus)
                .setPath(file.getPath());
        return fileBean;
    }

    /**
     * 根据文件路径去重
     *
     * @param tableView 列表对象
     * @param fileBeans 要去重的文件列表
     */
    public static void removeSameFilePath(TableView<FileVO> tableView, List<FileVO> fileBeans) {
        List<FileVO> currentItems = new ArrayList<>(tableView.getItems());
        List<FileVO> filteredList = fileBeans.stream()
                .filter(fileBean ->
                        currentItems.stream().noneMatch(current ->
                                current.getPath().equals(fileBean.getPath())))
                .toList();
        tableView.getItems().addAll(filteredList);
        tableView.refresh();
    }

    /**
     * 文件大小排序
     *
     * @param sizeColumn 要进行文件大小排序的列
     */
    public static void fileSizeColum(TableColumn<?, String> sizeColumn) {
        // 自定义比较器
        Comparator<String> customComparator = Comparator.comparingDouble(FileUtils::fileSizeCompareValue);
        // 应用自定义比较器
        sizeColumn.setComparator(customComparator);
    }

    /**
     * 关闭窗口
     *
     * @param stage    要关闭的窗口
     * @param runnable 关闭前的回调
     */
    public static void closeStage(Stage stage, Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
        WindowEvent closeEvent = new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
        stage.fireEvent(closeEvent);
        if (!closeEvent.isConsumed()) {
            stage.close();
        }
        System.gc();
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
        double with = Double.parseDouble(prop.getProperty(key_fileChooserWidth, "1000"));
        double height = Double.parseDouble(prop.getProperty(key_fileChooserHeight, "450"));
        input.close();
        Scene scene = new Scene(root, with, height);
        detailStage.setScene(scene);
        detailStage.setTitle(fileChooserConfig.getTitle());
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindowLogo(detailStage, logoPath);
        detailStage.show();
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        return controller;
    }

    /**
     * 处理要过滤的文件类型
     *
     * @param filterFileType 填有空格区分的要过滤的文件类型字符串的文本输入框
     * @return 要过滤的文件类型list
     */
    public static List<String> getFilterExtensionList(TextField filterFileType) {
        String filterFileTypeValue = filterFileType.getText();
        return getFilterExtensionList(filterFileTypeValue);
    }

    /**
     * 处理要过滤的文件类型
     *
     * @param filterFileTypeValue 空格区分的要过滤的文件类型字符串
     * @return 要过滤的文件类型list
     */
    public static List<String> getFilterExtensionList(String filterFileTypeValue) {
        List<String> filterExtensionList = new ArrayList<>();
        if (StringUtils.isNotBlank(filterFileTypeValue)) {
            filterExtensionList = Arrays.asList(filterFileTypeValue.toLowerCase().split(" "));
        }
        return filterExtensionList;
    }

}
