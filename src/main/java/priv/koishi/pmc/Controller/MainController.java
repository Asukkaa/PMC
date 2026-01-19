package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.MainApplication.mainStage;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningOutputStream;
import static priv.koishi.pmc.Utils.TableViewUtils.dragDataFormat;
import static priv.koishi.pmc.Utils.ToolTipUtils.creatTooltip;
import static priv.koishi.pmc.Utils.UiUtils.findTabById;

/**
 * 全局页面控制器
 *
 * @author KOISHI Date:2024-10-02
 * Time:下午1:08
 */
public class MainController extends RootController {

    /**
     * 关于页面控制器
     */
    public static AboutController aboutController;

    /**
     * 设置页面控制器
     */
    public static SettingController settingController;

    /**
     * 自动操作工具页面控制器
     */
    public static AutoClickController autoClickController;

    /**
     * 定时任务页面控制器
     */
    public static TimedTaskController timedTaskController;

    @FXML
    public TabPane tabPane;

    @FXML
    public Tab settingTab, listPMCTab, aboutTab, autoClickTab, timedStartTab;

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        // 设置 Tab 页的鼠标悬停提示
        tabPane.getTabs().forEach(tab -> tab.setTooltip(creatTooltip(tab.getText())));
        Platform.runLater(() -> {
            aboutController = getController(AboutController.class);
            settingController = getController(SettingController.class);
            autoClickController = getController(AutoClickController.class);
            timedTaskController = getController(TimedTaskController.class);
            setupTabDragging();
        });
    }

    /**
     * 组件自适应宽高
     */
    public void mainAdaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tabPane.setStyle("-fx-pref-height: " + stageHeight + "px;");
        // 自动操作工具页设置组件宽度自适应
        autoClickController.adaption();
        // 设置页组件宽度自适应
        settingController.adaption();
        // 定时任务页组件宽度自适应
        timedTaskController.adaption();
    }

    /**
     * 设置 Tab 拖拽排序功能
     */
    private void setupTabDragging() {
        // 为每个 Tab 添加拖拽图标
        for (Tab tab : tabPane.getTabs()) {
            Label dragHandle = new Label(tab.getText());
            tab.setText("");
            tab.setGraphic(dragHandle);
            // 设置拖拽事件
            setupDragEvents(dragHandle, tab);
        }
        // 为 TabPane 添加边界检测的拖拽事件
        setupTabPaneDragEvents();
    }

    /**
     * 为 TabPane 设置边界检测的拖拽事件
     */
    private void setupTabPaneDragEvents() {
        // 拖拽越过 - 检测边界
        tabPane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString() && findTabById(db.getString(), tabPane) != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                // 边界检测：如果拖拽到最左或最右侧，提供视觉反馈
                double x = event.getX();
                double paneWidth = tabPane.getWidth();
                // 边界阈值
                double threshold = 50;
                if (x < threshold) {
                    // 左侧边界反馈
                    tabPane.setStyle("-fx-border-color: #0078d4; -fx-border-width: 2 0 0 0;");
                } else if (x > paneWidth - threshold) {
                    // 右侧边界反馈
                    tabPane.setStyle("-fx-border-color: #0078d4; -fx-border-width: 0 0 0 2;");
                } else {
                    // 恢复正常样式
                    tabPane.setStyle("");
                }
            }
            event.consume();
        });
        // 拖拽释放 - 处理边界情况
        tabPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String draggedTabId = db.getString();
                Tab draggedTab = findTabById(draggedTabId, tabPane);
                if (draggedTab != null) {
                    double x = event.getX();
                    double paneWidth = tabPane.getWidth();
                    // 边界阈值
                    double threshold = 50;
                    int targetIndex;
                    if (x < threshold) {
                        // 移动到最左侧
                        targetIndex = 0;
                    } else if (x > paneWidth - threshold) {
                        // 移动到最右侧
                        targetIndex = tabPane.getTabs().size() - 1;
                    } else {
                        // 恢复正常样式并退出（不处理中间区域）
                        tabPane.setStyle("");
                        event.setDropCompleted(false);
                        event.consume();
                        return;
                    }
                    int draggedIndex = tabPane.getTabs().indexOf(draggedTab);
                    if (draggedIndex != targetIndex) {
                        // 移动 Tab 到目标位置
                        tabPane.getTabs().remove(draggedTab);
                        tabPane.getTabs().add(targetIndex, draggedTab);
                        tabPane.getSelectionModel().select(draggedTab);
                        success = true;
                    }
                }
            }
            // 恢复正常样式
            tabPane.setStyle("");
            event.setDropCompleted(success);
            event.consume();
        });
        // 拖拽退出时恢复样式
        tabPane.setOnDragExited(event -> {
            tabPane.setStyle("");
            event.consume();
        });
    }

    /**
     * 为拖拽图标设置拖拽事件
     *
     * @param dragHandle 拖拽图标
     * @param tab        拖拽图标对应的 Tab
     */
    private void setupDragEvents(Label dragHandle, Tab tab) {
        // 拖拽检测 - 使用自定义数据格式，防止被其他应用读取
        dragHandle.setOnDragDetected(event -> {
            Dragboard db = dragHandle.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            // 使用自定义数据格式，而不是普通字符串， 这样其他应用无法识别这个格式
            content.put(dragDataFormat, tab.getId());
            db.setContent(content);
            // 设置拖拽图标
            db.setDragView(dragHandle.snapshot(null, null));
            event.consume();
        });
        // 拖拽完成
        dragHandle.setOnDragDone(event -> {
            // 拖拽完成后恢复样式
            tabPane.setStyle("");
            event.consume();
        });
        // 拖拽越过 - 应用到所有 Tab 的拖拽图标
        for (Tab otherTab : tabPane.getTabs()) {
            if (otherTab.getGraphic() != null && !otherTab.equals(tab)) {
                otherTab.getGraphic().setOnDragOver(event -> {
                    Dragboard db = event.getDragboard();
                    // 只接受自定义格式的拖拽数据
                    if (db.hasContent(dragDataFormat)) {
                        String draggedTabId = (String) db.getContent(dragDataFormat);
                        if (!draggedTabId.equals(otherTab.getId())) {
                            event.acceptTransferModes(TransferMode.MOVE);
                        }
                    }
                    event.consume();
                });
                // 拖拽释放
                otherTab.getGraphic().setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasContent(dragDataFormat)) {
                        String draggedTabId = (String) db.getContent(dragDataFormat);
                        Tab draggedTab = findTabById(draggedTabId, tabPane);
                        if (draggedTab != null && !draggedTab.equals(otherTab)) {
                            int draggedIndex = tabPane.getTabs().indexOf(draggedTab);
                            int targetIndex = tabPane.getTabs().indexOf(otherTab);
                            if (draggedIndex != targetIndex) {
                                // 移动 Tab 到目标位置
                                tabPane.getTabs().remove(draggedTab);
                                tabPane.getTabs().add(targetIndex, draggedTab);
                                tabPane.getSelectionModel().select(draggedTab);
                                success = true;
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });
            }
        }
    }

    /**
     * 保存各个功能最后一次设置值
     *
     * @throws IOException 配置文件保存异常
     */
    public void saveAllLastConfig() throws IOException {
        // 保存自动操作工具功能最后设置
        if (autoClickController != null) {
            autoClickController.saveLastConfig();
        }
        // 保存设置功能最后设置
        if (settingController != null) {
            settingController.saveLastConfig();
        }
        // 保存日志文件数量设置
        if (aboutController != null) {
            aboutController.saveLastConfig();
        }
        // 保存关程序闭前页面状态设置
        saveLastConfig();
    }

    /**
     * 保存关程序闭前页面状态
     *
     * @throws IOException 配置文件保存异常
     */
    private void saveLastConfig() throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        prop.put(key_lastTab, selectedTab.getId());
        String fullWindow = mainStage.isFullScreen() ? activation : unActivation;
        prop.put(key_lastFullWindow, fullWindow);
        String maximize = mainStage.isMaximized() ? activation : unActivation;
        prop.put(key_lastMaxWindow, maximize);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

}
