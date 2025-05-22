package priv.koishi.pmc;

import com.github.kwhat.jnativehook.GlobalScreen;
import de.jangassen.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import priv.koishi.pmc.SingleInstanceGuard.SingleInstanceGuard;
import priv.koishi.pmc.ThreadPool.ThreadPoolManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import static priv.koishi.pmc.Controller.MainController.mainAdaption;
import static priv.koishi.pmc.Controller.MainController.saveAllLastConfig;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
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
    private static Stage primaryStage;

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
    private static boolean loadPMC;

    /**
     * 加载fxml页面
     *
     * @param stage 程序主舞台
     * @throws Exception io异常、设置全局异常处理器异常
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        // 读取fxml页面
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/Main-view.fxml"));
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
        Scene scene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(appName);
        stage.setScene(scene);
        setWindLogo(stage, logoPath);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/Styles.css")).toExternalForm());
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        // 设置默认选中的Tab
        if (!loadPMC && activation.equals(prop.getProperty(key_loadLastConfig, activation))) {
            tabPane.getTabs().forEach(tab -> {
                if (tab.getId().equals(prop.getProperty(key_lastTab, defaultLastTab))) {
                    tabPane.getSelectionModel().select(tab);
                }
            });
        }
        input.close();
        // 初始化macOS系统应用菜单
        initMenu(tabPane);
        // 监听窗口面板宽度变化
        stage.widthProperty().addListener((v1, v2, v3) ->
                Platform.runLater(() -> mainAdaption(stage)));
        // 监听窗口面板高度变化
        stage.heightProperty().addListener((v1, v2, v3) ->
                Platform.runLater(() -> mainAdaption(stage)));
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
        // 关闭线程池
        ThreadPoolManager.shutdownAll();
        // 卸载全局输入监听钩子
        GlobalScreen.unregisterNativeHook();
        // 保存设置
        saveAllLastConfig(primaryStage);
        // 关闭Socket服务
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        logger.info("==============程序退出中====================");
        System.exit(0);
    }

    /**
     * 初始化macOS系统应用菜单
     *
     * @param tabPane 程序页面基础布局
     */
    private void initMenu(TabPane tabPane) {
        MenuItem about = new MenuItem("关于 " + appName);
        about.setOnAction(e -> tabPane.getTabs().forEach(tab -> {
            if ("aboutTab".equals(tab.getId())) {
                tabPane.getSelectionModel().select(tab);
            }
            showStage(primaryStage);
        }));
        MenuItem setting = new MenuItem("设置...");
        setting.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        setting.setOnAction(e -> tabPane.getTabs().forEach(tab -> {
            if ("settingTab".equals(tab.getId())) {
                tabPane.getSelectionModel().select(tab);
            }
            showStage(primaryStage);
        }));
        MenuToolkit.toolkit(Locale.getDefault()).createAboutMenuItem(appName);
        MenuItem hide = MenuToolkit.toolkit(Locale.getDefault()).createHideMenuItem(appName);
        hide.setText("隐藏 " + appName);
        MenuItem hideOthers = MenuToolkit.toolkit(Locale.getDefault()).createHideOthersMenuItem();
        hideOthers.setText("隐藏其他");
        MenuItem quit = MenuToolkit.toolkit(Locale.getDefault()).createQuitMenuItem(appName);
        quit.setText("退出 " + appName);
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
                    Platform.runLater(() -> {
                        // 弹出程序窗口
                        if (primaryStage != null) {
                            showStage(primaryStage);
                        }
                    });
                    socket.close();
                }
            } catch (BindException e) {
                logger.error("端口{}已被占用，无法启动激活服务", port);
            } catch (IOException e) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    logger.error("激活窗口服务异常", e);
                }
            }
        }, "Activation-Server").start();
    }

    /**
     * 启动程序
     *
     * @param args 启动参数
     * @throws IOException io异常
     */
    public static void main(String[] args) throws IOException {
        // 打包后需要手动指定日志配置文件位置
        if (!isRunningFromJar()) {
            String logsPath = getLogsPath();
            File logDirectory = new File(logsPath);
            if (!logDirectory.exists()) {
                if (!logDirectory.mkdirs()) {
                    throw new IOException("日志文件夹创建失败： " + logsPath);
                }
            }
            System.setProperty("log.dir", logsPath);
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(getAppResourcePath(log4j2)));
            Configurator.initialize(null, source);
        }
        logger = LogManager.getLogger(MainApplication.class);
        logger.info("==============程序启动中====================");
        logger.info("启动参数数量: {}", args.length);
        for (int i = 0; i < args.length; i++) {
            // windows --r pmcFilePath 算两个参数，macOS算一个
            if ("--r".equals(args[i])) {
                runPMCFile = true;
            } else if (args[i].contains(PMC)) {
                loadPMCPath = args[i];
                if (loadPMCPath.contains(r)) {
                    loadPMCPath = loadPMCPath.substring(loadPMCPath.indexOf(r) + r.length());
                    runPMCFile = true;
                }
                loadPMC = true;
            }
            logger.info("参数 {}: {}", i, args[i]);
        }
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        int port = Integer.parseInt(prop.getProperty(key_appPort, defaultAppPort));
        input.close();
        // 启动时检查是否已经启动
        if (SingleInstanceGuard.checkRunning(port)) {
            System.exit(0);
        }
        // 启动激活监听服务
        startActivationServer(port);
        Locale.setDefault(Locale.CHINA);
        launch();
    }

}
