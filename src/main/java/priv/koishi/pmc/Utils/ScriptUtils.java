package priv.koishi.pmc.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.text_scriptNotExecutable;
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
        if (isMac) {
            runWithMacTerminal(minScriptWindow, command, fileType, path, parameter, pb);
        } else if (isWin) {
            runWithWinTerminal(minScriptWindow, command, fileType, path, parameter, pb);
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
        if (minScriptWindow) {
            command.add("/min");
        }
        command.add("/wait");
        if (py.equals(fileType)) {
            command.add("python3");
        }
        command.add(scriptPath);
        // 添加参数
        if (StringUtils.isNotBlank(parameter)) {
            // 将参数添加到脚本路径后面
            int scriptIndex = command.indexOf(scriptPath);
            if (scriptIndex >= 0) {
                // 解析参数并添加到命令中
                List<String> params = parseParameters(parameter);
                command.addAll(scriptIndex + 1, params);
            }
        }
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
        if (py.equals(fileType)) {
            appleScript.append("python3 ");
        }
        appleScript.append(scriptPath);
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

    /**
     * 解析参数字符串，支持带引号的参数
     *
     * @param parameter 参数字符串
     */
    private static List<String> parseParameters(String parameter) {
        List<String> params = new ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '"';
        for (int i = 0; i < parameter.length(); i++) {
            char c = parameter.charAt(i);
            if ((c == '"' || c == '\'') && !inQuotes) {
                // 开始引号
                inQuotes = true;
                quoteChar = c;
            } else if (c == quoteChar) {
                // 结束引号
                inQuotes = false;
                if (!currentParam.isEmpty()) {
                    params.add(currentParam.toString());
                    currentParam.setLength(0);
                }
            } else if (Character.isWhitespace(c) && !inQuotes) {
                // 空格且不在引号内，结束当前参数
                if (!currentParam.isEmpty()) {
                    params.add(currentParam.toString());
                    currentParam.setLength(0);
                }
            } else {
                // 普通字符
                currentParam.append(c);
            }
        }
        // 处理最后一个参数
        if (!currentParam.isEmpty()) {
            params.add(currentParam.toString());
        }
        return params;
    }

}
