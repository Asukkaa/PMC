package priv.koishi.pmc.Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.Bean.CheckUpdateBean;
import priv.koishi.pmc.Bean.TaskBean;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.MainApplication.runPMCFile;
import static priv.koishi.pmc.Service.CheckUpdateService.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 关于页面控制器
 *
 * @author KOISHI
 * Date:2025-01-07
 * Time:16:45
 */
public class AboutController extends RootController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(AboutController.class);

    /**
     * 更新时间格式
     */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 下载更新任务
     */
    private Task<Void> downloadedUpdateTask;

    @FXML
    public ImageView logo_Abt;

    @FXML
    public TextField logsNum_Abt;

    @FXML
    public CheckBox autoCheck_Abt;

    @FXML
    public Label logsPath_Abt, mail_Abt, version_Abt, title_Abt, checkMassage_Abt;

    @FXML
    public Button openBaiduLinkBtn_Abt, openQuarkLinkBtn_Abt, openXunleiLinkBtn_Abt, openGitHubLinkBtn_Abt,
            openGiteeLinkBtn_Abt, appreciate_Abt, checkUpdate_Abt;

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        // 获取日志储存数量配置
        setControlLastConfig(logsNum_Abt, prop, key_logsNum);
        // 获取自动检查更新配置
        setControlLastConfig(autoCheck_Abt, prop, key_autoCheck);
        title_Abt.setTextFill(Color.HOTPINK);
        title_Abt.setText(appName);
        input.close();
    }

    /**
     * 获取logs文件夹路径并展示
     */
    private void setLogsPath() {
        String logsPath = getLogsPath();
        setPathLabel(logsPath_Abt, logsPath);
    }

    /**
     * 保存日志问文件数量设置
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        String logsNumValue = logsNum_Abt.getText();
        prop.setProperty(key_logsNum, logsNumValue);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

    /**
     * 清理多余log文件
     *
     * @throws RuntimeException 删除日志文件失败
     */
    private void deleteLogs() throws IOException {
        String logsNumValue = logsNum_Abt.getText();
        if (StringUtils.isNotBlank(logsNumValue)) {
            File[] files = new File(logsPath_Abt.getText()).listFiles();
            if (files != null) {
                List<File> logList = new ArrayList<>();
                for (File file : files) {
                    if (log.equals(getExistsFileType(file))) {
                        logList.add(file);
                    }
                }
                int logsNum = Integer.parseInt(logsNumValue);
                if (logList.size() > logsNum) {
                    logList.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                    List<File> removeList = logList.stream().skip(logsNum).toList();
                    removeList.forEach(r -> {
                        String path = r.getAbsolutePath();
                        File file = new File(path);
                        if (!file.delete()) {
                            throw new RuntimeException(bundle.getString("about.deleteFailed") + path);
                        }
                    });
                }
            }
        }
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        // 版本号鼠标悬停提示
        addToolTip(tip_version(), version_Abt);
        // 日志文件数量输入框添加鼠标悬停提示
        addValueToolTip(logsNum_Abt, tip_logsNum());
        // 赞赏按钮添加鼠标悬停提示
        addToolTip(tip_appreciate(), appreciate_Abt);
        // 给logo和应用名称添加鼠标悬停提示
        addToolTip(tip_thanks(), logo_Abt, title_Abt);
        // 自动检查更新开关添加鼠标悬停提示
        addToolTip(autoCheck_Abt.getText(), autoCheck_Abt);
        // 检查更新按钮添加鼠标悬停提示
        addToolTip(tip_checkUpdate_Abt(), checkUpdate_Abt);
        // 给github、gitee跳转按钮添加鼠标悬停提示
        addToolTip(tip_openGitLink(), openGitHubLinkBtn_Abt, openGiteeLinkBtn_Abt);
        // 给网盘跳转按钮添加鼠标悬停提示
        addToolTip(tip_openLink(), openBaiduLinkBtn_Abt, openQuarkLinkBtn_Abt, openXunleiLinkBtn_Abt);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(checkUpdate_Abt);
    }

    /**
     * 取消更新
     */
    public void cancelUpdate() {
        if (downloadedUpdateTask != null && downloadedUpdateTask.isRunning()) {
            downloadedUpdateTask.cancel();
        }
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        // 设置版本号
        version_Abt.setText(version);
        // 添加右键菜单
        setCopyValueContextMenu(mail_Abt, bundle.getString("about.copyEmail"));
        // 设置鼠标悬停提示
        setToolTip();
        // 设置要防重复点击的组件
        setDisableNodes();
        // log 文件保留数量输入监听
        integerRangeTextField(logsNum_Abt, 0, null, tip_logsNum());
        // 读取配置文件
        getConfig();
        // 获取logs文件夹路径并展示
        setLogsPath();
        // 清理多余log文件
        deleteLogs();
        // 检查更新
        Platform.runLater(() -> {
            if (autoCheck_Abt.isSelected()) {
                checkUpdate();
            }
        });
    }

    /**
     * 打开百度云盘链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openBaiduLink() throws Exception {
        Desktop.getDesktop().browse(new URI(baiduLink));
    }

    /**
     * 打开夸克云盘链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openQuarkLink() throws Exception {
        Desktop.getDesktop().browse(new URI(quarkLink));
    }

    /**
     * 打开迅雷云盘链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openXunleiLink() throws Exception {
        Desktop.getDesktop().browse(new URI(xunleiLink));
    }

    /**
     * 打开GitHub链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openGitHubLink() throws Exception {
        Desktop.getDesktop().browse(new URI(githubLink));
    }

    /**
     * 打开Gitee链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openGiteeLink() throws Exception {
        Desktop.getDesktop().browse(new URI(giteeLink));
    }

    /**
     * 赞赏界面人口
     *
     * @throws IOException 界面打开失败
     */
    @FXML
    private void appreciate() throws IOException {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/Appreciate-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent root = loader.load();
        Stage detailStage = new Stage();
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        double with = Double.parseDouble(prop.getProperty(key_appreciateWidth, "450"));
        double height = Double.parseDouble(prop.getProperty(key_appreciateHeight, "450"));
        input.close();
        Scene scene = new Scene(root, with, height);
        detailStage.setScene(scene);
        detailStage.setTitle(tip_appreciate());
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setResizable(false);
        setWindowLogo(detailStage, logoPath);
        detailStage.show();
    }

    /**
     * 检查更新
     */
    @FXML
    public void checkUpdate() {
        String lastCheck = bundle.getString("update.lastCheck");
        TaskBean<?> taskBean = new TaskBean<>();
        taskBean.setMassageLabel(checkMassage_Abt)
                .setDisableNodes(disableNodes)
                .setBindingMassageLabel(true);
        Task<CheckUpdateBean> task = checkLatestVersion();
        bindingTaskNode(task, taskBean);
        task.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            CheckUpdateBean updateInfo = task.getValue();
            if (!updateInfo.getVersion().contains(bundle.getString("update.err"))) {
                // 检查是否有新版本
                if (isNewVersionAvailable(updateInfo)) {
                    checkMassage_Abt.setText(bundle.getString("update.findNewVersion") + updateInfo.getVersion()
                            + lastCheck + LocalDateTime.now().format(formatter));
                    checkMassage_Abt.setTextFill(Color.BLUE);
                    if (!runPMCFile || autoClickController == null || autoClickController.isFree()) {
                        // 弹出更新对话框
                        Optional<ButtonType> result = showUpdateDialog(updateInfo);
                        if (result.isPresent() && result.get().getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE) {
                            // 用户选择更新
                            downloadedUpdateTask = downloadAndInstallUpdate(updateInfo);
                            Thread.ofVirtual()
                                    .name("task-downloadedUpdate-vThread")
                                    .start(downloadedUpdateTask);
                            downloadedUpdateTask.setOnFailed(workerStateEvent -> {
                                downloadedUpdateTask = null;
                                try {
                                    logger.info("任务失败，删除临时文件夹： {}", PMCTempPath);
                                    deleteDirectoryRecursively(Path.of(PMCTempPath));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                taskNotSuccess(taskBean, bundle.getString("update.downloadFailed"));
                                throw new RuntimeException(downloadedUpdateTask.getException());
                            });
                            downloadedUpdateTask.setOnCancelled(workerStateEvent ->
                                    downloadedUpdateTask = null);
                        }
                    }
                } else {
                    checkMassage_Abt.setText(bundle.getString("update.nowIsLast")
                            + lastCheck + LocalDateTime.now().format(formatter));
                    checkMassage_Abt.setTextFill(Color.GREEN);
                }
            } else {
                checkMassage_Abt.setText(bundle.getString("update.checkFailed")
                        + lastCheck + LocalDateTime.now().format(formatter));
                checkMassage_Abt.setTextFill(Color.RED);
            }
        });
        task.setOnFailed(event -> {
            taskNotSuccess(taskBean, bundle.getString("update.checkFailed")
                    + lastCheck + LocalDateTime.now().format(formatter));
            throw new RuntimeException(task.getException());
        });
        Thread.ofVirtual()
                .name("task-checkUpdate-vThread")
                .start(task);
    }

    /**
     * 自动检测更新开关
     *
     * @throws IOException 配置文件打开失败
     */
    @FXML
    public void autoCheckAction() throws IOException {
        setLoadLastConfigCheckBox(autoCheck_Abt, configFile, key_autoCheck);
    }

}
