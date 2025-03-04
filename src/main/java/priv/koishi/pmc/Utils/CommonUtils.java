package priv.koishi.pmc.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * @author KOISHI
 * Date:2024-10-10
 * Time:下午1:14
 */
public class CommonUtils {

    /**
     * 资源文件夹地址前缀
     */
    static String resourcesPath = "src/main/resources/priv/koishi/pmc/";

    /**
     * 正则表达式用于匹配指定范围的整数
     *
     * @param str 要校验的字符串
     * @param min 最小值，为空则不限制
     * @param max 最大值，为空则不限制
     * @return 在设置范围内为true，不在范围内为false
     */
    public static boolean isInIntegerRange(String str, Integer min, Integer max) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        // 禁止出现0开头的非0数字
        if (str.indexOf("0") == 0 && str.length() > 1) {
            return false;
        }
        Pattern integerPattern = Pattern.compile("^-?\\d{1,10}$");
        // 使用正则表达式判断字符串是否为整数
        if (!integerPattern.matcher(str).matches()) {
            return false;
        }
        // 将字符串转换为整数并判断是否在指定范围内
        int value = Integer.parseInt(str);
        // 只判断是否为整数，不限定范围
        if (max == null && min == null) {
            return true;
        }
        // 限定最小值
        if (max == null) {
            return value >= min;
        }
        // 限定最大值
        if (min == null) {
            return value <= max;
        }
        return value >= min && value <= max;
    }

    /**
     * 获取详细的异常信息
     *
     * @param e 要获取的异常
     * @return 详细异常详细
     */
    public static String errToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * 判断程序是否打包运行
     *
     * @return 在jar环境运为true，其他环境为false
     */
    public static boolean isRunningFromJar() {
        // 获取当前运行的JVM的类加载器
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        // 获取URL资源
        URL resource = classLoader.getResource("");
        // 检查URL的协议是否是jar或者file协议，file协议表示不是从JAR中加载
        String protocol = null;
        if (resource != null) {
            protocol = resource.getProtocol();
        }
        return "jar".equals(protocol);
    }

    /**
     * 根据不同运行环境来创建输入流
     *
     * @param path 输入流路径
     * @return 根据不同运行环境创建的输入流
     * @throws IOException io异常
     */
    public static InputStream checkRunningInputStream(String path) throws IOException {
        InputStream input;
        if (isRunningFromJar()) {
            input = new FileInputStream(resourcesPath + path);
        } else {
            input = new FileInputStream(path);
        }
        return input;
    }

    /**
     * 根据不同运行环境来创建输出流
     *
     * @param path 输出流路径
     * @return 根据不同运行环境创建的输出流
     * @throws IOException io异常
     */
    public static OutputStream checkRunningOutputStream(String path) throws IOException {
        OutputStream output;
        if (isRunningFromJar()) {
            output = new FileOutputStream(resourcesPath + path);
        } else {
            output = new FileOutputStream(path);
        }
        return output;
    }

}
