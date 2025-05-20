package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import priv.koishi.pmc.Bean.TimedTaskBean;
import priv.koishi.pmc.MainApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonFinals.logoPath;
import static priv.koishi.pmc.Finals.CommonFinals.text_process;
import static priv.koishi.pmc.Service.ScheduledService.getTaskDetails;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
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
     * 程序主舞台
     */
    private Stage mainStage;

    @FXML
    private AnchorPane anchorPane_Task;

    @FXML
    private HBox fileNumberHBox_Task, tipHBox_Task;

    @FXML
    private Label dataNumber_Task, tip_Task;

    @FXML
    private Button addTimedTask_Task, getScheduleTask_Task;

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
     * @param item 要显示详情的操作流程设置
     */
    private void showDetail(TimedTaskBean item) {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/TaskDetail-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TaskDetailController controller = loader.getController();
        controller.initData(item, mainStage);
        // 设置保存后的回调
        controller.setRefreshCallback(() -> {
            if (item.isRemove()) {
                tableView_Task.getItems().remove(item);
            }
            // 刷新列表
            tableView_Task.refresh();
            // 更新列表数量
            updateTableViewSizeText(tableView_Task, dataNumber_Task, text_process);
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, detailWidth, detailHeight);
        detailStage.setScene(scene);
        String title = item.getTaskName();
        detailStage.setTitle(title + " 任务详情");
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::adaption));
        scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource("css/Styles.css")).toExternalForm());
        detailStage.show();
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            mainStage = (Stage) anchorPane_Task.getScene().getWindow();
            bindPrefWidthProperty();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Task, TimedTaskBean.class, tabId, index_Task);
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
        removeAll();
        List<TimedTaskBean> taskDetails = getTaskDetails();
        addData(taskDetails, append, tableView_Task, dataNumber_Task, text_data);
        System.out.println("查询定时任务：" + taskDetails);
    }

    @FXML
    public void addTimedTask() {
        showDetail(new TimedTaskBean());
    }

}
