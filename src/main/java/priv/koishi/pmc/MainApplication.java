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
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import priv.koishi.pmc.ThreadPool.CommonThreadPoolExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static priv.koishi.pmc.Controller.MainController.mainAdaption;
import static priv.koishi.pmc.Controller.MainController.saveLastConfig;
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
     * 程序主舞台
     */
    private Stage primaryStage;

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    private final ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

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
        double appWidth = Double.parseDouble(prop.getProperty(key_appWidth));
        double appHeight = Double.parseDouble(prop.getProperty(key_appHeight));
        if (activation.equals(prop.getProperty(key_lastMaxWindow)) && activation.equals(prop.getProperty(key_loadLastMaxWindow))) {
            stage.setMaximized(true);
        } else if (activation.equals(prop.getProperty(key_lastFullWindow)) && activation.equals(prop.getProperty(key_loadLastFullWindow))) {
            stage.setFullScreen(true);
        }
        Scene scene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(appName);
        stage.setScene(scene);
        setWindLogo(stage, logoPath);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/Styles.css")).toExternalForm());
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        // 设置默认选中的Tab
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            tabPane.getTabs().forEach(tab -> {
                if (tab.getId().equals(prop.getProperty(key_lastTab))) {
                    tabPane.getSelectionModel().select(tab);
                }
            });
        }
        input.close();
        // 初始化macOS系统应用菜单
        initMenu(tabPane);
        // 监听窗口面板宽度变化
        stage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage)));
        // 监听窗口面板高度变化
        stage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage)));
        // 注册全局输入监听器
        GlobalScreen.registerNativeHook();
        stage.setOnCloseRequest(event -> {
            try {
                stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        stage.show();
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
        Platform.runLater(() -> Thread.setDefaultUncaughtExceptionHandler((e, exception) -> showExceptionAlert(exception)));
    }

    /**
     * 程序停止时保存设置并关闭资源
     *
     * @throws IOException io异常、钩子异常、线程池关闭异常
     */
    @Override
    public void stop() throws Exception {
        if (executorService != null) {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
        saveLastConfig(primaryStage);
        GlobalScreen.unregisterNativeHook();
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
        menu.getItems().addAll(about, new SeparatorMenuItem(), setting, new SeparatorMenuItem(), hide, hideOthers, new SeparatorMenuItem(), quit);
        MenuToolkit.toolkit(Locale.getDefault()).setApplicationMenu(menu);
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
        launch();
    }

}
