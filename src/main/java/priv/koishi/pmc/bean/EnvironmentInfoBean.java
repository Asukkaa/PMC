package priv.koishi.pmc.bean;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static priv.koishi.pmc.utils.ScriptUtils.minJavaVersion;

/**
 * 环境变量数据类
 *
 * @author Koishi
 * Date:2026-07-13
 * Time:14:02
 */
@Data
@Accessors(chain = true)
public class EnvironmentInfoBean {

    /**
     * 操作系统 Java 版本
     */
    private String javaVersion;

    /**
     * Java 执行路径
     */
    private String javaPath;

    /**
     * 操作系统 Python 版本
     */
    private String pythonVersion;

    /**
     * 操作系统 PowerShell 版本
     */
    private String powershellVersion;

    /**
     * 判断 Java 版本是否符合最低要求版本
     *
     * @return true 符合最低要求
     */
    public boolean isJavaVersionValid() {
        if (StringUtils.isBlank(javaVersion)) {
            return false;
        }
        // 支持 "version \"26.0.2.1\"" 或 "version \"1.8.0_201\"" 等多种格式
        Pattern pattern = Pattern.compile("version \"(\\d+)(?:\\.\\d+)+.*?\"");
        Matcher matcher = pattern.matcher(javaVersion);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            return major >= minJavaVersion;
        }
        return false;
    }

    /**
     * 判断 Python 版本是否符合最低要求版本
     *
     * @return true 符合最低要求
     */
    public boolean isPythonVersionValid() {
        if (StringUtils.isBlank(pythonVersion)) {
            return false;
        }
        int minMajor = 3;
        String version = pythonVersion.trim();
        // 优先尝试匹配 "Python" 后面的第一个数字（支持任意后续格式）
        Pattern pattern = Pattern.compile("Python\\s+(\\d+)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            return major >= minMajor;
        }
        // 若没有 "Python" 前缀，尝试匹配字符串开头的数字（常见于纯版本号）
        pattern = Pattern.compile("^(\\d+)");
        matcher = pattern.matcher(version);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            return major >= minMajor;
        }
        return false;
    }

}
