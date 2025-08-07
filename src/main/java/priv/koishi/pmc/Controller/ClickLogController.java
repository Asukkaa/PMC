package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.pmc.Bean.ClickLogBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static priv.koishi.pmc.Finals.i18nFinal.text_log;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 操作记录页面控制器
 *
 * @author KOISHI
 * Date:2025-05-08
 * Time:12:26
 */
public class ClickLogController extends RootController {

    /**
     * 页面标识符
     */
    private static final String tabId = "_Log";

    /**
     * 详情页页面舞台
     */
    private Stage stage;

    /**
     * 操作记录
     */
    @Getter
    public List<ClickLogBean> clickLogs = new CopyOnWriteArrayList<>();

    /**
     * 更新数据用的回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    public AnchorPane anchorPane_Log;

    @FXML
    public HBox fileNumberHBox_Log;

    @FXML
    public Button removeAll_Log;

    @FXML
    public Label dataNumber_Log;

    @FXML
    public TableView<ClickLogBean> tableView_Log;

    @FXML
    public TableColumn<ClickLogBean, Integer> index_Log;

    @FXML
    public TableColumn<ClickLogBean, String> date_Log, name_Log, type_Log, X_Log, Y_Log, clickTime_Log,
            clickKey_Log, result_Log;

    /**
     * 组件宽高自适应
     */
    public void adaption() {
        double tableWidth = stage.getWidth() * 0.95;
        tableView_Log.setMaxWidth(tableWidth);
        tableView_Log.setPrefWidth(tableWidth);
        tableView_Log.setPrefHeight(stage.getHeight() * 0.8);
        regionRightAlignment(fileNumberHBox_Log, tableWidth, dataNumber_Log);
        bindPrefWidthProperty();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.05));
        date_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.2));
        name_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.15));
        type_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.1));
        X_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.1));
        Y_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.1));
        clickTime_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.1));
        clickKey_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.1));
        result_Log.prefWidthProperty().bind(tableView_Log.widthProperty().multiply(0.1));
    }

    /**
     * 初始化数据
     *
     * @param logs 操作记录
     */
    public void initData(List<ClickLogBean> logs) {
        clickLogs = logs;
        if (CollectionUtils.isNotEmpty(logs)) {
            tableView_Log.getItems().addAll(logs);
            updateTableViewSizeText(tableView_Log, dataNumber_Log, text_log());
            tableView_Log.refresh();
        }
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_Log.getScene().getWindow();
            // 组件宽高自适应
            adaption();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Log, ClickLogBean.class, tabId, index_Log);
        });
    }

    /**
     * 清空列表
     */
    @FXML
    private void removeAll() {
        removeTableViewData(tableView_Log, dataNumber_Log, null);
        clickLogs.clear();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

}
