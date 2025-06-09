package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.TimedTaskBean;
import priv.koishi.pmc.ThreadPool.ThreadPoolManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.MainApplication.mainStage;
import static priv.koishi.pmc.Service.ScheduledService.deleteTask;
import static priv.koishi.pmc.Service.ScheduledService.getTaskDetailsTask;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 定时任务控制器
 *
 * @author KOISHI
 * Date:2025-05-19
 * Time:16:17
 */
public class TimedTaskController extends RootController {

    /**
     * 页面标识符
     */
    private static final String tabId = "_Task";

    /**
     * 详情页高度
     */
    private int detailHeight;

    /**
     * 详情页宽度
     */
    private int detailWidth;

    /**
     * 线程池实例
     */
    private static final ExecutorService executorService = ThreadPoolManager.getPool(TimedTaskController.class);

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    @FXML
    public AnchorPane anchorPane_Task;

    @FXML
    public HBox fileNumberHBox_Task, tipHBox_Task, logHBox_Task;

    @FXML
    public ProgressBar progressBar_Task;

    @FXML
    public Label dataNumber_Task, tip_Task, log_Task;

    @FXML
    public Button addTimedTask_Task, getScheduleTask_Task;

    @FXML
    public TableView<TimedTaskBean> tableView_Task;

    @FXML
    public TableColumn<TimedTaskBean, Integer> index_Task;

    @FXML
    public TableColumn<TimedTaskBean, String> taskName_Task, date_Task, time_Task, repeat_Task, days_Task, name_Task,
            path_Task;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Task.setPrefHeight(stageHeight * 0.7);
        // 设置组件宽度
        double tableWidth = mainStage.getWidth() * 0.95;
        tableView_Task.setMaxWidth(tableWidth);
        tableView_Task.setPrefWidth(tableWidth);
        nodeRightAlignment(fileNumberHBox_Task, tableWidth, dataNumber_Task);
        nodeRightAlignment(tipHBox_Task, tableWidth, tip_Task);
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
     * 加载默认设置
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        detailWidth = Integer.parseInt(prop.getProperty(key_taskDetailWidth, defaultTaskDetailWidth));
        detailHeight = Integer.parseInt(prop.getProperty(key_taskDetailHeight, defaultTaskDetailHeight));
        input.close();
    }

    /**
     * 清空javafx列表按钮
     */
    public void removeAll() {
        removeTableViewData(tableView_Task, dataNumber_Task, null);
    }

    /**
     * 显示详情页
     *
     * @param item   要显示详情的操作流程设置
     * @param isEdit 是否为编辑模式
     */
    private void showDetail(TimedTaskBean item, boolean isEdit) {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/TaskDetail-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TaskDetailController controller = loader.getController();
        try {
            controller.initData(item, isEdit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 设置保存后的回调
        controller.setRefreshCallback(() -> {
            try {
                // 查询定时任务
                getScheduleTask();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, detailWidth, detailHeight);
        detailStage.setScene(scene);
        String title = item.getTaskName();
        detailStage.setTitle(title + bundle.getString("taskDetail.title"));
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindowLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((v1, v2, v3) ->
                Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((v1, v2, v3) ->
                Platform.runLater(controller::adaption));
        // 设置css样式
        setWindowCss(scene, stylesCss);
        detailStage.show();
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(addTimedTask_Task);
        disableNodes.add(getScheduleTask_Task);
    }

    /**
     * 执行查询设置的定时任务
     *
     * @param successHandler 成功回调
     */
    private void executeGetScheduleTask(Consumer<? super List<TimedTaskBean>> successHandler) {
        removeAll();
        TaskBean<TimedTaskBean> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_Task)
                .setDisableNodes(disableNodes)
                .setBindingMassageLabel(true)
                .setMassageLabel(log_Task);
        Task<List<TimedTaskBean>> task = getTaskDetailsTask();
        bindingTaskNode(task, taskBean);
        task.setOnSucceeded(event -> {
            List<TimedTaskBean> result = task.getValue();
            Platform.runLater(() -> {
                addData(result, append, tableView_Task, dataNumber_Task, text_task(), false);
                taskUnbind(taskBean);
            });
            if (successHandler != null) {
                successHandler.accept(result);
            }
        });
        task.setOnFailed(event -> {
            taskNotSuccess(taskBean, text_taskFailed());
            Throwable ex = task.getException();
            taskUnbind(taskBean);
            throw new RuntimeException(ex);
        });
        executorService.execute(task);
    }

    /**
     * 构建右键菜单
     */
    public void buildContextMenu() {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查看详情选项
        buildDetailMenuItem(tableView_Task, contextMenu);
        // 移动所选行选项
        buildMoveDataMenu(tableView_Task, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView_Task, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Task, contextMenu);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView_Task);
    }

    /**
     * 查看所选项第一行详情选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void buildDetailMenuItem(TableView<? extends TimedTaskBean> tableView, ContextMenu contextMenu) {
        MenuItem detailItem = new MenuItem(menu_detailMenu());
        detailItem.setOnAction(e -> {
            TimedTaskBean selected = tableView.getSelectionModel().getSelectedItems().getFirst();
            if (selected != null) {
                showDetail(selected, true);
            }
        });
        contextMenu.getItems().add(detailItem);
    }

    /**
     * 删除所选数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void buildDeleteDataMenuItem(TableView<TimedTaskBean> tableView, ContextMenu contextMenu) {
        MenuItem deleteDataMenuItem = new MenuItem(menu_deleteMenu());
        deleteDataMenuItem.setOnAction(event -> {
            List<TimedTaskBean> ts = tableView.getSelectionModel().getSelectedItems();
            ts.forEach(item -> {
                try {
                    deleteTask(item.getTaskName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                getScheduleTask();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        contextMenu.getItems().add(deleteDataMenuItem);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_addTimedTask(), addTimedTask_Task);
        addToolTip(tip_getScheduleTask(), getScheduleTask_Task);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        // 设置要防重复点击的组件
        setDisableNodes();
        Platform.runLater(() -> {
            // 设置javafx单元格宽度
            bindPrefWidthProperty();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Task, TimedTaskBean.class, tabId, index_Task);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Task);
            // 构建右键菜单
            buildContextMenu();
            // 设置鼠标悬停提示
            setToolTip();
            try {
                // 查询定时任务
                getScheduleTask();
                // 加载默认设置
                getConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 查询定时任务
     *
     * @throws IOException 获取任务详情失败
     */
    @FXML
    private void getScheduleTask() throws IOException {
        executeGetScheduleTask(null);
    }

    /**
     * 添加定时任务
     */
    @FXML
    public void addTimedTask() {
        executeGetScheduleTask(result -> Platform.runLater(() -> {
            int dataSize = tableView_Task.getItems().size() + 1;
            TimedTaskBean newTask = new TimedTaskBean()
                    .setTaskName(defaultTaskName() + dataSize)
                    .setRepeat(repeatType_daily());
            showDetail(newTask, false);
        }));
    }

}
