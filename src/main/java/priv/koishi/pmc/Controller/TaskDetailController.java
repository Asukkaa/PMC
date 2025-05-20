package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.TimedTaskBean;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Service.ScheduledService.createTask;
import static priv.koishi.pmc.Service.ScheduledService.deleteTask;
import static priv.koishi.pmc.Utils.FileUtils.updateProperties;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 定时任务详情控制器
 *
 * @author KOISHI
 * Date:2025-05-20
 * Time:14:53
 */
public class TaskDetailController {

    /**
     * 页面数据对象
     */
    private TimedTaskBean selectedItem;

    /**
     * 导入文件路径
     */
    private static String inFilePath;

    /**
     * 上级页面舞台
     */
    private Stage parentStage;

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
    private AnchorPane anchorPane_TD;

    @FXML
    private Button removePath_TD;

    @FXML
    private DatePicker datePicker_TD;

    @FXML
    private ChoiceBox<String> repeatType_TD;

    @FXML
    private Label pmcFilePath_TD, nullLabel_DT;

    @FXML
    private TextField hourField_TD, minuteField_TD, taskNameField_TD;

    /**
     * 组件宽高自适应
     */
    public void adaption() {
        nullLabel_DT.setPrefWidth(stage.getWidth() * 0.4);
    }

    /**
     * 初始化数据
     *
     * @param item        列表选中的数据
     * @param parentStage 上级页面的舞台
     */
    public void initData(TimedTaskBean item, Stage parentStage) {
        this.parentStage = parentStage;
        selectedItem = item;
        if (item.getPath() != null) {
            String path = item.getPath();
            if (StringUtils.isNotBlank(path)) {
                setPathLabel(pmcFilePath_TD, item.getPath());
                removePath_TD.setVisible(true);
            }
            datePicker_TD.setValue(LocalDate.parse(item.getDate()));
            hourField_TD.setText(item.getTime().substring(0, 2));
            minuteField_TD.setText(item.getTime().substring(3, 5));
            repeatType_TD.setValue(item.getRepeat());
            taskNameField_TD.setText(item.getTaskName());
        }
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 限制小时文本输入框内容
        integerRangeTextField(hourField_TD, 0, 23, tip_hour);
        // 限制分钟文本输入框内容
        minuteSecondRangeTextField(minuteField_TD, tip_minute);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 给日期选择框添加鼠标点击事件
        datePicker_TD.getEditor().setOnMouseClicked(e -> {
            if (!datePicker_TD.isShowing()) {
                datePicker_TD.show();
            }
        });
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_TD.getScene().getWindow();
        });
    }

    /**
     * 设置定时任务要执行的流程
     *
     * @throws IOException io异
     */
    @FXML
    private void loadAutoClick(ActionEvent actionEvent) throws IOException {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(appName, allPMC);
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(filter));
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatFileChooser(window, inFilePath, extensionFilters, text_selectAutoFile);
        if (selectedFile != null) {
            inFilePath = selectedFile.getPath();
            updateProperties(configFile_Click, key_inFilePath, new File(inFilePath).getParent());
            setPathLabel(pmcFilePath_TD, inFilePath);
            removePath_TD.setVisible(true);
        }
    }

    /**
     * 删除要执行的PMC文件路径
     */
    @FXML
    private void removePath() {
        setPathLabel(pmcFilePath_TD, "");
        removePath_TD.setVisible(false);
    }

    /**
     * 保存定时任务
     *
     * @throws IOException              任务创建失败
     * @throws IllegalArgumentException 时间格式错误
     */
    @FXML
    private void saveDetail() throws IOException {
        // 获取日期部分
        LocalDate selectedDate = datePicker_TD.getValue();
        if (selectedDate == null) {
            throw new IllegalArgumentException("日期格式为空");
        }
        // 获取时间部分
        int hour = Integer.parseInt(hourField_TD.getText());
        int minute = Integer.parseInt(minuteField_TD.getText());
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("时间格式错误");
        }
        List<Integer> days = new ArrayList<>();
        String repeatType = repeatType_TD.getValue();
        if (WEEKLY_CN.equals(repeatType)) {
            int dayOfWeek = selectedDate.getDayOfWeek().getValue();
            days.add(dayOfWeek);
        }
        // 组合完整时间
        LocalDateTime triggerTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
        // 创建定时任务
        createTask(triggerTime, repeatType, pmcFilePath_TD.getText(), days);
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 删除当前任务按钮
     */
    @FXML
    private void removeDetail() throws IOException {
        deleteTask();
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

}
