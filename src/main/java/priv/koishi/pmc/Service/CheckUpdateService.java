package priv.koishi.pmc.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.CheckUpdateBean;
import priv.koishi.pmc.Bean.UniCloudResponse;
import priv.koishi.pmc.Controller.AboutController;
import priv.koishi.pmc.ProgressDialog.ProgressDialog;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.FileUtils.getDownloadPath;

/**
 * 检查更新服务类
 *
 * @author KOISHI
 * Date:2025-06-24
 * Time:14:50
 */
public class CheckUpdateService {

    /**
     * 检查是有新版本
     *
     * @param checkUpdateBean 检查更新信息
     * @return true:有新版本 false:无新版本
     */
    public static boolean isNewVersionAvailable(CheckUpdateBean checkUpdateBean) {
        // 无效数据视为无更新
        if (checkUpdateBean == null ||
                StringUtils.isBlank(checkUpdateBean.getVersion()) ||
                StringUtils.isBlank(checkUpdateBean.getBuildDate())) {
            return false;
        }
        String lastVersion = checkUpdateBean.getVersion();
        // 检测到的最新版本
        String[] lastVersionSplit = lastVersion.split("\\.");
        // 当前版本
        String[] nowVersionSplit = version.split("\\.");
        //  比较版本号的各个位数
        int length = Math.max(lastVersionSplit.length, nowVersionSplit.length);
        for (int i = 0; i < length; i++) {
            // 检测到的最新版本
            int last = i < lastVersionSplit.length ? Integer.parseInt(lastVersionSplit[i]) : 0;
            // 当前版本
            int now = i < nowVersionSplit.length ? Integer.parseInt(nowVersionSplit[i]) : 0;
            if (last != now) {
                return last > now;
            }
        }
        // 比较构建日期
        return checkUpdateBean.getBuildDate().compareTo(buildDate) > 0;
    }

    /**
     * 调用 uniCloud 查询最新版本
     *
     * @param callback 回调函数
     */
    public static void checkLatestVersion(Consumer<? super CheckUpdateBean> callback) {
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
                    throw new IOException(bundle.getString("update.nullResponse"));
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
                        errorBean.setVersion(bundle.getString("update.checkError") + uniResponse.getMessage());
                        callback.accept(errorBean);
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    CheckUpdateBean errorBean = new CheckUpdateBean();
                    errorBean.setVersion(bundle.getString("update.checkError") + e.getMessage());
                    callback.accept(errorBean);
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * 下载并安装更新
     *
     * @param updateInfo 更新信息
     */
    public static void downloadAndInstallUpdate(CheckUpdateBean updateInfo) {
        // 显示下载进度对话框
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.show(bundle.getString("update.downloading"));
        new Thread(() -> {
            try {
                // 获取应用根目录并创建temp文件夹
                File tempDir = new File(getDownloadPath(), PMCTemp);
                if (!tempDir.exists()) {
                    if (!tempDir.mkdirs()) {
                        throw new RuntimeException(bundle.getString("update.tempFileErr"));
                    }
                }
                if (isWin) {
                    Files.setAttribute(tempDir.toPath(), "dos:hidden", true);
                }
                // 根据操作系统确定文件扩展名
                String extension = isWin ? zip : dmg;
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
                        throw new IOException(bundle.getString("update.downloadError") + response.statusCode());
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

    /**
     * 安装程序
     *
     * @param installerFile 安装程序文件
     */
    private static void executeInstaller(File installerFile) {
        // 根据操作系统执行安装程序
        try {
            if (isWin) {
                String destPath = new File(installerFile.getParentFile(), PMCUpdateUnzipped).getAbsolutePath();
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

    /**
     * 获取已挂载的dmg文件
     *
     * @param installerFile dmg文件
     * @return 已挂载的目录
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
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
                    // 获取第一个挂载点
                    mountedPath = line.trim().split("\\s+")[0];
                }
            }
        }
        if (mountProcess.waitFor() != 0 || mountedPath == null) {
            throw new RuntimeException(bundle.getString("update.dmgErr"));
        }
        return mountedPath;
    }

    /**
     * 更新Mac端应用
     *
     * @param installerFile 安装包文件
     * @param mountedPath   挂载点路径
     */
    private static void updateMacApp(File installerFile, String mountedPath) throws InterruptedException, IOException {
        File mountedVolume = new File(mountedPath);
        File appFile = new File(mountedVolume, appName + app);
        // 创建更新脚本
        File updateScript = new File(mountedVolume, "update.sh");
        try (PrintWriter writer = new PrintWriter(updateScript)) {
            writer.println("#!/bin/bash");
            writer.println("sudo rm -rf \"/Applications/" + appName + app + "\"");
            writer.println("sudo cp -Rf \"" + appFile.getAbsolutePath() + "\" \"/Applications/\"");
            writer.println("hdiutil detach \"" + mountedPath + "\"");
            writer.println("rm -f \"" + installerFile.getAbsolutePath() + "\"");
            writer.println("rm -f \"$0\"");
            writer.println("open -a \"/Applications/" + appName + app + "\"");
        }
        // 设置执行权限并运行
        new ProcessBuilder("chmod", "+x", updateScript.getAbsolutePath())
                .start()
                .waitFor();
        // 使用 ProcessBuilder 执行更新脚本
        new ProcessBuilder("osascript", "-e",
                "do shell script \"\\\"" + updateScript.getAbsolutePath() + "\\\"\" with administrator privileges")
                .start();
        Platform.exit();
    }

    /**
     * 更新win端应用
     *
     * @throws IOException 找不到批处理脚本
     */
    private static void updateWinApp() throws IOException {
        // 获取源目录和目标目录
        String sourceDir = getDownloadPath() + PMCTemp + PMCUpdateUnzipped;
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
                    throw new IOException(bundle.getString("update.batNotFind"));
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
        command.add(getDownloadPath() + PMCTemp);
        // 执行批处理
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(targetDir));
        builder.start();
    }

}
