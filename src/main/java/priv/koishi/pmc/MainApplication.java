package priv.koishi.pmc;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.github.kwhat.jnativehook.GlobalScreen;
import de.jangassen.MenuToolkit;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import priv.koishi.pmc.Bean.PMCListBean;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Controller.MainController;
import priv.koishi.pmc.Finals.Enum.ThemeEnum;

import java.io.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static priv.koishi.pmc.Controller.AutoClickController.recordTextColorProperty;
import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Controller.MainController.listPMCController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonFinals.isRunningFromIDEA;
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Service.ImageRecognitionService.*;
import static priv.koishi.pmc.Service.PMCFileService.buildPMC;
import static priv.koishi.pmc.Service.PMCFileService.buildPMCS;
import static priv.koishi.pmc.SingleInstanceGuard.SingleInstanceGuard.checkRunning;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
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
     * 用来激活已运行的窗口的 Socket 服务
     */
    private static ServerSocket serverSocket;

    /**
     * 程序启动后要自动加载的 PMC 文件路径
     */
    public static String loadPMCPath;

    /**
     * 程序启动后运行 PMC 流程标志（true-运行 false-不运行）
     */
    public static boolean runPMCFile;

    /**
     * 程序启动后自动加载过 PMC 文件标志（true-加载过 false-没有加载过）
     */
    public static boolean loadPMC;

    /**
     * 程序启动后要自动加载的 PMCS 文件路径
     */
    public static String loadPMCSPath;

    /**
     * 程序启动后运行 PMCS 流程标志（true-运行 false-不运行）
     */
    public static boolean runPMCSFile;

    /**
     * 程序启动后自动加载过 PMCS 文件标志（true-加载过 false-没有加载过）
     */
    public static boolean loadPMCS;

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
     * 当前是否为夜晚模式（true-当前为夜晚模式）
     */
    public static boolean isDarkTheme;

    /**
     * 加载 fxml 页面
     *
     * @param stage 程序主舞台
     * @throws IOException 配置文件读取异常、设置全局异常处理器异常
     */
    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        // 设置外观
        int theme = Integer.parseInt(prop.getProperty(key_theme));
        changeTheme(theme);
        // 读取 fxml 页面
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/Main-view.fxml"), bundle);
        double appWidth = Double.parseDouble(prop.getProperty(key_appWidth, defaultAppWidth));
        if (appWidth >= screenWidth) {
            appWidth = screenWidth * 0.9;
        }
        double appHeight = Double.parseDouble(prop.getProperty(key_appHeight, defaultAppHeight));
        if (appHeight >= screenHeight) {
            appHeight = screenHeight * 0.9;
        }
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
        // 设置 css 样式
        setWindowCss(mainScene, stylesCss);
        mainController = fxmlLoader.getController();
        TabPane tabPane = mainController.tabPane;
        // 设置默认选中的 Tab
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
        // 初始化 macOS 系统应用菜单
        if (isMac) {
            initMenu(tabPane);
        }
        // 监听窗口面板宽度变化
        stage.widthProperty().addListener((_, _, _) ->
                Platform.runLater(mainController::mainAdaption));
        // 监听窗口面板高度变化
        stage.heightProperty().addListener((_, _, _) ->
                Platform.runLater(mainController::mainAdaption));
        stage.setOnCloseRequest(_ -> {
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
        Platform.runLater(() -> Thread.setDefaultUncaughtExceptionHandler((_, exception) ->
                Platform.runLater(() -> showExceptionAlert(exception))));
    }

    /**
     * 程序停止时保存设置并关闭资源
     *
     * @throws Exception 配置文件保存异常、钩子异常、线程池关闭异常
     */
    @Override
    public void stop() throws Exception {
        // 卸载全局输入监听钩子
        GlobalScreen.unregisterNativeHook();
        // 保存设置
        if (mainController != null) {
            mainController.saveAllLastConfig();
        }
        // 关闭 Socket 服务
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        // 停止 JavaFX ui 线程
        Platform.exit();
        logger.info("==============程序退出中====================");
        System.exit(0);
    }

    /**
     * 切换主题
     *
     * @param theme 主题枚举
     */
    public static void changeTheme(int theme) {
        if (theme == ThemeEnum.Light.ordinal()) {
            setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            isDarkTheme = false;
        } else if (theme == ThemeEnum.Dark.ordinal()) {
            setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            isDarkTheme = true;
        } else if (theme == ThemeEnum.Auto.ordinal()) {
            ColorScheme scheme = Platform.getPreferences().getColorScheme();
            if (ColorScheme.DARK == scheme) {
                setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                isDarkTheme = true;
            } else {
                setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                isDarkTheme = false;
            }
        } else if (theme == ThemeEnum.JavaFx.ordinal()) {
            setUserAgentStylesheet(null);
            isDarkTheme = false;
        }
        setTextColorProperty(textColorProperty, isDarkTheme ? Color.WHITE : Color.BLACK);
        setTextColorProperty(recordTextColorProperty, isDarkTheme ? Color.AQUA : Color.BLUE);
    }

    /**
     * 初始化 macOS 系统应用菜单
     *
     * @param tabPane 程序页面基础布局
     */
    private void initMenu(TabPane tabPane) {
        MenuItem about = new MenuItem(macMenu_about() + appName);
        about.setOnAction(_ -> {
            // 只有在程序空闲时才弹出程序窗口
            if (autoClickController.isFree()) {
                tabPane.getSelectionModel().select(mainController.aboutTab);
                showStage(mainStage);
            }
        });
        MenuItem setting = new MenuItem(macMenu_settings());
        setting.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        setting.setOnAction(_ -> {
            // 只有在程序空闲时才弹出程序窗口
            if (autoClickController.isFree()) {
                tabPane.getSelectionModel().select(mainController.settingTab);
                showStage(mainStage);
            }
        });
        MenuToolkit.toolkit(Locale.getDefault()).createAboutMenuItem(appName);
        MenuItem hide = MenuToolkit.toolkit(Locale.getDefault()).createHideMenuItem(appName);
        hide.setText(macMenu_hide() + appName);
        MenuItem hideOthers = MenuToolkit.toolkit(Locale.getDefault()).createHideOthersMenuItem();
        hideOthers.setText(macMenu_hideOther());
        MenuItem quit = MenuToolkit.toolkit(Locale.getDefault()).createQuitMenuItem(appName);
        quit.setText(macMenu_quit() + appName);
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
                } else if (arg.endsWith(PMC) && FilenameUtils.getPrefixLength(arg) != -1) {
                    loadPMCPath = arg;
                    loadPMC = true;
                    break;
                } else if (arg.endsWith(PMCS) && FilenameUtils.getPrefixLength(arg) != -1) {
                    loadPMCSPath = arg;
                    loadPMCS = true;
                    break;
                }
            }
            Platform.runLater(() -> {
                showStage(mainStage);
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle(import_title());
                dialog.setHeaderText(import_header());
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                setWindowLogo(stage, logoPath);
                ButtonType appendButton = new ButtonType(import_append(), ButtonBar.ButtonData.APPLY);
                ButtonType clearButton = new ButtonType(import_clear(), ButtonBar.ButtonData.OTHER);
                ButtonType cancelButton = new ButtonType(import_cancel(), ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().addAll(appendButton, clearButton, cancelButton);
                Button appendBtn = (Button) dialog.getDialogPane().lookupButton(appendButton);
                Button clearBtn = (Button) dialog.getDialogPane().lookupButton(clearButton);
                Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(cancelButton);
                addToolTip(appendBtn, clearBtn, cancelBtn);
                TabPane tabPane = mainController.tabPane;
                if (loadPMC) {
                    File file = new File(loadPMCPath);
                    if (file.exists()) {
                        Tab autoClickTab = mainController.autoClickTab;
                        ButtonType buttonType = dialog.showAndWait().orElse(cancelButton);
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
                if (loadPMCS) {
                    File file = new File(loadPMCSPath);
                    if (file.exists()) {
                        Tab listPMCTab = mainController.listPMCTab;
                        ButtonType buttonType = dialog.showAndWait().orElse(cancelButton);
                        if (buttonType == appendButton) {
                            creatLoadedPMCSTask(file, tabPane, listPMCTab);
                            Thread.ofVirtual()
                                    .name("loadedPMCSTask-vThread")
                                    .start(listPMCController.loadedPMCSTask);
                        } else if (buttonType == clearButton) {
                            autoClickController.removeAll();
                            creatLoadedPMCSTask(file, tabPane, listPMCTab);
                            Thread.ofVirtual()
                                    .name("loadedPMCSTask-vThread")
                                    .start(listPMCController.loadedPMCSTask);
                        }
                    }
                }
                // 清空启动参数
                clearArgs();
            });
        }
    }

    /**
     * 创建加载 PMC 文件任务
     *
     * @param file         要加载的文件
     * @param tabPane      主页面布局
     * @param autoClickTab 自动点击页面
     */
    private static void creatLoadedPMCTask(File file, TabPane tabPane, Tab autoClickTab) {
        TaskBean<ClickPositionVO> taskBean = new TaskBean<>();
        taskBean.setProgressBar(autoClickController.progressBar_Click)
                .setMessageLabel(autoClickController.dataNumber_Click)
                .setTableView(autoClickController.tableView_Click)
                .setDisableNodes(autoClickController.disableNodes);
        autoClickController.loadedPMCTask = buildPMC(taskBean, file);
        bindingTaskNode(autoClickController.loadedPMCTask, taskBean);
        autoClickController.loadedPMCTask.setOnSucceeded(_ -> {
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
     * 创建加载 PMCS 文件任务
     *
     * @param file       要加载的文件
     * @param tabPane    主页面布局
     * @param listPMCTab 批量执行 PMC 文件页面
     */
    private static void creatLoadedPMCSTask(File file, TabPane tabPane, Tab listPMCTab) {
        TaskBean<PMCListBean> taskBean = new TaskBean<>();
        taskBean.setProgressBar(listPMCController.progressBar_List)
                .setMessageLabel(listPMCController.dataNumber_List)
                .setTableView(listPMCController.tableView_List)
                .setDisableNodes(listPMCController.disableNodes);
        listPMCController.loadedPMCSTask = buildPMCS(taskBean, file);
        bindingTaskNode(listPMCController.loadedPMCSTask, taskBean);
        listPMCController.loadedPMCSTask.setOnSucceeded(_ -> {
            taskUnbind(taskBean);
            List<PMCListBean> clickPositionVOS = listPMCController.loadedPMCSTask.getValue();
            listPMCController.addPMCSFile(clickPositionVOS, file.getPath());
            tabPane.getSelectionModel().select(listPMCTab);
            listPMCController.loadedPMCSTask = null;
        });
        listPMCController.loadedPMCSTask.setOnFailed(e -> {
            taskNotSuccess(taskBean, text_taskFailed());
            listPMCController.loadedPMCSTask = null;
            throw new RuntimeException(e.getSource().getException());
        });
    }


    /**
     * 清空启动参数
     */
    public static void clearArgs() {
        loadPMCPath = null;
        loadPMCSPath = null;
        loadPMC = false;
        loadPMCS = false;
        runPMCFile = false;
        runPMCSFile = false;
        args = null;
    }

    /**
     * 启动程序
     *
     * @param args 启动参数
     * @throws IOException log4j 日志配置文件读取异常、应用配置文件读取异常
     */
    public static void main(String[] args) throws IOException {
        MainApplication.args = args;
        String languagePath = "priv.koishi.pmc.language";
        bundle = ResourceBundle.getBundle(languagePath, Locale.getDefault());
        System.setProperty("log.dir", getLogsPath());
        // 打包后需要手动指定日志配置文件位置
        if (!isRunningFromIDEA) {
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
        // 首次运行时如果有对应语言包则使用操作系统设置的语言
        if (activation.equals(firstRunValue)) {
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
        // 获取屏幕参数
        refreshScreenParameters();
        launch(args);
    }

}
