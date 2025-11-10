package priv.koishi.pmc.Utils;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.Bean.VO.FileVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor;
import priv.koishi.pmc.MainApplication;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static priv.koishi.pmc.Controller.SettingController.windowInfoFloating;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.isDarkTheme;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.showFloatingWindow;
import static priv.koishi.pmc.Utils.CommonUtils.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.TableViewUtils.updateTableViewSizeText;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;

/**
 * ui相关工具类
 *
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:38
 */
public class UiUtils {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(UiUtils.class);

    /**
     * 字体颜色绑定
     */
    public static final ObjectProperty<Color> textColorProperty = new SimpleObjectProperty<>(Color.BLACK);

    /**
     * 无法切换深色布局的页面控制器类集合
     */
    public static Set<Class<?>> manuallyChangeThemeList = new HashSet<>();

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
     * 设置默认整数值
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
     * 设置默认小数值
     *
     * @param textField    要设置默认值的文本输入框
     * @param defaultValue 默认值
     * @param min          文本输入框可填写的最小值，为空则不限制最小值
     * @param max          文本输入框可填写的最大值，为空则不限制最大值
     * @return 文本输入框所填值如果在规定范围内则返回所填值，否则返回默认值
     */
    public static double setDefaultDoubleValue(TextField textField, double defaultValue, Double min, Double max) {
        String valueStr = textField.getText();
        double value = defaultValue;
        if (isInDecimalRange(valueStr, min, max)) {
            value = Double.parseDouble(valueStr);
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
     * 展示错误信息栏
     *
     * @param massageLabel 错误信息栏
     * @param log          要展示的错误信息
     */
    public static void showErrLabelText(Label massageLabel, String log) {
        Platform.runLater(() -> {
            massageLabel.setTextFill(Color.RED);
            massageLabel.setText(log);
        });
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
     * 设置要绑定的字体颜色
     *
     * @param textColorProperty 字体颜色绑定器
     * @param color             要绑定的字体颜色
     */
    public static void setTextColorProperty(ObjectProperty<? super Color> textColorProperty, Color color) {
        textColorProperty.set(color);
    }

    /**
     * 修改 label 信息
     *
     * @param label 要修改的文本栏
     * @param text  要修改的文本
     */
    public static void updateLabel(Label label, String text) {
        label.textProperty().unbind();
        label.textFillProperty().unbind();
        label.setText(text);
        label.textFillProperty().bind(textColorProperty);
        label.textFillProperty().unbind();
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
        setCopyValueContextMenu(contextMenu, valueLabel, text_copyPath());
        valueLabel.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(valueLabel, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 添加复制 Label 值右键菜单
     *
     * @param contextMenu 右键菜单
     * @param valueLabel  要处理的文本栏
     * @param text        右键菜单文本
     */
    public static void setCopyValueContextMenu(ContextMenu contextMenu, Label valueLabel, String text) {
        MenuItem copyValueMenuItem = new MenuItem(text);
        valueLabel.setContextMenu(contextMenu);
        // 设置右键菜单行为
        copyValueMenuItem.setOnAction(_ -> copyText(valueLabel.getText()));
        contextMenu.getItems().add(copyValueMenuItem);
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
     * 构建窗口信息栏右键菜单
     *
     * @param label         窗口信息栏
     * @param windowMonitor 窗口监视器
     * @param disableNodes  要防重复点击的组件
     * @param stages        需要隐藏的窗口
     * @return 右键菜单
     */
    public static ContextMenu buildWindowInfoMenu(Label label, WindowMonitor windowMonitor,
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
        return windowInfoMenu;
    }

    /**
     * 更新窗口数据选项
     *
     * @param contextMenu   右键菜单集合
     * @param windowMonitor 窗口监视器
     */
    private static void buildUpdateDataMenu(ContextMenu contextMenu, WindowMonitor windowMonitor) {
        MenuItem menuItem = new MenuItem(findImgSet_updateWindow());
        menuItem.setOnAction(_ -> {
            windowMonitor.updateWindowInfo();
            new MessageBubble(text_updateSuccess(), 2);
        });
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
            if (windowInfo != null && windowInfo.getPid() != -1) {
                int x = windowInfo.getX();
                int y = windowInfo.getY();
                int w = windowInfo.getWidth();
                int h = windowInfo.getHeight();
                if (x < 0 && y < 0 && Math.abs(x) > w && Math.abs(y) > h) {
                    new MessageBubble(text_windowHidden(), 2);
                } else {
                    stages.forEach(stage -> stage.setIconified(true));
                    String info = text_escCloseFloating() + "\n" +
                            findImgSet_PName() + windowInfo.getProcessName() + "\n" +
                            findImgSet_PID() + windowInfo.getPid() + "\n" +
                            findImgSet_windowPath() + windowInfo.getProcessPath() + "\n" +
                            findImgSet_windowTitle() + windowInfo.getTitle() + "\n" +
                            findImgSet_windowLocation() + " X: " + x + " Y: " + y + "\n" +
                            findImgSet_windowSize() + " W: " + w + " H: " + h;
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
            } else {
                new MessageBubble(text_noWindowInfo(), 2);
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
     * 处理无法自动切换主题的页面
     *
     * @param pane 页面布局
     */
    public static <T> void manuallyChangeThemePane(Pane pane, Class<T> clazz) {
        if (isDarkTheme) {
            pane.setStyle("""
                    -fx-background-color: -color-border-subtle, -color-base-9;
                    -fx-background-radius: 6px, 0;
                    -fx-background-insets: 0, 0 1 0 0;
                    -fx-border-radius: 6px;
                    -fx-border-width: 1px, 0 3px 0 0;
                    -fx-border-color: transparent, transparent;
                    """);
        } else {
            pane.setStyle(null);
        }
        manuallyChangeThemeList.add(clazz);
    }

}
