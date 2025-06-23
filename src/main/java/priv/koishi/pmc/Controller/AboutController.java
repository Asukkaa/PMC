package priv.koishi.pmc.Controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import priv.koishi.pmc.Bean.CheckUpdateBean;
import priv.koishi.pmc.Bean.UniCloudResponse;
import priv.koishi.pmc.ProgressDialog.ProgressDialog;

import javax.net.ssl.SSLContext;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 关于页面控制器
 *
 * @author KOISHI
 * Date:2025-01-07
 * Time:16:45
 */
public class AboutController extends RootController {

    @FXML
    public ImageView logo_Abt;

    @FXML
    public TextField logsNum_Abt;

    @FXML
    public Label logsPath_Abt, mail_Abt, version_Abt, title_Abt;

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
        // 给github、gitee跳转按钮添加鼠标悬停提示
        addToolTip(tip_openGitLink(), openGitHubLinkBtn_Abt, openGiteeLinkBtn_Abt);
        // 给网盘跳转按钮添加鼠标悬停提示
        addToolTip(tip_openLink(), openBaiduLinkBtn_Abt, openQuarkLinkBtn_Abt, openXunleiLinkBtn_Abt);
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
        // log 文件保留数量输入监听
        integerRangeTextField(logsNum_Abt, 0, null, tip_logsNum());
        // 读取配置文件
        getConfig();
        // 获取logs文件夹路径并展示
        setLogsPath();
        // 清理多余log文件
        deleteLogs();
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
        checkLatestVersion(updateInfo -> {
            if (!updateInfo.getVersion().contains("检查失败")) {
                Optional<ButtonType> result = showUpdateDialog(updateInfo);
                if (result.isPresent() && result.get().getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE) {
                    // 用户选择更新
                    downloadAndInstallUpdate(updateInfo);
                }
            }
        });
    }

    /**
     * 调用 uniCloud 查询最新版本
     *
     * @param callback 回调函数
     */
    public static void checkLatestVersion(Consumer<? super CheckUpdateBean> callback) {
        final String uniCloudCheckUpdateURL = "https://fc-mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.next.bspapp.com/pmcUpdateTest";
        String osType;
        if (isWin) {
            osType = win;
        } else if (isMac) {
            osType = mac;
        } else {
            osType = mac;
        }
        new Thread(() -> {
            try (HttpClient client = HttpClient.newHttpClient()) {
                // 构建请求体
                String requestBody = "{\"os\":\"" + osType + "\"}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uniCloudCheckUpdateURL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.body() == null) {
                    throw new IOException("响应体为空");
                }
                String jsonData = response.body();
                ObjectMapper objectMapper = new ObjectMapper();
                // 配置忽略未知字段
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                // 先解析为 UniCloudResponse
                UniCloudResponse uniResponse = objectMapper.readValue(jsonData, UniCloudResponse.class);
                if (uniResponse.getCode() == 200) {
                    // 成功时提取 data 字段
                    Platform.runLater(() -> callback.accept(uniResponse.getData()));
                } else {
                    // 错误处理
                    Platform.runLater(() -> {
                        CheckUpdateBean errorBean = new CheckUpdateBean();
                        errorBean.setVersion("检查失败: " + uniResponse.getMessage());
                        callback.accept(errorBean);
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    CheckUpdateBean errorBean = new CheckUpdateBean();
                    errorBean.setVersion("检查失败: " + e.getMessage());
                    callback.accept(errorBean);
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void downloadAndInstallUpdate(CheckUpdateBean updateInfo) {
        // 显示下载进度对话框
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.show(bundle.getString("update.downloading"));
        new Thread(() -> {
            try {
                // 获取应用根目录并创建temp文件夹
                File tempDir = new File(getDownloadPath(), "PMCTemp");
                if (!tempDir.exists()) {
                    if (!tempDir.mkdirs()) {
                        throw new RuntimeException("创建temp文件夹失败");
                    }
                }
                // 根据操作系统确定文件扩展名
                String extension = isWin ? ".zip" : ".dmg";
                // 在temp目录创建临时文件
                File tempFile = File.createTempFile("pmc_update_", extension, tempDir);
                // 创建使用TLSv1.2的SSLContext
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);
                // 创建HttpClient，并设置SSLContext
                try (HttpClient client = HttpClient.newBuilder()
                        .sslContext(sslContext)
                        .build()) {
                    String encodedLink = updateInfo.getDownloadLink().replace(" ", "%20");
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(encodedLink))
                            .GET()
                            .build();
                    // 发送请求并处理响应
                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new IOException("下载失败: HTTP " + response.statusCode());
                    }
                    try (InputStream inputStream = response.body();
                         FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                        long contentLength = response.headers()
                                .firstValueAsLong("Content-Length")
                                .orElse(0);
                        byte[] buffer = new byte[8192];
                        long downloadedBytes = 0;
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            downloadedBytes += bytesRead;
                            if (contentLength > 0) {
                                double progress = (double) downloadedBytes / contentLength;
                                Platform.runLater(() -> progressDialog.updateProgress(progress));
                            }
                        }
                    }
                }
                Platform.runLater(() -> {
                    progressDialog.close();
                    executeInstaller(tempFile);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void executeInstaller(File installerFile) {
        // 根据操作系统执行安装程序
        try {
            if (isWin) {
                String destPath = new File(installerFile.getParentFile(), "PMCUpdateUnzipped").getAbsolutePath();
                unzip(installerFile.getAbsolutePath(), destPath);
                updateWinApp();
            } else if (isMac) {
                String mountedPath = getMountedPath(installerFile);
                updateMacApp(installerFile, mountedPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getMountedPath(File installerFile) throws IOException, InterruptedException {
        // 挂载dmg文件
        Process mountProcess = new ProcessBuilder("hdiutil", "attach", installerFile.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
        // 读取挂载输出
        String mountedPath = null;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(mountProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("/Volumes/")) {
                    mountedPath = line.trim().split("\\s+")[0]; // 获取第一个挂载点
                }
            }
        }
        if (mountProcess.waitFor() != 0 || mountedPath == null) {
            throw new RuntimeException("挂载DMG失败");
        }
        return mountedPath;
    }

    private static void updateMacApp(File installerFile, String mountedPath) throws InterruptedException, IOException {
        File mountedVolume = new File(mountedPath);
        File appFile = new File(mountedVolume, appName + app);
        // 创建更新脚本
        File updateScript = new File(mountedVolume, "update.sh");
        try (PrintWriter writer = new PrintWriter(updateScript)) {
            writer.println("#!/bin/bash");
            writer.println("echo '正在更新应用...'");
            writer.println("sudo rm -rf \"/Applications/" + appName + app + "\"");
            writer.println("sudo cp -Rf \"" + appFile.getAbsolutePath() + "\" \"/Applications/\"");
            writer.println("echo '卸载镜像...'");
            writer.println("hdiutil detach \"" + mountedPath + "\"");
            writer.println("echo '清理临时文件...'");
            writer.println("rm -f \"" + installerFile.getAbsolutePath() + "\"");
            writer.println("rm -f \"$0\"");
            writer.println("echo '启动新版本应用...'");
            writer.println("open -a \"/Applications/" + appName + app + "\"");
        }
        // 设置执行权限并运行
        // 设置执行权限（使用 ProcessBuilder 替代已弃用的 Runtime.exec()）
        new ProcessBuilder("chmod", "+x", updateScript.getAbsolutePath())
                .start()
                .waitFor();
        // 使用 ProcessBuilder 执行更新脚本
        new ProcessBuilder("osascript", "-e",
                "do shell script \"\\\"" + updateScript.getAbsolutePath() + "\\\"\" with administrator privileges")
                .start();
        Platform.exit();
    }

    private static void updateWinApp() throws IOException {
        // 获取源目录和目标目录
        String sourceDir = getDownloadPath() + File.separator + "PMCTemp" + File.separator + "PMCUpdateUnzipped";
        String targetDir = getAppRootPath();
        // 创建临时批处理文件
        File batFile = File.createTempFile("pmc_update", ".bat");
        try (PrintWriter writer = new PrintWriter(batFile)) {
            // 从资源文件读取批处理脚本内容
            try (InputStream is = AboutController.class.getResourceAsStream("/priv/koishi/pmc/script/update.bat")) {
                if (is != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.println(line);
                        }
                    }
                } else {
                    throw new IOException("找不到批处理脚本");
                }
            }
        }
        // 构建命令参数
        List<String> command = new ArrayList<>();
        command.add("cmd.exe");
        command.add("/c");
        command.add(batFile.getAbsolutePath());
        command.add(sourceDir);
        command.add(targetDir);
        command.add(appName + exe);
        command.add(getDownloadPath() + File.separator + "PMCTemp");
        // 执行批处理
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(targetDir));
        builder.start();
    }

}
