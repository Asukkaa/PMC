package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.addToolTip;
import static priv.koishi.pmc.Utils.UiUtils.setControlLastConfig;

/**
 * 设置页面控制器
 *
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingController {

    @FXML
    private AnchorPane anchorPane_Set;

    @FXML
    private VBox vBox_Set;

    @FXML
    private CheckBox lastTab_Set, fullWindow_Set, loadAutoClick_Set, hideWindowRun_Set, showWindowRun_Set,
            hideWindowRecord_Set, showWindowRecord_Set, firstClick_Set, floatingRun_Set, floatingRecord_Set;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void settingAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        Node settingVBox = scene.lookup("#vBox_Set");
        settingVBox.setLayoutX(stageWidth * 0.03);
    }

    /**
     * 根据是否加载最后一次功能选项框选择值更新相关配置文件
     *
     * @param checkBox   更改配置的选项框
     * @param configFile 要更新的配置文件相对路径
     * @param key        要更新的配置
     * @throws IOException io异常
     */
    private void setLoadLastConfigCheckBox(CheckBox checkBox, String configFile, String key) throws IOException {
        if (checkBox.isSelected()) {
            updateProperties(configFile, key, activation);
        } else {
            updateProperties(configFile, key, unActivation);
        }
    }

    /**
     * 设置是否加载最后一次功能配置信息初始值
     *
     * @param prop     要读取的配置文件对象
     * @param checkBox 更改配置的选项框
     * @throws IOException io异常
     */
    private void setLoadLastConfig(Properties prop, CheckBox checkBox) throws IOException {
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        setControlLastConfig(checkBox, prop, key_loadLastConfig, false, null);
        setControlLastConfig(firstClick_Set, prop, key_lastFirstClick, false, null);
        setControlLastConfig(hideWindowRun_Set, prop, key_lastHideWindowRun, false, null);
        setControlLastConfig(showWindowRun_Set, prop, key_lastShowWindowRun, false, null);
        setControlLastConfig(hideWindowRecord_Set, prop, key_lastHideWindowRecord, false, null);
        setControlLastConfig(showWindowRecord_Set, prop, key_lastShowWindowRecord, false, null);
        input.close();
    }

    /**
     * 读取配置文件
     *
     * @param prop 要读取的配置文件对象
     * @throws IOException io异常
     */
    private void getConfig(Properties prop) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        setControlLastConfig(lastTab_Set, prop, key_loadLastConfig, false, null);
        setControlLastConfig(fullWindow_Set, prop, key_loadLastFullWindow, false, null);
        input.close();
    }

    /**
     * 设置是否加载最后一次功能配置信息初始值
     *
     * @throws IOException io异常
     */
    private void setLoadLastConfigs() throws IOException {
        Properties prop = new Properties();
        setLoadLastConfig(prop, loadAutoClick_Set);
        getConfig(prop);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(lastTab_Set.getText(), lastTab_Set);
        addToolTip(fullWindow_Set.getText(), fullWindow_Set);
        addToolTip(tip_firstClick, firstClick_Set);
        addToolTip(tip_floatingRun, floatingRun_Set);
        addToolTip(tip_hideWindowRun, hideWindowRun_Set);
        addToolTip(tip_showWindowRun, showWindowRun_Set);
        addToolTip(tip_floatingRecord, floatingRecord_Set);
        addToolTip(tip_hideWindowRecord, hideWindowRecord_Set);
        addToolTip(tip_showWindowRecord, showWindowRecord_Set);
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        // 设置是否加载最后一次功能配置信息初始值
        setLoadLastConfigs();
        // 设置鼠标悬停提示
        setToolTip();
    }

    /**
     * 自动操作工具功能加载上次设置信息
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadAutoClickAction() throws IOException {
        setLoadLastConfigCheckBox(loadAutoClick_Set, configFile_Click, key_loadLastConfig);
    }

    /**
     * 记住关闭前打开的页面设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadLastTabAction() throws IOException {
        setLoadLastConfigCheckBox(lastTab_Set, configFile, key_loadLastConfig);
    }

    /**
     * 记住窗口是否最大化设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFullWindowAction() throws IOException {
        setLoadLastConfigCheckBox(fullWindow_Set, configFile, key_loadLastFullWindow);
    }

    /**
     * 执行自动流程前最小化本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadHideWindowRunAction() throws IOException {
        setLoadLastConfigCheckBox(hideWindowRun_Set, configFile_Click, key_lastHideWindowRun);
    }

    /**
     * 执行自动流程结束后弹出本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadShowWindowRunAction() throws IOException {
        setLoadLastConfigCheckBox(showWindowRun_Set, configFile_Click, key_lastShowWindowRun);
    }

    /**
     * 录制自动流程前最小化本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadHideWindowRecordAction() throws IOException {
        setLoadLastConfigCheckBox(hideWindowRecord_Set, configFile_Click, key_lastHideWindowRecord);
    }

    /**
     * 录制自动流程结束后弹出本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadShowWindowRecordAction() throws IOException {
        setLoadLastConfigCheckBox(showWindowRecord_Set, configFile_Click, key_lastShowWindowRecord);
    }

    /**
     * 执行自动流程前点击第一个起始坐标
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFirstClickAction() throws IOException {
        setLoadLastConfigCheckBox(firstClick_Set, configFile_Click, key_lastFirstClick);
    }

    /**
     * 执行自动流程时显示信息浮窗
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFloatingRunAction() throws IOException {
        setLoadLastConfigCheckBox(floatingRun_Set, configFile_Click, key_loadFloatingRun);
    }

    /**
     * 录制自动流程时显示信息浮窗
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFloatingRecordAction() throws IOException {
        setLoadLastConfigCheckBox(floatingRecord_Set, configFile_Click, key_loadFloatingRecord);
    }

    /**
     * 重启程序按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void reLaunch() throws IOException {
        Platform.exit();
        if (!isRunningFromJar()) {
            ProcessBuilder processBuilder = null;
            if (systemName.contains(win)) {
                String path = userDir.substring(0, userDir.lastIndexOf(Tools) + Tools.length());
                String appPath = path + File.separator + "Tools.exe";
                processBuilder = new ProcessBuilder(appPath);
            } else if (systemName.contains(macos)) {
                String appName = File.separator + "Tools.app";
                String appPath = userDir.substring(0, userDir.lastIndexOf(appName)) + appName;
                processBuilder = new ProcessBuilder("open", "-n", appPath);
            }
            if (processBuilder != null) {
                processBuilder.start();
            }
        }
    }

    /**
     * 文件查询默认排序设置监听
     *
     * @throws IOException io异常
     */
    @FXML
    private void sortAction() throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

}
