package priv.koishi.pmc.Controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningOutputStream;
import static priv.koishi.pmc.Utils.UiUtils.creatTooltip;

/**
 * 全局页面控制器
 *
 * @author KOISHI Date:2024-10-02
 * Time:下午1:08
 */
public class MainController {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab settingTab, aboutTab, autoClickTab, timedStartTab;

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        // 设置tab页的鼠标悬停提示
        tabPane.getTabs().forEach(tab -> tab.setTooltip(creatTooltip(tab.getText())));
    }

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void mainAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        tabPane.setStyle("-fx-pref-height: " + stageHeight + "px;");
        // 自动操作工具页设置组件宽度自适应
        AutoClickController.adaption(stage);
        // 设置页组件宽度自适应
        SettingController.adaption(stage);
        // 定时任务页组件宽度自适应
        TimedTaskController.adaption(stage);
    }

    /**
     * 保存各个功能最后一次设置值
     *
     * @throws IOException io异常
     */
    public static void saveAllLastConfig(Stage stage) throws IOException {
        Scene scene = stage.getScene();
        // 保存自动操作工具功能最后设置
        AutoClickController.saveLastConfig(scene);
        // 保存设置功能最后设置
        SettingController.saveLastConfig(scene);
        // 保存关程序闭前页面状态设置
        saveLastConfig(stage);
        // 保存日志文件数量设置
        AboutController.saveLastConfig(scene);
    }

    /**
     * 保存关程序闭前页面状态
     *
     * @throws IOException io异常
     */
    private static void saveLastConfig(Stage stage) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        Scene scene = stage.getScene();
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        prop.put(key_lastTab, selectedTab.getId());
        String fullWindow = stage.isFullScreen() ? activation : unActivation;
        prop.put(key_lastFullWindow, fullWindow);
        String maximize = stage.isMaximized() ? activation : unActivation;
        prop.put(key_lastMaxWindow, maximize);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

}
