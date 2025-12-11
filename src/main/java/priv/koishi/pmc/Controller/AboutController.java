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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.Bean.CheckUpdateBean;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.UI.CustomProgressDialog.ProgressDialog;

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
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.MainApplication.runPMCFile;
import static priv.koishi.pmc.Service.CheckUpdateService.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ListenerUtils.integerRangeTextField;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.ToolTipUtils.addValueToolTip;
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
     * 页面标识符
     */
    private final String tabId = "_Abt";

    /**
     * 更新时间格式
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    /**
     * 要防重复点击的组件
     */
    private final List<Node> disableNodes = new ArrayList<>();

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
    public Label logsPath_Abt, mail_Abt, version_Abt, title_Abt, checkMassage_Abt, checkDate_Abt;

    @FXML
    public Button openBaiduLinkBtn_Abt, openQuarkLinkBtn_Abt, openXunleiLinkBtn_Abt, openGitHubLinkBtn_Abt,
            openGiteeLinkBtn_Abt, appreciate_Abt, checkUpdate_Abt;

    /**
     * 读取配置文件
     *
     * @throws IOException 日志文件配置读取异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        // 获取日志储存数量配置
        setControlLastConfig(logsNum_Abt, prop, key_logsNum);
        // 获取自动检查更新配置
        setControlLastConfig(autoCheck_Abt, prop, key_autoCheck);
        input.close();
    }

    /**
     * 获取 logs 文件夹路径并展示
     */
    private void setLogsPath() {
        String logsPath = getLogsPath();
        setPathLabel(logsPath_Abt, logsPath);
    }

    /**
     * 保存日志问文件数量设置
     *
     * @throws IOException 配置文件保存异常
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
     * 清理多余 log 文件
     */
    private void deleteLogs() {
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
                            throw new RuntimeException(about_deleteFailed() + path);
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
        // 自动检查更新开关添加鼠标悬停提示
        addToolTip(autoCheck_Abt);
        // 版本号鼠标悬停提示
        addToolTip(tip_version(), version_Abt);
        // 日志文件数量输入框添加鼠标悬停提示
        addValueToolTip(logsNum_Abt, tip_logsNum());
        // 赞赏按钮添加鼠标悬停提示
        addToolTip(tip_appreciate(), appreciate_Abt);
        // 给logo和应用名称添加鼠标悬停提示
        addToolTip(tip_thanks(), logo_Abt, title_Abt);
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
     * 更新最后检查日期
     *
     * @param color 文字颜色
     */
    private void updateCheckDate(Color color) {
        checkDate_Abt.setText(update_lastCheck() + LocalDateTime.now().format(formatter));
        checkDate_Abt.setTextFill(color);
    }

    /**
     * 构建邮件右键菜单
     */
    private void buildMailMenu() {
        ContextMenu contextMenu = new ContextMenu();
        setCopyValueContextMenu(contextMenu, mail_Abt, about_copyEmail());
        buildMailMenu(contextMenu, mail_Abt);
        mail_Abt.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(mail_Abt, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 添加复制 Label 值右键菜单
     *
     * @param contextMenu 右键菜单
     * @param valueLabel  要处理的文本栏
     */
    private void buildMailMenu(ContextMenu contextMenu, Label valueLabel) {
        MenuItem mailMenuItem = new MenuItem(text_mailTo());
        mailMenuItem.setOnAction(_ -> {
            try {
                String email = valueLabel.getText();
                URI mailtoURI;
                String mailto = "mailto:";
                if (email.startsWith(mailto)) {
                    mailtoURI = URI.create(email);
                } else {
                    mailtoURI = URI.create(mailto + email);
                }
                Desktop.getDesktop().mail(mailtoURI);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        contextMenu.getItems().add(mailMenuItem);
    }

    /**
     * 显示更新提示框
     *
     * @param updateInfo 更新信息
     * @return 用户选择的按钮类型
     */
    private static Optional<ButtonType> showUpdateDialog(CheckUpdateBean updateInfo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(update_checkUpdate_Abt());
        alert.setHeaderText(update_findNewVersion() + updateInfo.getVersion() + "        " +
                update_releaseDate() + updateInfo.getBuildDate());
        // 创建包含更新信息的文本区域
        TextArea textArea = new TextArea(updateInfo.getWhatsNew());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        alert.getDialogPane().setContent(expContent);
        ButtonType updateButton = new ButtonType(update_updateButton());
        ButtonType laterButton = new ButtonType(update_laterButton(), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(updateButton, laterButton);
        // 设置窗口图标
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        setWindowLogo(stage, logoPath);
        return alert.showAndWait();
    }

    /**
     * 界面初始化
     *
     * @throws IOException 日志文件配置读取异常
     */
    @FXML
    private void initialize() throws IOException {
        // 设置应用名称
        title_Abt.setText(appName);
        // 设置版本号
        version_Abt.setText(version);
        // 添加右键菜单
        buildMailMenu();
        // 设置鼠标悬停提示
        setToolTip();
        // 设置要防重复点击的组件
        setDisableNodes();
        // log 文件保留数量输入监听
        integerRangeTextField(logsNum_Abt, 0, null, tip_logsNum());
        // 读取配置文件
        getConfig();
        // 获取 logs 文件夹路径并展示
        setLogsPath();
        // 清理多余 log 文件
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
     * 打开 GitHub 链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openGitHubLink() throws Exception {
        Desktop.getDesktop().browse(new URI(githubLink));
    }

    /**
     * 打开 Gitee 链接
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
        double with = Double.parseDouble(prop.getProperty(key_appreciateWidth, "500"));
        double height = Double.parseDouble(prop.getProperty(key_appreciateHeight, "500"));
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
        checkDate_Abt.setText("");
        TaskBean<?> taskBean = new TaskBean<>();
        taskBean.setMassageLabel(checkMassage_Abt)
                .setDisableNodes(disableNodes)
                .setBindingMassageLabel(true);
        Task<CheckUpdateBean> task = checkLatestVersion();
        bindingTaskNode(task, taskBean);
        task.setOnSucceeded(_ -> {
            taskUnbind(taskBean);
            CheckUpdateBean updateInfo = task.getValue();
            // 检查是否有新版本
            if (isNewVersionAvailable(updateInfo)) {
                checkMassage_Abt.setText(update_findNewVersion() + updateInfo.getVersion());
                //更新最后检查日期
                updateCheckDate(Color.BLUE);
                checkMassage_Abt.setTextFill(Color.BLUE);
                if (!runPMCFile || autoClickController == null || autoClickController.isFree()) {
                    // 弹出更新对话框
                    Optional<ButtonType> result = showUpdateDialog(updateInfo);
                    if (result.isPresent() && result.get().getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE) {
                        ProgressDialog progressDialog = new ProgressDialog();
                        // 用户选择更新
                        downloadedUpdateTask = downloadAndInstallUpdate(updateInfo, progressDialog);
                        Thread.ofVirtual()
                                .name("task-downloadedUpdate-vThread" + tabId)
                                .start(downloadedUpdateTask);
                        downloadedUpdateTask.setOnFailed(_ -> {
                            try {
                                logger.info("任务失败，删除临时文件夹： {}", PMCTempPath);
                                deleteDirectoryRecursively(Path.of(PMCTempPath));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            String message = update_downloadFailed();
                            taskNotSuccess(taskBean, message);
                            progressDialog.close();
                            Throwable ex = downloadedUpdateTask.getException();
                            downloadedUpdateTask = null;
                            throw new RuntimeException(message, ex);
                        });
                        downloadedUpdateTask.setOnCancelled(_ -> downloadedUpdateTask = null);
                    }
                }
            } else {
                checkMassage_Abt.setText(update_nowIsLast());
                //更新最后检查日期
                updateCheckDate(Color.GREEN);
                checkMassage_Abt.setTextFill(Color.GREEN);
            }
        });
        task.setOnFailed(_ -> {
            taskNotSuccess(taskBean, update_checkFailed());
            //更新最后检查日期
            updateCheckDate(Color.RED);
            throw new RuntimeException(task.getException());
        });
        Thread.ofVirtual()
                .name("task-checkUpdate-vThread" + tabId)
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
