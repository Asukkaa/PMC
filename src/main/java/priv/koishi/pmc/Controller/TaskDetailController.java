package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.TimedTaskBean;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Service.ScheduledService.createTask;
import static priv.koishi.pmc.Service.ScheduledService.deleteTask;
import static priv.koishi.pmc.Utils.FileUtils.getFileName;
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
     * 页面是否修改标志
     */
    private boolean isModified;

    /**
     * 编辑模式标志
     */
    private boolean isEdit;

    /**
     * 上级页面舞台
     */
    private Stage parentStage;

    /**
     * 详情页页面舞台
     */
    private Stage stage;

    /**
     * 带鼠标悬停提示的内容变化监听器
     */
    private final Map<Object, ChangeListener<?>> changeListeners = new WeakHashMap<>();

    /**
     * 星期复选框集合
     */
    private final BidiMap<DayOfWeek, CheckBox> weekCheckBoxMap = new DualHashBidiMap<>();

    /**
     * 更新数据用的回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    private AnchorPane anchorPane_TD;

    @FXML
    private HBox filePathHBox_DT, fileNameHBox_TD;

    @FXML
    private Button delete_TD;

    @FXML
    private DatePicker datePicker_TD;

    @FXML
    private ChoiceBox<String> repeatType_TD;

    @FXML
    private Label pmcFilePath_TD, nullLabel_DT, fileName_DT;

    @FXML
    private TextField hourField_TD, minuteField_TD, taskNameField_TD;

    @FXML
    private CheckBox monday_TD, tuesday_TD, wednesday_TD, thursday_TD, friday_TD, saturday_TD, sunday_TD;

    /**
     * 组件宽高自适应
     */
    public void adaption() {
        fileName_DT.setMaxWidth(stage.getWidth() * 0.7);
        nullLabel_DT.setPrefWidth(stage.getWidth() * 0.4);
        pmcFilePath_TD.setMaxWidth(stage.getWidth() * 0.7);
    }

    /**
     * 初始化数据
     *
     * @param item        列表选中的数据
     * @param parentStage 上级页面的舞台
     * @param isEdit      是否为编辑模式
     * @throws IOException 路径不能为空、路径格式不正确
     */
    public void initData(TimedTaskBean item, Stage parentStage, boolean isEdit) throws IOException {
        this.isEdit = isEdit;
        this.parentStage = parentStage;
        selectedItem = item;
        String path = item.getPath();
        if (StringUtils.isNotBlank(path) && !text_onlyLaunch.equals(path)) {
            setPathLabel(pmcFilePath_TD, item.getPath());
            filePathHBox_DT.setVisible(true);
            fileNameHBox_TD.setVisible(true);
            fileName_DT.setText(getFileName(item.getPath()));
            addToolTip(fileName_DT.getText(), fileName_DT);
            if (new File(item.getPath()).exists()) {
                fileName_DT.setTextFill(existsFileColor);
            } else {
                fileName_DT.setTextFill(notExistsFileColor);
            }
        }
        LocalDateTime dateTime = item.getDateTime();
        if (dateTime != null) {
            datePicker_TD.setValue(dateTime.toLocalDate());
            hourField_TD.setText(String.valueOf(dateTime.getHour()));
            minuteField_TD.setText(String.valueOf(dateTime.getMinute()));
        } else {
            datePicker_TD.setValue(LocalDate.now());
        }
        String repeat = item.getRepeat();
        repeatType_TD.setValue(repeat);
        if (WEEKLY_CN.equals(repeat)) {
            String days = item.getDays();
            for (String day : days.split(dayOfWeekRegex)) {
                Integer dayInt = dayOfWeekMap.getKey(day);
                weekCheckBoxMap.get(DayOfWeek.of(dayInt)).setSelected(true);
            }
        }
        taskNameField_TD.setText(item.getTaskName());
        taskNameField_TD.setDisable(isEdit);
        taskNameField_TD.setPromptText(item.getTaskName());
    }

    /**
     * 移除所有监听器
     */
    private void removeAllListeners() {
        // 移除带鼠标悬停提示的内容变化监听器
        removeChangeListener(changeListeners);
        changeListeners.clear();
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 限制小时文本输入框内容
        ChangeListener<String> hourFieldListener = integerRangeTextField(hourField_TD, 0, 23, tip_hour);
        changeListeners.put(hourField_TD, hourFieldListener);
        // 限制分钟文本输入框内容
        ChangeListener<String> minuteFieldListener = integerRangeTextField(minuteField_TD, 0, 59, tip_minute);
        changeListeners.put(minuteField_TD, minuteFieldListener);
        // 限制任务名称文本输入框内容
        ChangeListener<String> taskNameFieldListener = textFieldValueListener(taskNameField_TD, tip_taskName + selectedItem.getTaskName());
        changeListeners.put(taskNameField_TD, taskNameFieldListener);
    }

    /**
     * 获取定时任务设置
     *
     * @return 定时任务设置
     */
    private TimedTaskBean getTimedTaskBean() {
        // 获取日期部分
        LocalDate selectedDate = datePicker_TD.getValue();
        // 获取时间部分
        int hour = setDefaultIntValue(hourField_TD, 0, 0, 23);
        int minute = setDefaultIntValue(minuteField_TD, 0, 0, 59);
        List<Integer> days = new ArrayList<>();
        String repeatType = repeatType_TD.getValue();
        List<String> dayNames = new ArrayList<>();
        if (WEEKLY_CN.equals(repeatType)) {
            Arrays.stream(DayOfWeek.values())
                    .filter(day -> weekCheckBoxMap.get(day).isSelected())
                    .forEach(day -> {
                        days.add(day.getValue());
                        dayNames.add(dayOfWeekMap.get(day.getValue()));
                    });
        }
        // 组合完整时间
        LocalDateTime triggerTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
        TimedTaskBean timedTaskBean = new TimedTaskBean();
        timedTaskBean.setTaskName(taskNameField_TD.getText())
                .setDays(String.join(dayOfWeekRegex, dayNames))
                .setPath(pmcFilePath_TD.getText())
                .setDateTime(triggerTime)
                .setRepeat(repeatType)
                .setDayList(days);
        isModified = !timedTaskBean.equals(selectedItem);
        return timedTaskBean;
    }

    /**
     * 添加确认关闭确认框
     */
    private void addCloseConfirm() {
        CheckBox remindSave = (CheckBox) parentStage.getScene().lookup("#remindTaskSave_Set");
        // 添加关闭请求监听
        if (remindSave != null && remindSave.isSelected()) {
            stage.setOnCloseRequest(e -> {
                getTimedTaskBean();
                if (isModified) {
                    ButtonType result = creatConfirmDialog("修改未保存", "当前有未保存的修改，是否保存？",
                            "保存并关闭", "直接关闭");
                    ButtonBar.ButtonData buttonData = result.getButtonData();
                    if (!buttonData.isCancelButton()) {
                        // 保存并关闭
                        try {
                            saveDetail();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        // 直接关闭
                        stage.close();
                    }
                }
                removeAllListeners();
            });
        }
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_deletePath, delete_TD);
        addValueToolTip(hourField_TD, tip_hour);
        addValueToolTip(minuteField_TD, tip_minute);
        addValueToolTip(datePicker_TD.getEditor(), tip_datePicker);
        addValueToolTip(repeatType_TD, tip_repeatType, repeatType_TD.getValue());
        addValueToolTip(taskNameField_TD, tip_taskName + selectedItem.getTaskName());
        weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> addToolTip(checkBox.getText(), checkBox));
    }

    /**
     * 设置星期复选框集合
     */
    private void setWSeekCheckBoxList() {
        weekCheckBoxMap.put(DayOfWeek.MONDAY, monday_TD);
        weekCheckBoxMap.put(DayOfWeek.TUESDAY, tuesday_TD);
        weekCheckBoxMap.put(DayOfWeek.WEDNESDAY, wednesday_TD);
        weekCheckBoxMap.put(DayOfWeek.THURSDAY, thursday_TD);
        weekCheckBoxMap.put(DayOfWeek.FRIDAY, friday_TD);
        weekCheckBoxMap.put(DayOfWeek.SATURDAY, saturday_TD);
        weekCheckBoxMap.put(DayOfWeek.SUNDAY, sunday_TD);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        // 设置星期复选框集合
        setWSeekCheckBoxList();
        // 设置日期选择器显示格式
        setDatePickerFormatter(datePicker_TD, DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE"));
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_TD.getScene().getWindow();
            // 设置鼠标悬停提示
            setToolTip();
            // 给输入框添加内容变化监听
            textFieldChangeListener();
            // 编辑模式添加确认关闭确认框
            if (isEdit) {
                addCloseConfirm();
            }
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
            fileNameHBox_TD.setVisible(true);
            filePathHBox_DT.setVisible(true);
            fileName_DT.setText(getFileName(inFilePath));
            fileName_DT.setTextFill(existsFileColor);
        }
    }

    /**
     * 删除要执行的PMC文件路径
     */
    @FXML
    private void removePath() {
        setPathLabel(pmcFilePath_TD, "");
        fileNameHBox_TD.setVisible(false);
        filePathHBox_DT.setVisible(false);
    }

    /**
     * 保存定时任务
     *
     * @throws IOException              任务创建失败
     * @throws IllegalArgumentException 时间格式错误
     */
    @FXML
    private void saveDetail() throws IOException {
        TimedTaskBean timedTaskBean = getTimedTaskBean();
        // 创建定时任务
        createTask(timedTaskBean);
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 删除当前任务按钮
     *
     * @throws IOException 删除任务失败
     */
    @FXML
    private void removeDetail() throws IOException {
        String taskName = selectedItem.getTaskName();
        if (StringUtils.isNotBlank(taskName)) {
            deleteTask(taskName);
        }
        stage.close();
        // 触发列表刷新（通过回调）
        if (isEdit && refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 监听重复类型选项变化
     */
    @FXML
    private void repeatTypeChange() {
        String repeatType = repeatType_TD.getValue();
        switch (repeatType) {
            case ONCE_CN -> {
                datePicker_TD.setDisable(false);
                datePickerAction();
                weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> checkBox.setDisable(true));
            }
            case WEEKLY_CN -> {
                datePicker_TD.setValue(LocalDate.now());
                datePicker_TD.setDisable(true);
                datePickerAction();
                weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> checkBox.setDisable(false));
            }
            case DAILY_CN -> {
                datePicker_TD.setValue(LocalDate.now());
                datePicker_TD.setDisable(true);
                weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> {
                    checkBox.setSelected(true);
                    checkBox.setDisable(true);
                });
            }
        }
    }

    /**
     * 监听日期选择器值变化
     */
    @FXML
    private void datePickerAction() {
        DayOfWeek dayOfWeek = datePicker_TD.getValue().getDayOfWeek();
        weekCheckBoxMap.forEach((dayOfWeek1, checkBox) -> checkBox.setSelected(false));
        weekCheckBoxMap.get(dayOfWeek).setSelected(true);
    }

}
