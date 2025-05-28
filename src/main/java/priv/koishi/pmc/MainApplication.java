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
import priv.koishi.pmc.Controller.MainController;
import priv.koishi.pmc.ThreadPool.ThreadPoolManager;

import java.io.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.SingleInstanceGuard.SingleInstanceGuard.checkRunning;
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
    private static boolean loadPMC;

    /**
     * 程序启动时的参数
     */
    private static String[] args;

    /**
     * 主控制器
     */
    public static MainController mainController;

    /**
     * 加载fxml页面
     *
     * @param stage 程序主舞台
     * @throws Exception io异常、设置全局异常处理器异常
     */
    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
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
        mainScene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(appName);
        stage.setScene(mainScene);
        setWindowLogo(stage, logoPath);
        mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/Styles.css")).toExternalForm());
        mainController = fxmlLoader.getController();
        TabPane tabPane = mainController.tabPane;
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
        // 关闭线程池
        ThreadPoolManager.shutdownAll();
        // 卸载全局输入监听钩子
        GlobalScreen.unregisterNativeHook();
        // 保存设置
        mainController.saveAllLastConfig();
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
            showStage(mainStage);
        }));
        MenuItem setting = new MenuItem("设置...");
        setting.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        setting.setOnAction(e -> tabPane.getTabs().forEach(tab -> {
            if ("settingTab".equals(tab.getId())) {
                tabPane.getSelectionModel().select(tab);
            }
            showStage(mainStage);
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
                    InputStream in = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    // 读取第一行为激活标记
                    String signal = reader.readLine();
                    if (activatePMC.equals(signal)) {
                        // 弹出程序窗口
                        showWindow(reader);
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
                if (arg.contains(PMC)) {
                    loadPMCPath = arg;
                    if (loadPMCPath.contains(r)) {
                        loadPMCPath = loadPMCPath.substring(loadPMCPath.indexOf(r) + r.length());
                    }
                    loadPMC = true;
                    break;
                }
            }
            Platform.runLater(() -> {
                showStage(mainStage);
                if (loadPMC) {
                    File file = new File(loadPMCPath);
                    logger.info(loadPMCPath);
                    if (file.exists()) {
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setTitle("导入pmc文件");
                        dialog.setHeaderText("是否清空操作列表？");
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        setWindowLogo(stage, logoPath);
                        ButtonType appendButton = new ButtonType("在操作列表下方追加流程", ButtonBar.ButtonData.APPLY);
                        ButtonType clearButton = new ButtonType("清空操作列表后导入流程", ButtonBar.ButtonData.OTHER);
                        ButtonType cancelButton = new ButtonType("取消导入", ButtonBar.ButtonData.CANCEL_CLOSE);
                        dialog.getDialogPane().getButtonTypes().addAll(appendButton, clearButton, cancelButton);
                        ButtonType buttonType = dialog.showAndWait().orElse(cancelButton);
                        if (buttonType == appendButton) {
                            try {
                                autoClickController.loadPMCFile(loadPMCPath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (buttonType == clearButton) {
                            autoClickController.removeAll();
                            try {
                                autoClickController.loadPMCFile(loadPMCPath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 启动程序
     *
     * @param args 启动参数
     * @throws IOException io异常
     */
    public static void main(String[] args) throws IOException {
        MainApplication.args = args;
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
        input.close();
        // 启动时检查是否已经启动
        if (checkRunning(port, args)) {
            System.exit(0);
        }
        // 启动激活监听服务
        startActivationServer(port);
        launch();
    }

}
