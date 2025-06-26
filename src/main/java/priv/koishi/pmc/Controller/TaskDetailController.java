package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.TimedTaskBean;
import priv.koishi.pmc.MessageBubble.MessageBubble;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.Service.ScheduledService.createTask;
import static priv.koishi.pmc.Service.ScheduledService.deleteTask;
import static priv.koishi.pmc.Utils.FileUtils.getFileName;
import static priv.koishi.pmc.Utils.FileUtils.updateProperties;
import static priv.koishi.pmc.Utils.ListenerUtils.removeChangeListener;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 定时任务详情控制器
 *
 * @author KOISHI
 * Date:2025-05-20
 * Time:14:53
 */
public class TaskDetailController extends RootController {

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
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 更新数据用的回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    public AnchorPane anchorPane_TD;

    @FXML
    public VBox progressBarVBox_TD;

    @FXML
    public HBox filePathHBox_DT, fileNameHBox_TD;

    @FXML
    public DatePicker datePicker_TD;

    @FXML
    public ProgressBar progressBar_TD;

    @FXML
    public ChoiceBox<String> repeatType_TD;

    @FXML
    public Label pmcFilePath_TD, fileName_DT, log_TD;

    @FXML
    public TextField hourField_TD, minuteField_TD, taskNameField_TD;

    @FXML
    public Button delete_TD, saveDetail_TD, removeDetail_TD, loadAutoClick_TD;

    @FXML
    public CheckBox monday_TD, tuesday_TD, wednesday_TD, thursday_TD, friday_TD, saturday_TD, sunday_TD;

    /**
     * 组件宽高自适应
     */
    public void adaption() {
        fileName_DT.setMaxWidth(stage.getWidth() * 0.7);
        pmcFilePath_TD.setMaxWidth(stage.getWidth() * 0.7);
        progressBarVBox_TD.setPrefWidth(stage.getWidth() * 0.4);
    }

