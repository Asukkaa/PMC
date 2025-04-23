package priv.koishi.pmc.Utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;

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
     * @throws IOException 文件不存在
     */
    public static String getExistsFileType(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException(text_fileNotExists);
        }
        if (file.isDirectory()) {
            return extension_folder;
        }
        String filePath = file.getPath();
        if (filePath.lastIndexOf(".") == -1) {
            return extension_file;
        }
        return filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 获取文件类型（文件不存在时可用）
     *
     * @param path 文件路径
     * @return 文件类型
     * @throws IOException 路径不能为空、路径格式不正确
     */
    public static String getFileType(String path) throws IOException {
        if (StringUtils.isBlank(path)) {
            throw new IOException(text_nullPath);
        }
        if (FilenameUtils.getPrefixLength(path) == -1) {
            throw new IOException(text_errPathFormat);
        }
        if (path.lastIndexOf(".") == -1) {
            return extension_fileOrFolder;
        }
        return path.substring(path.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 校验文件是否为图片
     *
     * @param file 要校验的文件
     * @return true为图片，false为非图片
     */
    public static boolean isImgFile(File file) throws IOException {
        if (!file.exists()) {
            return false;
        }
        return imageType.contains(getExistsFileType(file));
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
            input = new FileInputStream(getAppResourcePath(path));
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
            output = new FileOutputStream(getAppResourcePath(path));
        }
        return output;
    }

    /**
     * 获取app资源文件绝对路径
     *
     * @param path 资源文件相对路径
     * @return 资源文件绝对路径
     */
    public static String getAppResourcePath(String path) {
        String resourcePath = packagePath + path;
        // 处理macos打包成.app文件后的路径
        if (systemName.contains(mac)) {
            resourcePath = javaHome + "/bin/" + path;
        }
        if (!new File(resourcePath).exists()) {
            resourcePath = path;
        }
        return resourcePath;
    }

    /**
     * 获取logs文件夹地址
     *
     * @return 不同操作系统下logs文件夹地址
     */
    public static String getLogsPath() {
        String logsPath = userDir + File.separator + logs;
        // 处理macos打包成.app文件后的路径
        if (systemName.contains(mac) && !isRunningFromJar()) {
            logsPath = javaHome + logsDir;
        }
        return logsPath;
    }

    /**
     * 获取程序启动路径
     *
     * @return 不同操作系统下程序启动路径
     */
    public static String getAppPath() {
        if (systemName.contains(win)) {
            return new File(javaHome).getParent() + File.separator + appName + exe;
        } else if (systemName.contains(mac)) {
            return javaHome.substring(0, javaHome.indexOf(app) + app.length());
        }
        return javaHome;
    }

    /**
     * 获取文件不带拓展名的名称或文件夹的名称
     *
     * @param file 要获取文件名的文件
     * @return 文件夹或不带拓展名的文件名称
     * @throws IOException 文件不存在
     */
    public static String getExistsFileName(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException(text_fileNotExists);
        }
        String fileName = file.getName();
        if (!file.isDirectory() && fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 获取文件不带拓展名的名称或文件夹的名称 (文件可不存在)
     *
     * @param path 要获取文件名的文件路径
     * @return 文件夹或不带拓展名的文件名称
     * @throws IOException 路径不能为空、路径格式不正确
     */
    public static String getFileName(String path) throws IOException {
        if (StringUtils.isBlank(path)) {
            throw new IOException(text_nullPath);
        }
        if (FilenameUtils.getPrefixLength(path) != -1) {
            if (path.lastIndexOf(".") != -1) {
                return path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
            } else {
                return path.substring(path.lastIndexOf(File.separator) + 1);
            }
        }
        throw new IOException(text_errPathFormat);
    }

    /**
     * 递归寻找存在的文件或上级文件
     *
     * @param path 要寻找的文件路径
     * @return 存在的文件或上级文件
     */
    public static File getExistsFile(String path) {
        File defaultFile = new File(defaultFileChooserPath);
        // 验证输入有效性
        if (StringUtils.isBlank(path)) {
            return defaultFile;
        }
        File currentFile = new File(path);
        // 直接验证文件存在性
        if (currentFile.exists()) {
            return currentFile;
        }
        // 获取父目录并验证递归终止条件
        File parentFile = currentFile.getParentFile();
        if (parentFile == null || parentFile.getPath().equals(currentFile.getPath())) {
            return defaultFile;
        }
        return getExistsFile(parentFile.getPath());
    }

    /**
     * 文件重名不覆盖
     *
     * @param path 要判断的文件路径
     * @return 不会重名文件路径
     * @throws IOException 路径不能为空
     */
    public static String notOverwritePath(String path) throws IOException {
        if (StringUtils.isBlank(path)) {
            throw new IOException(text_nullPath);
        }
        File file = new File(path);
        if (!file.exists()) {
            return path;
        }
        String parentDir = file.getParent();
        String fileName = getExistsFileName(file);
        String extension = getExistsFileType(file);
        if (extension_file.equals(extension) || extension_folder.equals(extension)) {
            extension = "";
        }
        // 递归添加尾缀
        int counter = 1;
        while (file.exists()) {
            path = parentDir + File.separator + fileName + "-" + counter + extension;
            file = new File(path);
            counter++;
        }
        return path;
    }

    /**
     * 获取用户桌面路径
     *
     * @return 用户桌面路径
     */
    public static String getDesktopPath() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File desktopFile = fsv.getHomeDirectory();
        String path = desktopFile.getAbsolutePath();
        if (!path.contains(desktop)) {
            String desktopPath = path + File.separator + desktop;
            if (new File(desktopPath).exists()) {
                return desktopPath;
            }
        }
        return path;
    }

}
