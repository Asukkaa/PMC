package priv.koishi.pmc;

import com.github.kwhat.jnativehook.GlobalScreen;
import de.jangassen.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Controller.MainController;

import java.io.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonFinals.isRunningFromJar;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Service.AutoClickService.loadPMC;
import static priv.koishi.pmc.SingleInstanceGuard.SingleInstanceGuard.checkRunning;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 程序启动类
 *
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:48
 */
public class MainApplication extends Application {

    /**
     * 日志记录器
     */
    private static Logger logger;

    /**
     * 程序主舞台
     */
    public static Stage mainStage;

    /**
     * 程序主场景
     */
    public static Scene mainScene;

    /**
     * 用来激活已运行的窗口的Socket服务
     */
    private static ServerSocket serverSocket;

    /**
     * 程序启动后要自动加载的pmc文件路径
     */
    public static String loadPMCPath;

    /**
     * 程序启动后运行pmc流程标志（true-运行 false-不运行）
     */
    public static boolean runPMCFile;

    /**
     * 重新启动后自动加载过pmc文件标志（true-加载过 false-没有加载过）
     */
    public static boolean loadPMC;

    /**
     * 程序启动时的参数
     */
    public static String[] args;

    /**
     * 主控制器
     */
    public static MainController mainController;

    /**
     * 语言包
     */
    public static ResourceBundle bundle;

