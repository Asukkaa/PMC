package priv.koishi.pmc.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Setter;
import priv.koishi.pmc.Bean.ClickPositionBean;

import static priv.koishi.pmc.Finals.CommonFinals.*;
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

    @Setter
    private Runnable refreshCallback;

    @FXML
    private AnchorPane anchorPane_Det;

    @FXML
    private ChoiceBox<String> clickType_Det;

    @FXML
    private TextField clickName_Det, mouseStartX_Det, mouseStartY_Det, mouseEndX_Det, mouseEndY_Det, wait_Det,
            clickNumBer_Det, timeClick_Det, interval_Det;

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
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        // 设置鼠标悬停提示
        setToolTip();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
    }

    /**
     * 保存更改并关闭详情页按钮
     */
    @FXML
    private void saveDetails() {
        selectedItem.setName(clickName_Det.getText());
        selectedItem.setStartX(mouseStartX_Det.getText());
        selectedItem.setStartY(mouseStartY_Det.getText());
        selectedItem.setEndX(mouseEndX_Det.getText());
        selectedItem.setEndY(mouseEndY_Det.getText());
        selectedItem.setWaitTime(wait_Det.getText());
        selectedItem.setClickNum(clickNumBer_Det.getText());
        selectedItem.setClickTime(timeClick_Det.getText());
        selectedItem.setClickInterval(interval_Det.getText());
        selectedItem.setType(clickType_Det.getValue());
        // 关闭当前窗口
        Stage stage = (Stage) anchorPane_Det.getScene().getWindow();
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
    private void closeDetails() {
        // 关闭当前窗口
        Stage stage = (Stage) anchorPane_Det.getScene().getWindow();
        stage.close();
    }

}
