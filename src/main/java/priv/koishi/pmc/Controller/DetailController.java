package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Bean.ImgFileBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.FileUtils.getFileType;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 操作步骤详情页控制器
 *
 * @author koishi
 * Date 2022/3/11
 * Time 15:09
 */
public class DetailController {

    /**
     * 页面数据对象
     */
    private ClickPositionBean selectedItem;

    /**
     * 上次所选要点击的图片地址
     */
    private String clickImgSelectPath;

    /**
     * 上次所选终止操作的图片地址
     */
    private String stopImgSelectPath;

    /**
     * 默认要点击的图片识别重试次数
     */
    private String defaultClickRetryNum;

    /**
     * 默认终止操作图片识别重试次数
     */
    private String defaultStopRetryNum;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Det";

    /**
     * 详情页页面舞台
     */
    private Stage stage;

    /**
     * 更新数据用的回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    private AnchorPane anchorPane_Det;

    @FXML
    private HBox fileNumberHBox_Det;

    @FXML
    private ImageView clickImg_Det;

    @FXML
    private Button removeClickImg_Det;

    @FXML
    private ChoiceBox<String> clickType_Det;

    @FXML
    private Slider clickOpacity_Det, stopOpacity_Det;

    @FXML
    private Label clickImgPath_Det, dataNumber_Det, nullLabel_Debt;

    @FXML
    private TextField clickName_Det, mouseStartX_Det, mouseStartY_Det, mouseEndX_Det, mouseEndY_Det, wait_Det,
            clickNumBer_Det, timeClick_Det, interval_Det, clickRetryNum_Det, stopRetryNum_Det;

    @FXML
    private TableView<ImgFileBean> tableView_Det;

    @FXML
    private TableColumn<ImgFileBean, ImageView> thumb_Det;

    @FXML
    private TableColumn<ImgFileBean, String> name_Det, path_Det, type_Det;

    /**
     * 组件宽高自适应
     */
    public void detailAdaption() {
        double tableWidth = stage.getWidth() * 0.5;
        tableView_Det.setMaxWidth(tableWidth);
        tableView_Det.setPrefWidth(tableWidth);
        tableView_Det.setPrefHeight(stage.getHeight() * 0.4);
        nodeRightAlignment(fileNumberHBox_Det, tableWidth, dataNumber_Det);
        nullLabel_Debt.setPrefWidth(stage.getWidth() * 0.4);
    }

