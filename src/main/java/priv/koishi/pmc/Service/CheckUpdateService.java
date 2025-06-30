package priv.koishi.pmc.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.update_downloadFailed;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.Utils.FileUtils.*;

/**
 * 检查更新服务类
 *
 * @author KOISHI
 * Date:2025-06-24
 * Time:14:50
 */
public class CheckUpdateService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(CheckUpdateService.class);

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
     * @return 最新版本信息
     */
    public static Task<CheckUpdateBean> checkLatestVersion() {
        return new Task<>() {
            @Override
            protected CheckUpdateBean call() {
                updateMessage(bundle.getString("update.checking"));
                String osType;
                if (isWin) {
                    osType = win;
                } else if (isMac) {
                    osType = mac;
                } else {
                    osType = mac;
                }
                try (HttpClient client = HttpClient.newHttpClient()) {
                    // 构建请求体
                    String requestBody = "{\"os\":\"" + osType + "\"}";
                    logger.info("查询版本更新，请求体: {}", requestBody);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(uniCloudCheckUpdateURL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    logger.info("查询版本更新，响应体: {}", response);
                    String jsonData = response.body();
                    logger.info("查询版本更新，响应体body: {}", jsonData);
                    if (jsonData == null) {
                        throw new IOException(bundle.getString("update.nullResponse"));
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    // 配置忽略未知字段
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    // 先解析为 UniCloudResponse
                    UniCloudResponse uniResponse = objectMapper.readValue(jsonData, UniCloudResponse.class);
                    if (uniResponse.getCode() == 200) {
                        // 成功时提取 data 字段
                        return uniResponse.getData();
                    } else {
                        // 错误处理
                        CheckUpdateBean errorBean = new CheckUpdateBean();
                        errorBean.setVersion(bundle.getString("update.checkError") + uniResponse.getMessage());
                        return errorBean;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(bundle.getString("update.errCheck"), e);
                }
            }
        };
    }

    /**
     * 下载并安装更新
     *
     * @param updateInfo     更新信息
     * @param progressDialog 进度对话框
     */
    public static Task<Void> downloadAndInstallUpdate(CheckUpdateBean updateInfo, ProgressDialog progressDialog) {
        return new Task<>() {
            @Override
            protected Void call() {
                // 显示下载进度对话框
                progressDialog.show(bundle.getString("update.downloading"), bundle.getString("update.downloadingUpdate"));
                try {
                    // 创建temp文件夹
                    File tempDir = new File(PMCTempPath);
                    if (!tempDir.exists()) {
                        if (!tempDir.mkdirs()) {
                            throw new RuntimeException(bundle.getString("update.tempFileErr"));
                        }
                    }
                    if (isWin) {
                        Files.setAttribute(tempDir.toPath(), "dos:hidden", true);
                    }
                    // 在temp目录创建临时文件
                    File tempFile = File.createTempFile("pmc_update_", zip, tempDir);
                    // 创建使用TLSv1.2的SSLContext
                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                    sslContext.init(null, null, null);
                    // 创建HttpClient，并设置SSLContext
                    try (HttpClient client = HttpClient.newBuilder()
                            .sslContext(sslContext)
                            .build()) {
                        String encodedLink = updateInfo.getDownloadLink().replace(" ", "%20");
                        logger.info("下载更新，请求体: {}", encodedLink);
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(encodedLink))
                                .GET()
                                .build();
                        // 发送请求并处理响应
                        HttpResponse<InputStream> response;
                        try {
                            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                        } catch (Exception e) {
                            throw new IOException(update_downloadFailed());
                        }
                        if (response != null && (response.statusCode() < 200 || response.statusCode() >= 300)) {
                            throw new IOException(bundle.getString("update.downloadError") + response.statusCode());
                        }
                        if (response != null) {
                            try (InputStream inputStream = response.body();
                                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                                long contentLength = response.headers()
                                        .firstValueAsLong("Content-Length")
                                        .orElse(0);
                                byte[] buffer = new byte[8192];
                                long downloadedBytes = 0;
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    // 每次循环都检查取消状态
                                    if (isCancelled()) {
                                        break;
                                    }
                                    outputStream.write(buffer, 0, bytesRead);
                                    downloadedBytes += bytesRead;
                                    if (contentLength > 0) {
                                        double progress = (double) downloadedBytes / contentLength;
                                        Platform.runLater(() ->
                                                progressDialog.updateProgress(progress, bundle.getString("update.installing")));
                                    }
                                }
                                // 如果任务被取消，删除临时文件夹
                                if (isCancelled()) {
                                    logger.info("任务被取消，删除临时文件夹： {}", PMCTempPath);
                                    deleteDirectoryRecursively(Path.of(PMCTempPath));
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
                return null;
            }
        };
    }

    /**
     * 安装程序
     *
     * @param installerFile 安装程序文件
     */
    public static void executeInstaller(File installerFile) {
        logger.info("====================准备开始安装更新======================");
        try {
            //解压安装程序
            String destPath = new File(installerFile.getParentFile(), PMCUpdateUnzipped).getAbsolutePath();
            unzip(installerFile.getAbsolutePath(), destPath);
            // 根据操作系统执行安装程序
            if (isWin) {
                updateWinApp();
            } else if (isMac) {
                updateMacApp();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新Mac端应用
     */
    private static void updateMacApp() throws IOException, InterruptedException {
        // 获取源目录
        String sourceDir = PMCTempPath + PMCUpdateUnzipped + File.separator + appName + app;
        // 在系统临时目录创建脚本
        File tempDir = new File(tmpdir);
        File updateScriptFile = File.createTempFile("pmc_update_", ".sh", tempDir);
        try (PrintWriter writer = new PrintWriter(updateScriptFile)) {
            writer.println("#!/bin/bash");
            writer.println("export PATH=/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin");
            // 终止应用
            writer.println("/usr/bin/pkill -9 -f '" + appName + "' || true");
            // 删除旧应用
            writer.println("/usr/bin/sudo /bin/rm -rf \"/Applications/" + appName + ".app\"");
            // 复制新应用
            writer.println("/usr/bin/sudo /bin/cp -Rf \"" + sourceDir + "\" \"/Applications/\"");
            // 修复权限
            writer.println("/usr/bin/sudo /usr/sbin/chown -R " + sysUerName + ":staff \"/Applications/" + appName + ".app\"");
            writer.println("/usr/bin/sudo /usr/bin/xattr -d com.apple.quarantine \"/Applications/" + appName + ".app\"");
            writer.println("/usr/bin/sudo /bin/chmod -R 755 \"/Applications/" + appName + ".app\"");
            // 重新签名
            writer.println("/usr/bin/sudo /usr/bin/codesign --force --deep --sign - \"/Applications/" + appName + ".app\" || echo \"签名失败，继续执行\"");
            // 清理
            writer.println("/bin/rm -rf \"" + PMCTempPath + "\"");
            writer.println("/bin/rm -f \"$0\"");
            // 启动新应用
            writer.println("/usr/bin/open -a \"/Applications/" + appName + ".app\"");
            writer.println("exit 0");
        }
        // 设置权限
        if (!updateScriptFile.setExecutable(true)) {
            throw new IOException("无法设置脚本可执行权限");
        }
        // 验证权限
        if (!updateScriptFile.canExecute()) {
            throw new IOException("脚本不可执行" + updateScriptFile.getAbsolutePath());
        }
        // 构建执行命令
        String scriptCommand = String.format(
                "do shell script \"\\\"%s\\\"\" with administrator privileges",
                updateScriptFile.getAbsolutePath()
        );
        logger.info("-------------------------开始执行Mac更新脚本------------------------------");
        // 执行并捕获输出
        Process process = new ProcessBuilder("osascript", "-e", scriptCommand)
                .redirectErrorStream(true)
                .start();
        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("脚本输出: {}", line);
                output.append(line).append("\n");
            }
        }
        // 等待完成
        int exitCode = process.waitFor();
        logger.info("脚本退出码: {}", exitCode);
        if (exitCode != 0) {
            throw new IOException("脚本执行失败，退出码: " + exitCode + "\n输出: " + output);
        }
    }

    /**
     * 更新win端应用
     *
     * @throws IOException 找不到批处理脚本
     */
    private static void updateWinApp() throws IOException {
        // 获取源目录
        String sourceDir = PMCTempPath + PMCUpdateUnzipped;
        // 获取目标目录
        String targetDir = getAppRootPath();
        // 创建临时批处理文件
        File batFile = File.createTempFile(updateScript, bat);
        try (PrintWriter writer = new PrintWriter(batFile)) {
            // 从资源文件读取批处理脚本内容
            try (InputStream is = AboutController.class.getResourceAsStream(resourcePath + "script/update.bat")) {
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
        command.add(PMCTempPath);
        // 执行批处理
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(targetDir));
        logger.info("-------------------------开始执行Win更新脚本------------------------------");
        builder.start();
    }

}
