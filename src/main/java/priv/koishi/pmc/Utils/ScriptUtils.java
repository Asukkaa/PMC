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
        List<String> command = buildCommand(path, getFileType(path), parameter, minScriptWindow);
        pb.command(command);
        Process process = pb.start();
        process.waitFor();
    }

    /**
     * 构建执行命令 - 使用系统默认终端
     *
     * @param scriptPath      要执行的脚本路径
     * @param fileType        脚本文件类型
     * @param parameter       运行脚本的参数
     * @param minScriptWindow 是否最小化窗口 （true 最小化窗口执行）
     */
    private static List<String> buildCommand(String scriptPath, String fileType,
                                             String parameter, boolean minScriptWindow) {
        List<String> command = new ArrayList<>();
        if (bat.equals(fileType) || cmd.equals(fileType)) {
            command.add("cmd");
            command.add("/c");
            command.add("start");
            if (minScriptWindow) {
                command.add("/min");
            }
            command.add("/wait");
        } else if (sh.equals(fileType) || bash.equals(fileType)) {
            command.add("/bin/bash");
        } else if (py.equals(fileType)) {
            if (isWin) {
                command.add("cmd");
                command.add("/c");
                command.add("start");
                if (minScriptWindow) {
                    command.add("/min");
                }
                command.add("/wait");
            }
            command.add("python3");
        }
        command.add(scriptPath);
        // 添加参数
        if (StringUtils.isNotBlank(parameter)) {
            // 对于Windows批处理，参数需要放在start命令之后
            if (bat.equals(fileType) || cmd.equals(fileType)) {
                // 将参数添加到脚本路径后面
                int scriptIndex = command.indexOf(scriptPath);
                if (scriptIndex >= 0) {
                    // 解析参数并添加到命令中
                    List<String> params = parseParameters(parameter);
                    command.addAll(scriptIndex + 1, params);
                }
            } else if (py.equals(fileType)) {
                // 其他系统直接添加参数
                List<String> params = parseParameters(parameter);
                command.addAll(params);
            }
        }
        return command;
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