    /**
     * 初始化数据
     *
     * @param item 列表选中的数据
     */
    public void initData(ClickPositionBean item) {
        this.selectedItem = item;
        clickName_Det.setText(item.getName());
        mouseStartX_Det.setText(item.getStartX());
        mouseStartY_Det.setText(item.getStartY());
        mouseEndX_Det.setText(item.getEndX());
        mouseEndY_Det.setText(item.getEndY());
        wait_Det.setText(item.getWaitTime());
        clickNumBer_Det.setText(item.getClickNum());
        timeClick_Det.setText(item.getClickTime());
        interval_Det.setText(item.getClickInterval());
        clickType_Det.setValue(item.getType());
        clickOpacity_Det.setValue(Double.parseDouble(item.getClickMatchThreshold()));
        stopOpacity_Det.setValue(Double.parseDouble(item.getStopMatchThreshold()));
        clickRetryNum_Det.setText(item.getClickRetryTimes());
        stopRetryNum_Det.setText(item.getStopRetryTimes());
        clickImgSelectPath = item.getClickImgSelectPath();
        stopImgSelectPath = item.getStopImgSelectPath();
        List<ImgFileBean> stopImgFileBeans = item.getStopImgFileBeans();
        if (CollectionUtils.isNotEmpty(stopImgFileBeans)) {
            tableView_Det.getItems().addAll(stopImgFileBeans);
        }
        String clickImgPath = item.getClickImgPath();
        if (StringUtils.isNotBlank(clickImgPath)) {
            setPathLabel(clickImgPath_Det, clickImgPath, false);
            removeClickImg_Det.setVisible(true);
            clickImg_Det.setImage(new Image(new File(clickImgPath_Det.getText()).toURI().toString()));
        }
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 操作名称文本输入框鼠标悬停提示
        textFieldValueListener(clickName_Det, tip_clickName);
        // 限制单次操作点击间隔文本输入框内容
        integerRangeTextField(wait_Det, 0, null, tip_wait);
        // 限制操作时长文本输入内容
        integerRangeTextField(timeClick_Det, 0, null, tip_clickTime);
        // 限制鼠标结束位置横(X)坐标文本输入框内容
        integerRangeTextField(mouseEndX_Det, 0, null, tip_mouseEndX);
        // 限制鼠标结束位置纵(Y)坐标文本输入框内容
        integerRangeTextField(mouseEndY_Det, 0, null, tip_mouseEndY);
        // 限制鼠标起始位置横(X)坐标文本输入框内容
        integerRangeTextField(mouseStartX_Det, 0, null, tip_mouseStartX);
        // 限制鼠标起始位置纵(Y)坐标文本输入框内容
        integerRangeTextField(mouseStartY_Det, 0, null, tip_mouseStartY);
        // 限制点击次数文本输入框内容
        integerRangeTextField(clickNumBer_Det, 0, null, tip_clickNumBer);
        // 限制终止操作识别失败重试次数文本输入框内容
        integerRangeTextField(stopRetryNum_Det, 0, null, tip_stopRetryNum + defaultStopRetryNum);
        // 限制要点击的图片识别失败重试次数文本输入框内容
        integerRangeTextField(clickRetryNum_Det, 0, null, tip_clickRetryNum + defaultClickRetryNum);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_wait, wait_Det);
        addToolTip(tip_mouseEndX, mouseEndX_Det);
        addToolTip(tip_mouseEndY, mouseEndY_Det);
        addToolTip(tip_clickType, clickType_Det);
        addToolTip(tip_clickTime, timeClick_Det);
        addToolTip(tip_clickName, clickName_Det);
        addToolTip(tip_clickInterval, interval_Det);
        addToolTip(tip_clickNumBer, clickNumBer_Det);
        addToolTip(tip_mouseStartX, mouseStartX_Det);
        addToolTip(tip_mouseStartY, mouseStartY_Det);
        addToolTip(tip_stopRetryNum + defaultStopRetryNum, stopRetryNum_Det);
        addToolTip(tip_clickRetryNum + defaultClickRetryNum, clickRetryNum_Det);
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        defaultStopRetryNum = prop.getProperty(key_defaultStopRetryNum);
        defaultClickRetryNum = prop.getProperty(key_defaultClickRetryNum);
        input.close();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        thumb_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.3));
        name_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.25));
        path_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.3));
        type_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.15));
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 所选行上移一行选项
        buildUpMoveDataMenuItem(tableView_Det, contextMenu);
        // 所选行下移一行选项
        buildDownMoveDataMenuItem(tableView_Det, contextMenu);
        // 修改图片路径选项
        buildEditImgPathMenu(tableView_Det, contextMenu, dataNumber_Det, text_img);
        // 查看文件选项
        buildFilePathItem(tableView_Det, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView_Det, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Det, dataNumber_Det, contextMenu, text_img);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView_Det);
    }

    /**
     * 页面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 读取配置文件
        getConfig();
        // 设置鼠标悬停提示
        setToolTip();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_Det.getScene().getWindow();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Det, ImgFileBean.class, tabId);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Det);
            // 构建右键菜单
            buildContextMenu();
        });
    }

    /**
     * 保存更改并关闭详情页按钮
     */
    @FXML
    private void saveDetail() {
        int mouseStartX = setDefaultIntValue(mouseStartX_Det, 0, 0, null);
        int mouseStartY = setDefaultIntValue(mouseStartY_Det, 0, 0, null);
        selectedItem.setName(clickName_Det.getText());
        selectedItem.setType(clickType_Det.getValue());
        selectedItem.setStartX(String.valueOf(mouseStartX));
        selectedItem.setStartY(String.valueOf(mouseStartY));
        selectedItem.setStopImgSelectPath(stopImgSelectPath);
        selectedItem.setClickImgSelectPath(clickImgSelectPath);
        selectedItem.setClickImgPath(clickImgPath_Det.getText());
        selectedItem.setEndX(String.valueOf(setDefaultIntValue(mouseEndX_Det, mouseStartX, 0, null)));
        selectedItem.setEndY(String.valueOf(setDefaultIntValue(mouseEndY_Det, mouseStartY, 0, null)));
        selectedItem.setWaitTime(String.valueOf(setDefaultIntValue(wait_Det, 0, 0, null)));
        selectedItem.setClickTime(String.valueOf(setDefaultIntValue(timeClick_Det, 0, 0, null)));
        selectedItem.setClickNum(String.valueOf(setDefaultIntValue(clickNumBer_Det, 1, 1, null)));
        selectedItem.setClickInterval(String.valueOf(setDefaultIntValue(interval_Det, 0, 0, null)));
        selectedItem.setClickRetryTimes(String.valueOf(setDefaultIntValue(clickRetryNum_Det, Integer.parseInt(defaultClickRetryNum), 0, null)));
        selectedItem.setStopRetryTimes(String.valueOf(setDefaultIntValue(stopRetryNum_Det, Integer.parseInt(defaultStopRetryNum), 0, null)));
        selectedItem.setClickMatchThreshold(String.valueOf(clickOpacity_Det.getValue()));
        selectedItem.setStopMatchThreshold(String.valueOf(stopOpacity_Det.getValue()));
        selectedItem.setStopImgFileBeans(tableView_Det.getItems());
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 关闭窗口按钮
     */
    @FXML
    private void removeDetail() {
        selectedItem.setRemove(true);
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 选择要点击的图片
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void addClickImgPath(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatImgChooser(window, clickImgSelectPath);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            clickImgSelectPath = updatePathLabel(selectedFile.getPath(), clickImgSelectPath, key_clickImgSelectPath, clickImgPath_Det, configFile_Click);
            removeClickImg_Det.setVisible(true);
            clickImg_Det.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    /**
     * 选择终止操作的图片
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void addStopImgPath(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatImgChooser(window, stopImgSelectPath);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            stopImgSelectPath = updatePathLabel(selectedFile.getPath(), stopImgSelectPath, key_stopImgSelectPath, null, configFile_Click);
            ObservableList<ImgFileBean> items = tableView_Det.getItems();
            boolean isExist = items.stream().anyMatch(bean -> selectedFile.getPath().equals(bean.getPath()));
            ImgFileBean imgFileBean = new ImgFileBean();
            if (!isExist) {
                imgFileBean.setType(getFileType(selectedFile))
                        .setName(selectedFile.getName())
                        .setPath(selectedFile.getPath());
                items.add(imgFileBean);
            }
            dataNumber_Det.setText(text_allHave + items.size() + text_img);
        }
    }

    /**
     * 删除要点击的图片
     */
    @FXML
    public void removeClickImg() {
        clickImgPath_Det.setText("");
        removeClickImg_Det.setVisible(false);
        clickImg_Det.setImage(null);
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    public void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        ObservableList<ImgFileBean> items = tableView_Det.getItems();
        files.forEach(file -> {
            boolean isExist = items.stream().anyMatch(bean -> file.getPath().equals(bean.getPath()));
            if (!isExist) {
                ImgFileBean imgFileBean = new ImgFileBean();
                imgFileBean.setName(file.getName())
                        .setType(getFileType(file))
                        .setPath(file.getPath());
                items.add(imgFileBean);
            }
        });
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    public void acceptDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        files.forEach(file -> {
            if (imageType.contains(getFileType(file))) {
                // 接受拖放
                dragEvent.acceptTransferModes(TransferMode.COPY);
                dragEvent.consume();
            }
        });
    }

    /**
     * 清空图片列表
     */
    @FXML
    private void removeAll() {
        removeTableViewData(tableView_Det, dataNumber_Det, null);
    }

}
