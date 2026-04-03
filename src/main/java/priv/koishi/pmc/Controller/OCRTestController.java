package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.pmc.Bean.OCRDataBean;

import java.util.List;

import static priv.koishi.pmc.Controller.AutoClickController.ocrTestStage;
import static priv.koishi.pmc.Finals.i18nFinal.tip_removeAll_Log;
import static priv.koishi.pmc.Finals.i18nFinal.unit_data;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.UiUtils.manuallyChangeThemePane;

/**
 * OCR 测试结果页面控制器
 *
 * @author applesaucepenguin
 * Date 2026-04-03
 * time 15:59
 */
public class OCRTestController extends ManuallyChangeThemeController {

    /**
     * 页面标识符
     */
    private final String tabId = "_Tes";

    /**
     * 更新数据用的回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    public ScrollPane scrollPane_Tes;

    @FXML
    public Label dataNumber_Tes;

    @FXML
    public Button removeAll_Tes;

    @FXML
    public TableView<OCRDataBean> tableView_Tes;

    @FXML
    public TableColumn<OCRDataBean, Integer> index_Tes;

    @FXML
    public TableColumn<OCRDataBean, String> text_Tes, x_Tes, y_Tes, confidenceString_Tes;

    /**
     * 组件宽高自适应
     */
    public void adaption() {
        double tableWidth = ocrTestStage.getWidth() * 0.95;
        tableView_Tes.setMaxWidth(tableWidth);
        tableView_Tes.setPrefWidth(tableWidth);
        tableView_Tes.setPrefHeight(ocrTestStage.getHeight() * 0.8);
        bindPrefWidthProperty();
    }

    /**
     * 设置 JavaFX 单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Tes.prefWidthProperty().bind(tableView_Tes.widthProperty().multiply(0.2));
        text_Tes.prefWidthProperty().bind(tableView_Tes.widthProperty().multiply(0.4));
        x_Tes.prefWidthProperty().bind(tableView_Tes.widthProperty().multiply(0.2));
        y_Tes.prefWidthProperty().bind(tableView_Tes.widthProperty().multiply(0.2));
    }

    /**
     * 初始化数据
     *
     * @param data 识别结果
     */
    public void initData(List<? extends OCRDataBean> data) {
        if (CollectionUtils.isNotEmpty(data)) {
            tableView_Tes.getItems().addAll(data);
            updateTableViewSizeText(tableView_Tes, dataNumber_Tes, unit_data());
            tableView_Tes.refresh();
        }
    }

    /**
     * 手动处理主题切换
     */
    @Override
    void manuallyChangeTheme() {
        manuallyChangeThemePane(scrollPane_Tes, getClass());
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        // 手动处理主题切换
        manuallyChangeTheme();
        Platform.runLater(() -> {
            // 设置页面关闭事件处理逻辑
            ocrTestStage.setOnCloseRequest(_ -> {
                removeController();
                ocrTestStage = null;
            });
            // 组件宽高自适应
            adaption();
            // 添加鼠标悬停提示
            addToolTip(tip_removeAll_Log(), removeAll_Tes);
            // 自动填充 JavaFX 表格
            autoBuildTableViewData(tableView_Tes, OCRDataBean.class, tabId, index_Tes);
        });
    }

    /**
     * 清空列表按钮
     */
    @FXML
    private void removeAll() {
        removeTableViewData(tableView_Tes, dataNumber_Tes);
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

}
