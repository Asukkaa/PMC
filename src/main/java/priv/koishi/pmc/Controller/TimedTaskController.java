package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
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
import static priv.koishi.pmc.Service.ScheduledService.getTaskDetails;
import static priv.koishi.pmc.Utils.FileUtils.updateProperties;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 定时任务控制器
 *
 * @author KOISHI
 * Date:2025-05-19
 * Time:16:17
 */
public class TimedTaskController {

    /**
     * 导入文件路径
     */
    private static String inFilePath;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Task";

    @FXML
    private AnchorPane anchorPane_Task;

    @FXML
    private HBox fileNumberHBox_Task, tipHBox_Task;

    @FXML
    private DatePicker datePicker_Task;

    @FXML
    private ChoiceBox<String> repeatType_Task;

    @FXML
    private TextField hourField_Task, minuteField_Task;

    @FXML
    private Label pmcFilePath_Task, dataNumber_Task, tip_Task;

    @FXML
    private Button removePath_Task, addTimedTask_Task, getScheduleTask_Task, clearButton_Task;

    @FXML
    private TableView<TimedTaskBean> tableView_Task;

    @FXML
    private TableColumn<TimedTaskBean, Integer> index_Task;

    @FXML
    private TableColumn<TimedTaskBean, String> taskName_Task, date_Task, time_Task, repeat_Task, days_Task, name_Task, path_Task;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void adaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Task");
        table.setPrefHeight(stageHeight * 0.7);
        // 设置组件宽度
        double tableWidth = stage.getWidth() * 0.95;
        table.setMaxWidth(tableWidth);
        table.setPrefWidth(tableWidth);
        Node index = scene.lookup("#index_Task");
        index.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node taskName = scene.lookup("#taskName_Task");
        taskName.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node date = scene.lookup("#date_Task");
        date.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node time = scene.lookup("#time_Task");
        time.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node repeat = scene.lookup("#repeat_Task");
        repeat.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node days = scene.lookup("#days_Task");
        days.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node name = scene.lookup("#name_Task");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node path = scene.lookup("#path_Task");
        path.setStyle("-fx-pref-width: " + tableWidth * 0.3 + "px;");
        Label dataNum = (Label) scene.lookup("#dataNumber_Task");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Task");
        nodeRightAlignment(fileNumberHBox, tableWidth, dataNum);
        Label tip = (Label) scene.lookup("#tip_Task");
        HBox tipHBox = (HBox) scene.lookup("#tipHBox_Task");
        nodeRightAlignment(tipHBox, tableWidth, tip);
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.1));
        taskName_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.1));
        date_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.1));
        time_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.1));
        repeat_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.1));
        days_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.1));
        name_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.1));
        path_Task.prefWidthProperty().bind(tableView_Task.widthProperty().multiply(0.3));
    }


    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 限制小时文本输入框内容
        integerRangeTextField(hourField_Task, 0, 23, tip_hour);
        // 限制分钟文本输入框内容
        minuteSecondRangeTextField(minuteField_Task, tip_minute);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 给日期选择框添加鼠标点击事件
        datePicker_Task.getEditor().setOnMouseClicked(e -> {
            if (!datePicker_Task.isShowing()) {
                datePicker_Task.show();
            }
        });
        Platform.runLater(() -> {
            bindPrefWidthProperty();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Task, TimedTaskBean.class, tabId, index_Task);
            try {
                getScheduleTask();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 添加定时任务
     *
     * @throws IOException              任务创建失败
     * @throws IllegalArgumentException 时间格式错误
     */
    @FXML
    private void addTimedTask() throws IOException {
        // 获取日期部分
        LocalDate selectedDate = datePicker_Task.getValue();
        if (selectedDate == null) {
            throw new IllegalArgumentException("日期格式为空");
        }
        // 获取时间部分
        int hour = Integer.parseInt(hourField_Task.getText());
        int minute = Integer.parseInt(minuteField_Task.getText());
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("时间格式错误");
        }
        List<Integer> days = new ArrayList<>();
        String repeatType = repeatType_Task.getValue();
        if (WEEKLY_CN.equals(repeatType)) {
            int dayOfWeek = selectedDate.getDayOfWeek().getValue();
            days.add(dayOfWeek);
        }
        // 组合完整时间
        LocalDateTime triggerTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
        // 创建定时任务
        createTask(triggerTime, repeatType, pmcFilePath_Task.getText(), days);
        getScheduleTask();
    }

    /**
     * 查询定时任务
     *
     * @throws IOException 获取任务详情失败
     */
    @FXML
    private void getScheduleTask() throws IOException {
        removeAll();
        List<TimedTaskBean> taskDetails = getTaskDetails();
        addData(taskDetails, append, tableView_Task, dataNumber_Task, text_data);
        System.out.println("查询定时任务：" + taskDetails);
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
            setPathLabel(pmcFilePath_Task, inFilePath);
            removePath_Task.setVisible(true);
        }
    }

    /**
     * 删除要执行的PMC文件路径
     */
    @FXML
    private void removePath() {
        setPathLabel(pmcFilePath_Task, "");
        removePath_Task.setVisible(false);
    }

    /**
     * 清空javafx列表按钮
     */
    @FXML
    public void removeAll() {
        removeTableViewData(tableView_Task, dataNumber_Task, null);
    }

}
