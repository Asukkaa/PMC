package priv.koishi.pmc.Utils;

import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.Bean.EnvironmentInfoBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.text_scriptNotExecutable;
import static priv.koishi.pmc.Service.AutoClickService.getEnvInfo;
import static priv.koishi.pmc.Utils.FileUtils.getExistsFileName;
import static priv.koishi.pmc.Utils.FileUtils.getFileType;

/**
 * 运行脚本工具类
 *
 * @author KOISHI
 * Date:2025-10-27
 * Time:21:24
 */
public class ScriptUtils {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(ScriptUtils.class);

    /**
     * 操作系统环境信息
     */
    public static final EnvironmentInfoBean envInfo = new EnvironmentInfoBean();

    /**
     * 最低 Java 版本
     */
    public static final int minJavaVersion = 11;

    /**
     * 获取系统环境任务线程
     */
    public static Task<Void> envInfoTask;

    /**
     * 启动获取系统环境任务线程
     *
     * @param tabId 启动页面 ID
     */
    public static void startEnvInfoTask(String tabId, Runnable runnable) {
        if (envInfoTask != null) {
            envInfoTask.cancel();
            envInfoTask = null;
        }
        envInfoTask = getEnvInfo();
        envInfoTask.setOnSucceeded(_ -> {
            if (runnable != null) {
                runnable.run();
            }
            envInfoTask = null;
        });
        envInfoTask.setOnFailed(_ -> envInfoTask = null);
        if (!envInfoTask.isRunning()) {
            Thread.ofVirtual()
                    .name("startEnvInfoTask-vThread" + tabId)
                    .start(envInfoTask);
        }
    }

