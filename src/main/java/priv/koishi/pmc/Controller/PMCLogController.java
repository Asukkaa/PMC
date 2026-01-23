package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.pmc.Bean.PMCLogBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static priv.koishi.pmc.Finals.i18nFinal.tip_removeAll_Log;
import static priv.koishi.pmc.Finals.i18nFinal.unit_log;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;

/**
 * PMC 文件操作记录页面控制器
 *
 * @author KOISHI
 * Date:2026-01-23
 * Time:18:05
 */
public class PMCLogController extends RootController {

    /**
     * 页面标识符
     */
    private final String tabId = "_PLog";

    /**
     * 操作记录页面舞台
     */
    private Stage stage;

    /**
     * 操作记录
     */
    @Getter
    public List<PMCLogBean> clickLogs = new CopyOnWriteArrayList<>();

    /**
     * 更新数据用的回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    public ScrollPane scrollPane_PLog;

    @FXML
    public Button removeAll_PLog;

    @FXML
    public Label dataNumber_PLog;

    @FXML
    public TableView<PMCLogBean> tableView_PLog;

    @FXML
    public TableColumn<PMCLogBean, Integer> index_PLog;

    @FXML
    public TableColumn<PMCLogBean, String> date_PLog, name_PLog, path_PLog, result_PLog, time_PLog;

    /**
     * 组件宽高自适应
     */
    public void adaption() {
        double tableWidth = stage.getWidth() * 0.95;
        tableView_PLog.setMaxWidth(tableWidth);
        tableView_PLog.setPrefWidth(tableWidth);
        tableView_PLog.setPrefHeight(stage.getHeight() * 0.8);
        bindPrefWidthProperty();
    }

    /**
     * 设置 JavaFX 单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_PLog.prefWidthProperty().bind(tableView_PLog.widthProperty().multiply(0.1));
        date_PLog.prefWidthProperty().bind(tableView_PLog.widthProperty().multiply(0.2));
        name_PLog.prefWidthProperty().bind(tableView_PLog.widthProperty().multiply(0.2));
        path_PLog.prefWidthProperty().bind(tableView_PLog.widthProperty().multiply(0.3));
        result_PLog.prefWidthProperty().bind(tableView_PLog.widthProperty().multiply(0.1));
        time_PLog.prefWidthProperty().bind(tableView_PLog.widthProperty().multiply(0.1));
    }

    /**
     * 初始化数据
     *
     * @param logs 操作记录
     */
    public void initData(List<PMCLogBean> logs) {
        clickLogs = logs;
        if (CollectionUtils.isNotEmpty(logs)) {
            tableView_PLog.getItems().addAll(logs);
            updateTableViewSizeText(tableView_PLog, dataNumber_PLog, unit_log());
            tableView_PLog.refresh();
        }
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            stage = (Stage) scrollPane_PLog.getScene().getWindow();
            // 设置页面关闭事件处理逻辑
            stage.setOnCloseRequest(_ -> {
                removeController();
                stage = null;
            });
            // 组件宽高自适应
            adaption();
            // 添加鼠标悬停提示
            addToolTip(tip_removeAll_Log(), removeAll_PLog);
            // 自动填充 JavaFX 表格
            autoBuildTableViewData(tableView_PLog, PMCLogBean.class, tabId, index_PLog);
        });
    }

    /**
     * 清空列表
     */
    @FXML
    private void removeAll() {
        removeTableViewData(tableView_PLog, dataNumber_PLog);
        clickLogs.clear();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

}