    /**
     * 加载fxml页面
     *
     * @param stage 程序主舞台
     * @throws IOException io异常、设置全局异常处理器异常
     */
    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        // 读取fxml页面
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/Main-view.fxml"), bundle);
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        double appWidth = Double.parseDouble(prop.getProperty(key_appWidth, defaultAppWidth));
        double appHeight = Double.parseDouble(prop.getProperty(key_appHeight, defaultAppHeight));
        if (activation.equals(prop.getProperty(key_lastMaxWindow, unActivation))
                && activation.equals(prop.getProperty(key_loadLastMaxWindow, unActivation))) {
            stage.setMaximized(true);
        } else if (activation.equals(prop.getProperty(key_lastFullWindow, unActivation))
                && activation.equals(prop.getProperty(key_loadLastFullWindow, unActivation))) {
            stage.setFullScreen(true);
        }
        mainScene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(appName);
        stage.setScene(mainScene);
        setWindowLogo(stage, logoPath);
        // 设置css样式
        setWindowCss(mainScene, stylesCss);
        mainController = fxmlLoader.getController();
        TabPane tabPane = mainController.tabPane;
        // 设置默认选中的Tab
        if (loadPMC) {
            tabPane.getSelectionModel().select(mainController.autoClickTab);
        } else if (activation.equals(prop.getProperty(key_loadLastConfig, activation))) {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getId().equals(prop.getProperty(key_lastTab, defaultLastTab))) {
                    tabPane.getSelectionModel().select(tab);
                    break;
                }
            }
        }
        input.close();
        // 初始化macOS系统应用菜单
        initMenu(tabPane);
        // 监听窗口面板宽度变化
        stage.widthProperty().addListener((v1, v2, v3) ->
                Platform.runLater(mainController::mainAdaption));
        // 监听窗口面板高度变化
        stage.heightProperty().addListener((v1, v2, v3) ->
                Platform.runLater(mainController::mainAdaption));
        stage.setOnCloseRequest(event -> {
            try {
                stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        stage.show();
        logger.info("--------------程序启动成功-------------------");
    }

    /**
     * 全局异常处理器
     *
     * @throws Exception 全局异常
     */
    @Override
    public void init() throws Exception {
        super.init();
        // 在init()方法中设置全局异常处理器
        Platform.runLater(() -> Thread.setDefaultUncaughtExceptionHandler((e, exception) ->
                showExceptionAlert(exception)));
    }

    /**
     * 程序停止时保存设置并关闭资源
     *
     * @throws IOException io异常、钩子异常、线程池关闭异常
     */
    @Override
    public void stop() throws Exception {
        // 卸载全局输入监听钩子
        GlobalScreen.unregisterNativeHook();
        // 保存设置
        if (mainController != null) {
            mainController.saveAllLastConfig();
        }
        // 关闭Socket服务
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        // 停止 javafx ui 线程
        Platform.exit();
        logger.info("==============程序退出中====================");
        System.exit(0);
    }

    /**
     * 初始化macOS系统应用菜单
     *
     * @param tabPane 程序页面基础布局
     */
    private void initMenu(TabPane tabPane) {
        MenuItem about = new MenuItem(bundle.getString("macMenu.about") + appName);
        about.setOnAction(e -> {
            // 只有在程序空闲时才弹出程序窗口
            if (autoClickController.isFree()) {
                tabPane.getSelectionModel().select(mainController.aboutTab);
                showStage(mainStage);
            }
        });
        MenuItem setting = new MenuItem(bundle.getString("macMenu.settings"));
        setting.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        setting.setOnAction(e -> {
            // 只有在程序空闲时才弹出程序窗口
            if (autoClickController.isFree()) {
                tabPane.getSelectionModel().select(mainController.settingTab);
                showStage(mainStage);
            }
        });
        MenuToolkit.toolkit(Locale.getDefault()).createAboutMenuItem(appName);
        MenuItem hide = MenuToolkit.toolkit(Locale.getDefault()).createHideMenuItem(appName);
        hide.setText(bundle.getString("macMenu.hide") + appName);
        MenuItem hideOthers = MenuToolkit.toolkit(Locale.getDefault()).createHideOthersMenuItem();
        hideOthers.setText(bundle.getString("macMenu.hideOther"));
        MenuItem quit = MenuToolkit.toolkit(Locale.getDefault()).createQuitMenuItem(appName);
        quit.setText(bundle.getString("macMenu.quit") + appName);
        Menu menu = new Menu();
        menu.getItems().addAll(about, new SeparatorMenuItem(), setting, new SeparatorMenuItem(),
                hide, hideOthers, new SeparatorMenuItem(), quit);
        MenuToolkit.toolkit(Locale.getDefault()).setApplicationMenu(menu);
    }

    /**
     * 激活已运行的程序窗口
     *
     * @param port 激活信号端口
     */
    private static void startActivationServer(int port) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    InputStream in = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    // 读取第一行为激活标记
                    String signal = reader.readLine();
                    if (activatePMC.equals(signal)) {
                        // 只有在程序空闲时才弹出程序窗口
                        if (autoClickController.isFree()) {
                            showWindow(reader);
                        }
                    }
                    socket.close();
                }
            } catch (BindException e) {
                // 发送激活正在运行的程序窗口信号
                try (Socket socket = new Socket("localhost", port)) {
                    OutputStream out = socket.getOutputStream();
                    String payload = activatePMC + "\n" + String.join("\n", args);
                    out.write(payload.getBytes());
                    logger.info("程序正在运行，发送激活信号及参数: {}", payload);
                    System.exit(0);
                } catch (ConnectException ce) {
                    logger.error("端口{}被其他进程占用", port);
                } catch (IOException ex) {
                    logger.error("端口检测异常: {}", ex.getMessage());
                    System.exit(1);
                }
            } catch (IOException e) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    logger.error("激活窗口服务异常", e);
                }
            }
        }, "Activation-Server").start();
    }

    /**
     * 弹出程序窗口
     *
     * @param reader 输入流
     */
    private static void showWindow(BufferedReader reader) {
        // 弹出程序窗口
        if (mainStage != null) {
            // 读取后续行作为参数
            List<String> receivedArgs = new ArrayList<>();
            String line;
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) {
                        break;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                receivedArgs.add(line);
            }
            for (String arg : receivedArgs) {
                logger.info("程序运行时接收到参数: {}", arg);
                // 只处理手动打开文件的行为，忽略自动任务导入
                if (arg.contains(r.trim())) {
                    break;
                } else if (arg.contains(PMC)) {
                    loadPMCPath = arg;
                    loadPMC = true;
                    break;
                }
            }
            Platform.runLater(() -> {
                showStage(mainStage);
                if (loadPMC) {
                    File file = new File(loadPMCPath);
                    if (file.exists()) {
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setTitle(bundle.getString("showWindow.import.title"));
                        dialog.setHeaderText(bundle.getString("showWindow.import.header"));
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        setWindowLogo(stage, logoPath);
                        ButtonType appendButton = new ButtonType(bundle.getString("showWindow.import.append"),
                                ButtonBar.ButtonData.APPLY);
                        ButtonType clearButton = new ButtonType(bundle.getString("showWindow.import.clear"),
                                ButtonBar.ButtonData.OTHER);
                        ButtonType cancelButton = new ButtonType(bundle.getString("showWindow.import.cancel"),
                                ButtonBar.ButtonData.CANCEL_CLOSE);
                        dialog.getDialogPane().getButtonTypes().addAll(appendButton, clearButton, cancelButton);
                        ButtonType buttonType = dialog.showAndWait().orElse(cancelButton);
                        TabPane tabPane = mainController.tabPane;
                        Tab autoClickTab = mainController.autoClickTab;
                        if (buttonType == appendButton) {
                            creatLoadedPMCTask(file, tabPane, autoClickTab);
                            Thread.ofVirtual()
                                    .name("loadedPMCTask-vThread")
                                    .start(autoClickController.loadedPMCTask);
                        } else if (buttonType == clearButton) {
                            autoClickController.removeAll();
                            creatLoadedPMCTask(file, tabPane, autoClickTab);
                            Thread.ofVirtual()
                                    .name("loadedPMCTask-vThread")
                                    .start(autoClickController.loadedPMCTask);
                        }
                    }
                }
                // 清空启动参数
                clearArgs();
            });
        }
    }

    /**
     * 创建加载pmc文件任务
     *
     * @param file         要加载的文件
     * @param tabPane      主页面布局
     * @param autoClickTab 自动点击页面
     */
    private static void creatLoadedPMCTask(File file, TabPane tabPane, Tab autoClickTab) {
        TaskBean<ClickPositionVO> taskBean = new TaskBean<>();
        taskBean.setProgressBar(autoClickController.progressBar_Click)
                .setMassageLabel(autoClickController.dataNumber_Click)
                .setTableView(autoClickController.tableView_Click)
                .setDisableNodes(autoClickController.disableNodes);
        autoClickController.loadedPMCTask = loadPMC(taskBean, file);
        bindingTaskNode(autoClickController.loadedPMCTask, taskBean);
        autoClickController.loadedPMCTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            List<ClickPositionVO> clickPositionVOS = autoClickController.loadedPMCTask.getValue();
            autoClickController.addAutoClickPositions(clickPositionVOS, file.getPath());
            tabPane.getSelectionModel().select(autoClickTab);
            autoClickController.loadedPMCTask = null;
        });
        autoClickController.loadedPMCTask.setOnFailed(e -> {
            taskNotSuccess(taskBean, text_taskFailed());
            autoClickController.loadedPMCTask = null;
            throw new RuntimeException(e.getSource().getException());
        });
    }

    /**
     * 清空启动参数
     */
    public static void clearArgs() {
        loadPMCPath = null;
        loadPMC = false;
        runPMCFile = false;
        args = null;
    }

    /**
     * 启动程序
     *
     * @param args 启动参数
     * @throws IOException io异常
     */
    public static void main(String[] args) throws IOException {
        MainApplication.args = args;
        String languagePath = "priv.koishi.pmc.language";
        bundle = ResourceBundle.getBundle(languagePath, Locale.getDefault());
        System.setProperty("log.dir", getLogsPath());
        // 打包后需要手动指定日志配置文件位置
        if (!isRunningFromJar) {
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(getAppResourcePath(log4j2)));
            Configurator.initialize(null, source);
        }
        logger = LogManager.getLogger(MainApplication.class);
        logger.info("==============程序启动中====================");
        logger.info("启动参数数量: {}", args.length);
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            // windows --r pmcFilePath 算两个参数，macOS算一个
            if (r.trim().equals(arg)) {
                runPMCFile = true;
            } else if (arg.contains(PMC)) {
                loadPMCPath = arg;
                if (loadPMCPath.contains(r)) {
                    loadPMCPath = loadPMCPath.substring(loadPMCPath.indexOf(r) + r.length());
                    runPMCFile = true;
                }
                loadPMC = true;
            }
            logger.info("参数 {}: {}", i, arg);
        }
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        int port = Integer.parseInt(prop.getProperty(key_appPort, defaultAppPort));
        String firstRunValue = prop.getProperty(key_firstRun);
        boolean firstRun = activation.equals(firstRunValue);
        // 首次运行时如果有对应语言包则使用操作系统设置的语言
        if (firstRun) {
            if (languageMap.containsKey(Locale.getDefault())) {
                Locale locale = languageMap.getKey(prop.getProperty(key_language));
                Locale.setDefault(locale);
                bundle = ResourceBundle.getBundle(languagePath, locale);
                // 处理非zh_TW的繁体中文
            } else if (Locale.getDefault().getDisplayName().contains("繁體")) {
                Locale.setDefault(Locale.TRADITIONAL_CHINESE);
                bundle = ResourceBundle.getBundle(languagePath, Locale.getDefault());
            } else {
                Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
                bundle = ResourceBundle.getBundle(languagePath, Locale.getDefault());
            }
            updateProperties(configFile, key_firstRun, unActivation);
        } else {
            Locale locale = languageMap.getKey(prop.getProperty(key_language));
            Locale.setDefault(locale);
            bundle = ResourceBundle.getBundle(languagePath, locale);
        }
        // 更新映射文本
        updateAllDynamicTexts();
        input.close();
        // 启动时检查是否已经启动
        if (checkRunning(port, args)) {
            System.exit(0);
        }
        // 启动激活监听服务
        startActivationServer(port);
        launch(args);
    }

}
