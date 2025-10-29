package priv.koishi.pmc.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.text_scriptNotExecutable;
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
        List<String> command = new ArrayList<>();
        if (isWin) {
            runWithWinTerminal(minScriptWindow, command, fileType, path, parameter, pb);
        } else {
            runWithMacTerminal(minScriptWindow, command, fileType, path, parameter, pb);
        }
        Process process = pb.start();
        process.waitFor();
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
        StringBuilder executeCommand = new StringBuilder();
        boolean addScriptPath = true;
        switch (fileType) {
            case py -> executeCommand.append("python3 ");
            case ps1 -> executeCommand.append("powershell -ExecutionPolicy Bypass -File ");
            case java -> executeCommand.append("java ");
            case jar -> executeCommand.append("java -jar ");
            case clazz -> {
                File classFile = new File(scriptPath);
                String classDir = classFile.getParent();
                executeCommand.append("java -cp ");
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
            case java -> appleScript.append("java ");
            case jar -> appleScript.append("java -jar ");
            case clazz -> {
                appleScript.append("java ");
                appleScript.append("-cp ");
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