    /**
     * 初始化数据
     *
     * @param item   列表选中的数据
     * @param isEdit 是否为编辑模式
     * @throws IOException 路径不能为空、路径格式不正确
     */
    public void initData(TimedTaskBean item, boolean isEdit) throws IOException {
        this.isEdit = isEdit;
        selectedItem = item;
        String path = item.getPath();
        if (StringUtils.isNotBlank(path) && !text_onlyLaunch().equals(path)) {
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
        if (repeatType_weekly().equals(repeat)) {
            String days = item.getDays();
            weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> checkBox.setSelected(false));
            for (String day : days.split(dayOfWeekRegex)) {
                Integer dayInt = dayOfWeekMap.getKey(day);
                weekCheckBoxMap.get(DayOfWeek.of(dayInt)).setSelected(true);
            }
        } else if (repeatType_daily().equals(repeat)) {
            weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> {
                checkBox.setSelected(true);
                checkBox.setDisable(true);
            });
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
        ChangeListener<String> hourFieldListener = integerRangeTextField(hourField_TD, 0, 23, tip_hour());
        changeListeners.put(hourField_TD, hourFieldListener);
        // 限制分钟文本输入框内容
        ChangeListener<String> minuteFieldListener = integerRangeTextField(minuteField_TD, 0, 59, tip_minute());
        changeListeners.put(minuteField_TD, minuteFieldListener);
        // 限制任务名称文本输入框内容
        ChangeListener<String> taskNameFieldListener = textFieldValueListener(taskNameField_TD, tip_taskName() + selectedItem.getTaskName());
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
        Arrays.stream(DayOfWeek.values())
                .filter(day -> weekCheckBoxMap.get(day).isSelected())
                .forEach(day -> {
                    days.add(day.getValue());
                    dayNames.add(dayOfWeekMap.get(day.getValue()));
                });
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
        CheckBox remindSave = settingController.remindTaskSave_Set;
        // 添加关闭请求监听
        if (remindSave != null && remindSave.isSelected()) {
            stage.setOnCloseRequest(e -> {
                getTimedTaskBean();
                if (isModified) {
                    ButtonType result = creatConfirmDialog(
                            confirm_unSaved(),
                            confirm_unSavedConfirm(),
                            confirm_ok(),
                            confirm_cancelSave());
                    ButtonBar.ButtonData buttonData = result.getButtonData();
                    if (!buttonData.isCancelButton()) {
                        // 保存并关闭
                        saveDetail();
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
        addToolTip(tip_deletePath(), delete_TD);
        addValueToolTip(hourField_TD, tip_hour());
        addValueToolTip(minuteField_TD, tip_minute());
        addToolTip(tip_loadAutoClickBtn(), loadAutoClick_TD);
        addValueToolTip(datePicker_TD.getEditor(), tip_datePicker());
        addValueToolTip(repeatType_TD, tip_repeatType(), repeatType_TD.getValue());
        addValueToolTip(taskNameField_TD, tip_taskName() + selectedItem.getTaskName());
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
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(saveDetail_TD);
        disableNodes.add(removeDetail_TD);
    }

    /**
     * 初始化下拉框
     */
    private void setChoiceBoxItems() {
        // 重复类型
        initializeChoiceBoxItems(repeatType_TD, repeatType_daily(), repeatTypeList);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        // 初始化下拉框
        setChoiceBoxItems();
        // 设置要防重复点击的组件
        setDisableNodes();
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
        File selectedFile = creatFileChooser(window, inFilePath, extensionFilters, text_selectAutoFile());
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
     * @throws IllegalArgumentException 时间格式错误
     */
    @FXML
    private void saveDetail() {
        TimedTaskBean timedTaskBean = getTimedTaskBean();
        if (StringUtils.isBlank(timedTaskBean.getDays())) {
            throw new IllegalArgumentException(bundle.getString("taskDetail.noWeekDay"));
        }
        TaskBean<TimedTaskBean> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_TD)
                .setDisableNodes(disableNodes)
                .setBindingMassageLabel(true)
                .setMassageLabel(log_TD);
        // 创建定时任务
        Task<Void> task = createTask(timedTaskBean);
        bindingTaskNode(task, taskBean);
        task.setOnSucceeded(event -> Platform.runLater(() -> {
            taskUnbind(taskBean);
            // 复制成功消息气泡
            new MessageBubble(text_successSave(), 2);
            stage.close();
            // 触发列表刷新（通过回调）
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        }));
        task.setOnFailed(event -> {
            taskNotSuccess(taskBean, text_taskFailed());
            throw new RuntimeException(task.getException());
        });
        Thread.ofVirtual()
                .name("task-save-vThread")
                .start(task);
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
        addValueToolTip(repeatType_TD, tip_repeatType(), repeatType);
        if (repeatType_once().equals(repeatType)) {
            datePicker_TD.setDisable(false);
            datePickerAction();
            weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> checkBox.setDisable(true));
        } else if (repeatType_weekly().equals(repeatType)) {
            datePicker_TD.setValue(LocalDate.now());
            datePicker_TD.setDisable(true);
            datePickerAction();
            weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> checkBox.setDisable(false));
        } else if (repeatType_daily().equals(repeatType)) {
            datePicker_TD.setValue(LocalDate.now());
            datePicker_TD.setDisable(true);
            weekCheckBoxMap.forEach((dayOfWeek, checkBox) -> {
                checkBox.setSelected(true);
                checkBox.setDisable(true);
            });
        }
    }

    /**
     * 监听日期选择器值变化
     */
    @FXML
    private void datePickerAction() {
        DayOfWeek dayOfWeek = datePicker_TD.getValue().getDayOfWeek();
        weekCheckBoxMap.forEach((day, checkBox) -> checkBox.setSelected(false));
        weekCheckBoxMap.get(dayOfWeek).setSelected(true);
    }

}
