package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static priv.koishi.pmc.Utils.ScriptUtils.minJavaVersion;

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
     */
    public boolean isJavaVersionValid() {
        if (StringUtils.isBlank(javaVersion)) {
            return false;
        }
        // 匹配常见格式： "openjdk version \"11.0.12\"  ..." 或 "java version \"17.0.2\""
        Pattern pattern = Pattern.compile("version \"(\\d+)\\.\\d+\\.\\d+\"");
        Matcher matcher = pattern.matcher(javaVersion);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            return major >= minJavaVersion;
        }
        return false;
    }

    /**
     * 判断 Python 版本是否符合最低要求版本
     */
    public boolean isPythonVersionValid() {
        if (StringUtils.isBlank(pythonVersion)) {
            return false;
        }
        int minPyVersion = 3;
        // 匹配常见格式： "Python 3.12.5" 或 "Python 3.11.0"
        Pattern pattern = Pattern.compile("Python (\\d+)\\.\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(pythonVersion);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            return major >= minPyVersion;
        }
        // 兼容只有 "3.12.5" 等情况
        pattern = Pattern.compile("^(\\d+)\\.\\d+\\.\\d+");
        matcher = pattern.matcher(pythonVersion);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            return major >= minPyVersion;
        }
        return false;
    }

}