    /**
     * 检测系统 Java 环境，决定使用的 Java 可执行文件，并填充 Map
     */
    public static void detectJavaEnvironment() {
        String systemJava = "java";
        String detectedVersion = null;
        boolean systemAvailable = false;
        // 检测系统 java -version
        try {
            ProcessBuilder pb = new ProcessBuilder(systemJava, "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    Pattern pattern = Pattern.compile("version \"(\\d+)\\.\\d+\\.\\d+\"");
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            int majorVersion = Integer.parseInt(matcher.group(1));
                            detectedVersion = line.trim();
                            if (majorVersion >= minJavaVersion) {
                                systemAvailable = true;
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            logger.warn("系统 Java 不可用");
        }
        if (systemAvailable) {
            // 使用系统 Java
            envInfo.setJavaVersion(detectedVersion)
                    .setJavaPath(systemJava);
        } else {
            // 系统 Java 不可用或版本不足，尝试自带 Java
            String javaHome = System.getProperty("java.home");
            String selfJava = javaHome + File.separator + "bin" + File.separator + "java";
            if (isWin) {
                selfJava += ".exe";
            }
            File selfJavaFile = new File(selfJava);
            // 使用自带 Java 路径
            if (selfJavaFile.exists() && selfJavaFile.canExecute()) {
                envInfo.setJavaPath(selfJava);
            }
        }
    }

    /**
     * 检测系统 Python 版本
     */
    public static void detectPythonEnvironment() {
        String pythonVersion = getCommandVersion("python3");
        if (StringUtils.isBlank(pythonVersion)) {
            pythonVersion = getCommandVersion("python");
        }
        envInfo.setPythonVersion(pythonVersion);
    }

    /**
     * 检测系统 PowerShell 版本（仅版本，不获取路径）
     * 优先检测 pwsh (PowerShell Core)，再检测 powershell (Windows PowerShell)
     */
    public static void detectPowerShellEnvironment() {
        String version;
        // 尝试 pwsh --version (PowerShell Core)
        version = getCommandVersion("pwsh");
        if (version == null) {
            // 尝试 powershell -Command "$PSVersionTable.PSVersion.ToString()"
            version = getPowerShellVersionFromCommand();
        }
        envInfo.setPowershellVersion(version);
    }

    /**
     * 通过执行命令获取 PowerShell 版本
     *
     * @return PowerShell 版本
     */
    private static String getPowerShellVersionFromCommand() {
        try {
            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", "$PSVersionTable.PSVersion.ToString()");
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null) {
                        return line.trim();
                    }
                }
            }
        } catch (Exception ignored) {
            logger.warn("PowerShell 版本获取失败");
        }
        return null;
    }

    /**
     * 执行命令并返回其第一行输出（支持 stdout 或 stderr）
     *
     * @param command 需要执行的版本查询命令
     * @return 查询到的版本
     */
    private static String getCommandVersion(String command) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(command);
            Collections.addAll(cmd, "--version");
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null) {
                        return line.trim();
                    }
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line = reader.readLine();
                    if (line != null) {
                        return line.trim();
                    }
                }
            }
        } catch (Exception ignored) {
            logger.warn("环境版本获取失败");
        }
        return null;
    }

    /**
     * 运行脚本
     *
     * <p>
     * 测试文件所在目录： ScriptTest
     *
     * @param script          要运行的脚本文件
     * @param workDir         运行脚本的目录
     * @param parameter       运行脚本的参数
     * @param minScriptWindow 是否最小化窗口 （true 最小化窗口执行）
     * @throws Exception 运行脚本时发生错误
     */
    public static void runScript(File script, String workDir, String parameter,
                                 boolean minScriptWindow) throws Exception {
        String path = script.getAbsolutePath();
        // 验证脚本文件是否可执行
        if (isMac && !script.canExecute()) {
            // 尝试设置可执行权限
            if (!script.setExecutable(true)) {
                throw new RuntimeException(text_scriptNotExecutable() + path);
            }
        }
        ProcessBuilder pb = new ProcessBuilder();
        if (StringUtils.isNotBlank(workDir)) {
            File workDirFile = new File(workDir);
            if (workDirFile.isDirectory()) {
                pb.directory(workDirFile);
            }
        }
        String fileType = getFileType(path);
        // 运行 Java 相关文件前检测是否有相关环境，如果没有则使用应用自身环境
        if (java.equals(fileType) || jar.equals(fileType) || clazz.equals(fileType)) {
            detectJavaEnvironment();
        }
        List<String> command = new ArrayList<>();
        if (isWin) {
            runWithWinTerminal(minScriptWindow, command, fileType, path, parameter, pb);
        } else {
            runWithMacTerminal(minScriptWindow, command, fileType, path, parameter, pb);
        }
        try (Process process = pb.start()) {
            process.waitFor();
        }
    }

    /**
     * 拼接 Windows 终端启动脚本命令
     *
     * @param minScriptWindow 是否最小化窗口 （true 最小化窗口执行）
     * @param command         命令列表
     * @param fileType        脚本文件类型
     * @param scriptPath      脚本文件路径
     * @param pb              进程构建器
     */
    private static void runWithWinTerminal(boolean minScriptWindow, List<String> command, String fileType,
                                           String scriptPath, String parameter, ProcessBuilder pb) {
        command.add("cmd");
        command.add("/c");
        command.add("start");
        // 设置窗口标题
        String title = new File(scriptPath).getName();
        command.add("\"" + title + "\"");
        if (minScriptWindow) {
            command.add("/min");
        }
        // 构建实际的执行命令
        String javaPath = envInfo.getJavaPath();
        String javaCmd = (StringUtils.isNotBlank(javaPath)) ? "\"" + javaPath + "\"" : "java";
        StringBuilder executeCommand = new StringBuilder();
        boolean addScriptPath = true;
        switch (fileType) {
            case py -> executeCommand.append("python3 ");
            case ps1 -> executeCommand.append("powershell -ExecutionPolicy Bypass -File ");
            case java -> executeCommand.append(javaCmd).append(" ");
            case jar -> executeCommand.append(javaCmd).append(" -jar ");
            case clazz -> {
                File classFile = new File(scriptPath);
                String classDir = classFile.getParent();
                executeCommand.append(javaCmd).append(" -cp ");
                executeCommand.append("\"").append(classDir != null ? classDir : ".").append("\" ");
                executeCommand.append(getExistsFileName(classFile));
                addScriptPath = false;
            }
        }
        if (addScriptPath) {
            executeCommand.append("\"").append(scriptPath).append("\"");
        }
        // 添加参数
        if (StringUtils.isNotBlank(parameter)) {
            executeCommand.append(" ").append(parameter);
        }
        // 添加自动退出命令 - 关键修改
        executeCommand.append(" && exit");
        command.add("/wait");
        command.add("cmd");
        command.add("/c");
        command.add(executeCommand.toString());
        pb.command(command);
    }

    /**
     * 拼接 macOS 终端启动脚本命令
     *
     * @param minScriptWindow 是否最小化窗口 （true 最小化窗口执行）
     * @param command         命令列表
     * @param fileType        脚本文件类型
     * @param scriptPath      脚本文件路径
     * @param pb              进程构建器
     */
    private static void runWithMacTerminal(boolean minScriptWindow, List<String> command, String fileType,
                                           String scriptPath, String parameter, ProcessBuilder pb) {
        command.add("osascript");
        command.add("-e");
        String javaPath = envInfo.getJavaPath();
        String javaCmd = (StringUtils.isNotBlank(javaPath)) ? "\"" + javaPath + "\"" : "java";
        StringBuilder appleScript = new StringBuilder();
        appleScript.append("tell application \"Terminal\"\n");
        appleScript.append("  do script \"");
        File directory = pb.directory();
        if (directory != null) {
            appleScript.append("cd ")
                    .append(directory.getPath())
                    .append(" && ");
        }
        boolean addScriptPath = true;
        switch (fileType) {
            case py -> appleScript.append("python3 ");
            case ps1 -> appleScript.append("pwsh ");
            case java -> appleScript.append(javaCmd).append(" ");
            case jar -> appleScript.append(javaCmd).append(" -jar ");
            case clazz -> {
                appleScript.append(javaCmd).append(" -cp ");
                // 设置类路径为 class 文件所在目录
                File classFile = new File(scriptPath);
                String classDir = classFile.getParent();
                appleScript.append(classDir != null ? classDir : ".");
                appleScript.append(" ");
                appleScript.append(getExistsFileName(classFile));
                addScriptPath = false;
            }
        }
        if (addScriptPath) {
            appleScript.append(scriptPath);
        }
        if (StringUtils.isNotBlank(parameter)) {
            // 将参数添加到脚本路径后面
            appleScript.append(" ").append(parameter);
        }
        appleScript.append(" && exit\"\n");
        if (minScriptWindow) {
            appleScript.append("  set miniaturized of window 1 to true\n");
        } else {
            appleScript.append("  activate\n");
        }
        appleScript.append("  repeat until not busy of window 1\n");
        // 额外延迟确保脚本开始执行
        appleScript.append("    delay 0.5\n");
        appleScript.append("  end repeat\n");
        appleScript.append("  delay 0.5\n");
        appleScript.append("  close window 1\n");
        appleScript.append("end tell");
        command.add(appleScript.toString());
        pb.command(command);
    }

}
