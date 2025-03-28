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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.FileUtils.getFileName;
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
    private ClickPositionVO selectedItem;

    /**
     * 上次所选要点击的图片地址
     */
    private String clickImgSelectPath;

    /**
     * 上次所选终止操作的图片地址
     */
    public String stopImgSelectPath;

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
    private VBox clickImgVBox_Det;

    @FXML
    private ImageView clickImg_Det;

    @FXML
    private CheckBox skip_Det;

    @FXML
    private Slider clickOpacity_Det, stopOpacity_Det;

    @FXML
    private ChoiceBox<String> clickType_Det, retryType_Det;

    @FXML
    private Label clickImgPath_Det, dataNumber_Det, nullLabel_Debt, clickImgName_Det;

    @FXML
    private Button removeClickImg_Det, stopImgBtn_Det, clickImgBtn_Det, removeAll_Det, updateClickName_Det;

    @FXML
    private TextField clickName_Det, mouseStartX_Det, mouseStartY_Det, mouseEndX_Det, mouseEndY_Det, wait_Det,
            clickNumBer_Det, timeClick_Det, interval_Det, clickRetryNum_Det, stopRetryNum_Det;

    @FXML
    private TableView<ImgFileVO> tableView_Det;

    @FXML
    private TableColumn<ImgFileVO, ImageView> thumb_Det;

    @FXML
    private TableColumn<ImgFileVO, String> name_Det, path_Det, type_Det;

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
    public void initData(ClickPositionVO item) throws IOException {
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
        skip_Det.setSelected(item.isSkip());
        clickImgSelectPath = item.getClickImgSelectPath();
        stopImgSelectPath = item.getStopImgSelectPath();
        String retryType = item.getRetryType();
        if (StringUtils.isNotBlank(retryType)) {
            retryType_Det.setValue(item.getRetryType());
        }
        List<ImgFileBean> imgFileBeans = item.getStopImgFiles();
        ObservableList<ImgFileVO> items = tableView_Det.getItems();
        if (CollectionUtils.isNotEmpty(imgFileBeans)) {
            imgFileBeans.forEach(b -> {
                // 必须重新创建对象才能正确属性列表图片
                ImgFileVO imgFileVO = new ImgFileVO();
                imgFileVO.setTableView(tableView_Det)
                        .setType(b.getType())
                        .setName(b.getName())
                        .setPath(b.getPath());
                items.add(imgFileVO);
            });
            dataNumber_Det.setText(text_allHave + items.size() + text_img);
        }
        tableView_Det.refresh();
        showClickImg(item.getClickImgPath());
    }

    /**
     * 展示要点击的图片
     *
     * @param clickImgPath 要点击的图片路径
     * @throws IOException 文件不存在
     */
    private void showClickImg(String clickImgPath) throws IOException {
        if (StringUtils.isNotBlank(clickImgPath)) {
            File clickImgFile = setPathLabel(clickImgPath_Det, clickImgPath, false);
            removeClickImg_Det.setVisible(true);
            if (clickImgFile.exists()) {
                clickImg_Det.setImage(new Image((clickImgFile).toURI().toString()));
                clickImgName_Det.setTextFill(Color.rgb(0, 88, 128));
            } else {
                clickImg_Det.setImage(null);
                clickImgName_Det.setTextFill(Color.RED);
            }
            String imgName = getFileName(clickImgFile.getPath());
            clickImgName_Det.setText(imgName);
            addToolTip(imgName, clickImgName_Det);
            clickImgVBox_Det.setVisible(true);
        } else {
            clickImgPath_Det.setText("");
            removeClickImg_Det.setVisible(false);
            clickImg_Det.setImage(null);
            clickImgName_Det.setText("");
            clickImgVBox_Det.setVisible(false);
        }
    }

    /**
     * 给组件添加内容变化监听
     */
    private void nodeValueChangeListener() {
        // 停止操作图像识别准确度设置监听
        integerSliderValueListener(stopOpacity_Det, tip_stopOpacity);
        // 要点击的图像识别准确度设置监听
        integerSliderValueListener(clickOpacity_Det, tip_clickOpacity);
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
        addToolTip(tip_skip, skip_Det);
        addToolTip(tip_wait, wait_Det);
        addToolTip(tip_mouseEndX, mouseEndX_Det);
        addToolTip(tip_mouseEndY, mouseEndY_Det);
        addToolTip(tip_clickType, clickType_Det);
        addToolTip(tip_clickTime, timeClick_Det);
        addToolTip(tip_clickName, clickName_Det);
        addToolTip(tip_stopImgBtn, stopImgBtn_Det);
        addToolTip(tip_clickInterval, interval_Det);
        addToolTip(tip_clickImgBtn, clickImgBtn_Det);
        addToolTip(tip_clickNumBer, clickNumBer_Det);
        addToolTip(tip_mouseStartX, mouseStartX_Det);
        addToolTip(tip_mouseStartY, mouseStartY_Det);
        addToolTip(tip_removeStopImgBtn, removeAll_Det);
        addToolTip(tip_removeClickImgBtn, removeClickImg_Det);
        addToolTip(tip_updateClickNameBtn, updateClickName_Det);
        addToolTip(tip_stopRetryNum + defaultStopRetryNum, stopRetryNum_Det);
        addToolTip(tip_clickRetryNum + defaultClickRetryNum, clickRetryNum_Det);
        addValueToolTip(stopOpacity_Det, tip_stopOpacity, text_nowValue, String.valueOf((int) stopOpacity_Det.getValue()));
        addValueToolTip(clickOpacity_Det, tip_clickOpacity, text_nowValue, String.valueOf((int) clickOpacity_Det.getValue()));
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
        nodeValueChangeListener();
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_Det.getScene().getWindow();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Det, ImgFileVO.class, tabId);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Det);
            // 构建右键菜单
            buildContextMenu(tableView_Det, dataNumber_Det);
        });
    }

    /**
     * 保存更改并关闭详情页按钮
     */
    @FXML
    private void saveDetail() {
        int mouseStartX = setDefaultIntValue(mouseStartX_Det, 0, 0, null);
        int mouseStartY = setDefaultIntValue(mouseStartY_Det, 0, 0, null);
        selectedItem.setSkip(skip_Det.isSelected());
        selectedItem.setName(clickName_Det.getText());
        selectedItem.setType(clickType_Det.getValue());
        selectedItem.setStartX(String.valueOf(mouseStartX));
        selectedItem.setStartY(String.valueOf(mouseStartY));
        selectedItem.setRetryType(retryType_Det.getValue());
        selectedItem.setStopImgSelectPath(stopImgSelectPath);
        selectedItem.setClickImgSelectPath(clickImgSelectPath);
        selectedItem.setClickImgPath(clickImgPath_Det.getText());
        selectedItem.setStopImgFiles(new ArrayList<>(tableView_Det.getItems()));
        selectedItem.setStopMatchThreshold(String.valueOf(stopOpacity_Det.getValue()));
        selectedItem.setClickMatchThreshold(String.valueOf(clickOpacity_Det.getValue()));
        selectedItem.setEndX(String.valueOf(setDefaultIntValue(mouseEndX_Det, mouseStartX, 0, null)));
        selectedItem.setEndY(String.valueOf(setDefaultIntValue(mouseEndY_Det, mouseStartY, 0, null)));
        selectedItem.setWaitTime(String.valueOf(setDefaultIntValue(wait_Det, 0, 0, null)));
        selectedItem.setClickTime(String.valueOf(setDefaultIntValue(timeClick_Det, 0, 0, null)));
        selectedItem.setClickNum(String.valueOf(setDefaultIntValue(clickNumBer_Det, 1, 1, null)));
        selectedItem.setClickInterval(String.valueOf(setDefaultIntValue(interval_Det, 0, 0, null)));
        selectedItem.setStopRetryTimes(String.valueOf(setDefaultIntValue(stopRetryNum_Det, Integer.parseInt(defaultStopRetryNum), 0, null)));
        selectedItem.setClickRetryTimes(String.valueOf(setDefaultIntValue(clickRetryNum_Det, Integer.parseInt(defaultClickRetryNum), 0, null)));
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 删除当前步骤按钮
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
            showClickImg(clickImgSelectPath);
        }
    }

    /**
     * 选择终止操作的图片
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void addStopImgPath(ActionEvent actionEvent) throws IOException {
        stopImgSelectPath = addStopImgPaths(actionEvent, tableView_Det, dataNumber_Det, stopImgSelectPath);
    }

    /**
     * 删除要点击的图片
     */
    @FXML
    public void removeClickImg() throws IOException {
        clickImgPath_Det.setText("");
        showClickImg(null);
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    public void handleDrop(DragEvent dragEvent) {
        handleDropImg(dragEvent, tableView_Det);
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    public void acceptDrop(DragEvent dragEvent) {
        acceptDropImg(dragEvent);
    }

    /**
     * 清空图片列表
     */
    @FXML
    private void removeAll() {
        removeTableViewData(tableView_Det, dataNumber_Det, null);
    }

    /**
     * 将当前步骤名称更新为图片名称
     */
    @FXML
    private void updateClickName() {
        clickName_Det.setText(clickImgName_Det.getText());
    }

}
