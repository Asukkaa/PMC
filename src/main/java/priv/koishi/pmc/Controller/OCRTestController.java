package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.robot.Robot;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.pmc.Bean.OCRDataBean;

import java.util.List;

import static priv.koishi.pmc.Controller.AutoClickController.ocrTestStage;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.UiUtils.copyText;
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
            tableView_Tes.getItems().setAll(data);
            updateTableViewSizeText(tableView_Tes, dataNumber_Tes, unit_data());
            tableView_Tes.refresh();
        }
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 添加列表右键菜单
        ContextMenu tableMenu = new ContextMenu();
        // 复制选择文本选项
        buildCopyTextMenu(tableMenu);
        // 移动鼠标到选中坐标选项
        buildMoveMouseMenu(tableMenu);
        // 移动所选行选项
        buildMoveDataMenu(tableView_Tes, tableMenu);
        // 取消选中选项
        buildClearSelectedMenu(tableView_Tes, tableMenu);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(tableMenu, tableView_Tes);
    }

    /**
     * 复制选择文本选项
     *
     * @param contextMenu 右键菜单
     */
    private void buildCopyTextMenu(ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem(tessdata_copyText());
        menuItem.setOnAction(_ -> {
            ObservableList<OCRDataBean> selectedItems = tableView_Tes.getSelectionModel().getSelectedItems();
            String text = selectedItems.getFirst().getText();
            copyText(text, text_copied() + text);
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 移动鼠标到选中坐标选项
     *
     * @param contextMenu 右键菜单
     */
    private void buildMoveMouseMenu(ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem(tessdata_moveMouse());
        menuItem.setOnAction(_ -> {
            ObservableList<OCRDataBean> selectedItems = tableView_Tes.getSelectionModel().getSelectedItems();
            OCRDataBean first = selectedItems.getFirst();
            int x = Integer.parseInt(first.getX());
            int y = Integer.parseInt(first.getY());
            Robot robot = new Robot();
            Platform.runLater(() -> robot.mouseMove(x, y));
        });
        contextMenu.getItems().add(menuItem);
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
            autoBuildTableViewData(tableView_Tes, OCRDataBean.class, tabId);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Tes);
            // 构建右键菜单
            buildContextMenu();
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
