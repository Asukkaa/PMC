package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.MainApplication.mainStage;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningOutputStream;
import static priv.koishi.pmc.Utils.UiUtils.creatTooltip;

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
    public Tab settingTab, aboutTab, autoClickTab, timedStartTab;

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        // 设置tab页的鼠标悬停提示
        tabPane.getTabs().forEach(tab -> tab.setTooltip(creatTooltip(tab.getText())));
        Platform.runLater(() -> {
            aboutController = getController(AboutController.class);
            settingController = getController(SettingController.class);
            autoClickController = getController(AutoClickController.class);
            timedTaskController = getController(TimedTaskController.class);
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
