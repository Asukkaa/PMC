package priv.koishi.pmc.Utils;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.CommonUtils.checkRunningOutputStream;

/**
 * 文件操作工具类
 *
 * @author KOISHI
 * Date 2024-09-30
 * Time 下午3:16
 */
public class FileUtils {

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return 文件类型
     */
    public static String getFileType(File file) {
        if (file.isDirectory()) {
            return "文件夹";
        }
        String filePath = file.getPath();
        if (filePath.lastIndexOf(".") == -1) {
            return "文件";
        }
        return filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 根据操作系统将数值转换为文件大小
     *
     * @param size          没带单位的文件大小
     * @param distinguishOS 区分操作系统文件大小单位进制，true区分，false不区分，macos文件大小为1000进制，内存大小为1024进制
     * @return 带单位的文件大小
     */
    public static String getUnitSize(long size, boolean distinguishOS) {
        long win = 1024;
        long mac = 1000;
        long kb;
        // macOS与Windows文件大小进制不同
        if (systemName.contains(macos) && distinguishOS) {
            kb = mac;
        } else {
            kb = win;
        }
        return getRet(kb, size);
    }

    /**
     * 格式化文件大小数据
     *
     * @param kb   文件大小单位进制
     * @param size 不带单位的文件大小
     * @return 带单位的文件大小
     */
    private static String getRet(long kb, long size) {
        long mb = kb * kb;
        long gb = mb * kb;
        long tb = gb * kb;
        String ret = "";
        DecimalFormat df = new DecimalFormat("0.00");
        if (size >= tb) {
            ret = df.format(size / (tb * 1.0)) + " " + TB;
        } else if (size >= gb) {
            ret = df.format(size / (gb * 1.0)) + " " + GB;
        } else if (size >= mb) {
            ret = df.format(size / (mb * 1.0)) + " " + MB;
        } else if (size >= kb) {
            ret = df.format(size / (kb * 1.0)) + " " + KB;
        } else if (size >= 0) {
            ret = df.format(size) + " " + Byte;
        }
        return ret;
    }

    /**
     * 更新配置文件
     *
     * @param properties 要更新的配置文件
     * @param key        要更新的配置名
     * @param value      要更新的值
     * @throws IOException io异常
     */
    public static void updateProperties(String properties, String key, String value) throws IOException {
        InputStream input = checkRunningInputStream(properties);
        Properties prop = new Properties();
        prop.load(input);
        prop.put(key, value);
        OutputStream output = checkRunningOutputStream(properties);
        prop.store(output, null);
        input.close();
        output.close();
    }

    /**
     * 打开文件
     *
     * @param openPath 要打开的路径
     * @throws IOException 文件不存在
     */
    public static void openFile(String openPath) throws IOException {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new IOException(text_fileNotExists);
            }
            Desktop.getDesktop().open(file);
        }
    }

    /**
     * 打开文件夹并选中文件
     *
     * @param openPath 要打开的路径
     * @throws IOException 文件不存在
     */
    public static void openDirectory(String openPath) throws IOException {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new IOException(text_fileNotExists);
            }
            if (file.isDirectory()) {
                Desktop.getDesktop().open(file);
            }
            if (file.isFile()) {
                ProcessBuilder processBuilder;
                if (systemName.contains(win)) {
                    processBuilder = new ProcessBuilder("cmd.exe", "/C", "explorer /select, " + openPath);
                } else {
                    processBuilder = new ProcessBuilder("bash", "-c", "open -R " + "'" + openPath + "'");
                }
                processBuilder.start();
            }
        }
    }

    /**
     * 验证文件名是否符合Windows和UNIX文件系统的命名规则。
     *
     * @param fileName 待验证的文件名
     * @return 如果文件名有效，返回true；否则返回false。
     */
    public static boolean isValidFileName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return false;
        }
        // 文件名中不能包含以下字符
        String illegalChars = "<>:\"/\\|?*";
        if (fileName.contains("//") || fileName.contains("\\\\")) {
            return false;
        }
        // 检查文件名是否包含非法字符
        for (int i = 0; i < fileName.length(); i++) {
            if (illegalChars.indexOf(fileName.charAt(i)) >= 0) {
                return false;
            }
        }
        // 检查是否以空格或特殊字符开头
        char firstChar = fileName.charAt(0);
        return !Character.isSpaceChar(firstChar) && illegalChars.indexOf(firstChar) < 0;
    }

    /**
     * 判断字符串是否为文件路径
     *
     * @param path 要校验的文件路径
     * @return 如果是文件路径返回true，否则返回false
     */
    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
