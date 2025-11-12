package priv.koishi.pmc.Utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FileConfig;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.ProtectionDomain;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;

/**
 * 文件操作工具类
 *
 * @author KOISHI
 * Date 2024-09-30
 * Time 下午3:16
 */
public class FileUtils {

    /**
     * 字符串格式保留两位小数
     */
    private static final DecimalFormat df = new DecimalFormat("0.00");

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return 文件类型
     */
    public static String getExistsFileType(File file) {
        if (!file.exists()) {
            throw new RuntimeException(text_fileNotExists());
        }
        if (file.isDirectory()) {
            return extension_folder();
        }
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 ||
                lastDotIndex == 0 ||
                lastDotIndex == fileName.length() - 1) {
            return extension_file();
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 获取文件类型（文件不存在时可用）
     *
     * @param path 文件路径
     * @return 文件类型
     */
    public static String getFileType(String path) {
        if (StringUtils.isBlank(path)) {
            throw new RuntimeException(text_pathNull());
        }
        if (FilenameUtils.getPrefixLength(path) == -1) {
            throw new RuntimeException(text_errPathFormat());
        }
        File file = new File(path);
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 ||
                lastDotIndex == 0 ||
                lastDotIndex == fileName.length() - 1) {
            return extension_fileOrFolder();
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 校验文件是否为图片
     *
     * @param file 要校验的文件
     * @return true-图片，false-非图片
     */
    public static boolean isImgFile(File file) {
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
     * @throws IOException 配置文件保存异常
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
     */
    public static void openFile(String openPath) {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new RuntimeException(text_fileNotExists());
            }
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 打开文件夹并选中文件
     *
     * @param openPath 要打开的路径
     */
    public static void openDirectory(String openPath) {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new RuntimeException(text_fileNotExists());
            }
            try {
                if (file.isDirectory()) {
                    Desktop.getDesktop().open(file);
                }
                if (file.isFile()) {
                    openParentDirectory(openPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 打开上级目录并选中目标文件
     *
     * @param openPath 目标文件的路径
     */
    public static void openParentDirectory(String openPath) {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new RuntimeException(text_fileNotExists());
            }
            ProcessBuilder processBuilder;
            if (isWin) {
                processBuilder = new ProcessBuilder("cmd.exe", "/C", "explorer /select, " + openPath);
            } else {
                processBuilder = new ProcessBuilder("bash", "-c", "open -R " + "'" + openPath + "'");
            }
            try {
                processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 验证文件名是否符合 Windows 和 UNIX 文件系统的命名规则
     *
     * @param fileName 待验证的文件名
     * @return 如果文件名有效，返回 true 否则返回 false
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
     * 判断程序是否在 idea 中运行
     *
     * @return 在 idea 环境运为 true，其他环境为 false
     */
    public static boolean isRunningFromIDEA() {
        return "file".equals(getRunningFrom());
    }

    /**
     * 获取程序运行环境
     *
     * @return 运行环境
     */
    public static String getRunningFrom() {
        ProtectionDomain protectionDomain = FileUtils.class.getProtectionDomain();
        URL codeSourceLocation = protectionDomain.getCodeSource().getLocation();
        return codeSourceLocation.getProtocol();
    }

    /**
     * 根据不同运行环境来创建输入流
     *
     * @param path 输入流路径
     * @return 根据不同运行环境创建的输入流
     * @throws IOException 文件读取异常
     */
    public static InputStream checkRunningInputStream(String path) throws IOException {
        InputStream input;
        if (isRunningFromIDEA) {
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
     * @throws IOException 文件保存异常
     */
    public static OutputStream checkRunningOutputStream(String path) throws IOException {
        OutputStream output;
        if (isRunningFromIDEA) {
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
        return javaHome + packagePath + path;
    }

    /**
     * 获取 logs 文件夹地址
     *
     * @return 不同操作系统下 logs 文件夹地址
     */
    public static String getLogsPath() {
        if (isRunningFromIDEA) {
            return userDir + File.separator + logs;
        }
        String logsPath = rootDir + File.separator + logs;
        // 处理macos打包成.app文件后的路径
        if (isMac) {
            logsPath = javaHome + logsDir;
        }
        return logsPath;
    }

    /**
     * 获取程序启动路径
     *
     * @return 不同操作系统下程序启动路径(win - exe 文件路径 ， mac - app 文件路径)
     */
    public static String getAppLaunchPath() {
        if (isWin) {
            return rootDir + File.separator + appName + exe;
        } else if (isMac) {
            return javaHome.substring(0, javaHome.indexOf(app) + app.length());
        }
        return javaHome;
    }

    /**
     * 获取文件不带拓展名的名称或文件夹的名称
     *
     * @param file 要获取文件名的文件
     * @return 文件夹或不带拓展名的文件名称
     */
    public static String getExistsFileName(File file) {
        if (!file.exists()) {
            throw new RuntimeException(text_fileNotExists());
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
     */
    public static String getFileName(String path) {
        if (StringUtils.isBlank(path)) {
            throw new RuntimeException(text_pathNull());
        }
        if (FilenameUtils.getPrefixLength(path) != -1) {
            if (path.lastIndexOf(".") != -1) {
                return path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
            } else {
                return path.substring(path.lastIndexOf(File.separator) + 1);
            }
        }
        throw new RuntimeException(text_errPathFormat());
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
     */
    public static String notOverwritePath(String path) {
        if (StringUtils.isBlank(path)) {
            throw new RuntimeException(text_pathNull());
        }
        File file = new File(path);
        if (!file.exists()) {
            return path;
        }
        String parentDir = file.getParent();
        String fileName = getExistsFileName(file);
        String extension = getExistsFileType(file);
        if (extension_file().equals(extension) || extension_folder().equals(extension)) {
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

    /**
     * 根据操作系统计算文件大小
     *
     * @param file 要计算的文件
     * @return 带单位的文件大小
     */
    public static String getFileUnitSize(File file) {
        return getUnitSize(file.length());
    }

    /**
     * 根据操作系统将数值转换为文件大小
     *
     * @param size 没带单位的文件大小
     * @return 带单位的文件大小
     */
    public static String getUnitSize(long size) {
        return getUnitSize(size, true);
    }

    /**
     * 根据操作系统将数值转换为文件大小
     *
     * @param size          没带单位的文件大小
     * @param distinguishOS 区分操作系统文件大小单位进制，true 区分，false 不区分，macos 文件大小为 1000 进制，内存大小为 1024 进制
     * @return 带单位的文件大小
     */
    public static String getUnitSize(long size, boolean distinguishOS) {
        long winUnit = 1024;
        long macUnit = 1000;
        long kb;
        // macOS与Windows文件大小进制不同
        if (isMac && distinguishOS) {
            kb = macUnit;
        } else {
            kb = winUnit;
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
     * 根据 jvm 参数 key 读取 cfg 文件对应设置值
     *
     * @param optionKeys 要查询的 jvm 参数 key
     * @return jvm 参数 key 与对应的参数右侧的值
     * @throws IOException cfg 配置文件读取异常
     */
    public static Map<String, String> getJavaOptionValue(List<String> optionKeys) throws IOException {
        Map<String, String> jvmOptions = new HashMap<>();
        List<String> jvmOptionsList = new ArrayList<>(optionKeys);
        try (BufferedReader reader = Files.newBufferedReader(Path.of(cfgFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 检测配置段
                if (line.startsWith(javaOptions)) {
                    String arg = line.substring(javaOptions.length());
                    Iterator<String> iterator = jvmOptionsList.iterator();
                    while (iterator.hasNext()) {
                        String optionKey = iterator.next();
                        if (arg.contains(optionKey)) {
                            String value = arg.substring(arg.indexOf(optionKey) + optionKey.length());
                            jvmOptions.put(optionKey, value);
                            iterator.remove();
                        }
                    }
                }
            }
        }
        jvmOptionsList.forEach(optionKey -> jvmOptions.put(optionKey, ""));
        return jvmOptions;
    }

    /**
     * 更新 cfg 文件中 jvm 参数设置
     *
     * @param options 要修改的 jvm 参数键值对
     * @throws IOException cfg 配置文件读取或写入异常
     */
    public static void setJavaOptionValue(Map<String, String> options) throws IOException {
        Path configPath = Path.of(cfgFilePath);
        List<String> lines = Files.readAllLines(configPath);
        boolean modified = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(javaOptions)) {
                Iterator<Map.Entry<String, String>> iterator = options.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    String optionKey = entry.getKey();
                    String optionValue = entry.getValue();
                    if (line.contains(optionKey)) {
                        if (StringUtils.isNotBlank(optionValue)) {
                            // 处理修改参数
                            String newLineContent = javaOptions + optionKey + optionValue;
                            lines.set(i, line.replace(line, newLineContent));
                        } else {
                            // 处理删除参数
                            lines.remove(i);
                        }
                        iterator.remove();
                        modified = true;
                    }
                }
            }
        }
        // 处理新增参数
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String optionValue = entry.getValue();
            if (StringUtils.isNotBlank(optionValue)) {
                lines.add(javaOptions + entry.getKey() + optionValue);
                modified = true;
            }
        }
        if (modified) {
            Files.write(configPath, lines);
        }
    }

    /**
     * 获取 cfg 文件路径
     *
     * @return cfg 文件路径
     */
    public static String getCFGPath() {
        String cfgPath;
        if (isRunningFromIDEA) {
            cfgPath = appName + cfg;
        } else {
            String cfgFileName = "/" + appName + cfg;
            if (isWin) {
                cfgPath = appRootPath + appDirectory + cfgFileName;
            } else {
                cfgPath = appLaunchPath + contentsDirectory + appDirectory + cfgFileName;
            }
        }
        return cfgPath;
    }

    /**
     * 解压 zip 文件
     *
     * @param zipFilePath   zip 文件路径
     * @param destDirectory 输出目录
     */
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        // 如果目标目录存在，则先删除
        if (destDir.exists()) {
            deleteDirectory(destDir);
        }
        // 创建目标目录
        try {
            Files.createDirectories(destDir.toPath());
        } catch (IOException e) {
            throw new RuntimeException(update_creatDirErr() + destDirectory, e);
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                // 规范化路径并确保目录条目正确识别
                if (entryName.contains("\\")) {
                    // 统一使用 Unix 风格分隔符
                    entryName = entryName.replace('\\', '/');
                }
                // 使用 File 构造路径，自动处理空格和分隔符
                File file = new File(destDir, entryName);
                // 检查是否是目录条目
                if (entry.isDirectory() || entryName.endsWith("/")) {
                    // 确保目录路径以分隔符结尾
                    String dirPath = file.getPath();
                    if (!dirPath.endsWith(File.separator)) {
                        file = new File(dirPath + File.separator);
                    }
                    // 处理目录条目
                    try {
                        Files.createDirectories(file.toPath());
                    } catch (IOException e) {
                        throw new IOException(update_creatDirErr() + file.getAbsolutePath(), e);
                    }
                } else {
                    // 确保父目录存在
                    File parent = file.getParentFile();
                    if (parent != null) {
                        try {
                            Files.createDirectories(parent.toPath());
                        } catch (IOException e) {
                            throw new IOException(update_creatFatherDirErr() + parent.getAbsolutePath(), e);
                        }
                    }
                    // 处理文件条目
                    extractFile(zipIn, file);
                }
                zipIn.closeEntry();
            }
        }
    }

    /**
     * 递归删除目录
     *
     * @param dir 要删除的目录
     */
    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!dir.delete()) {
            throw new RuntimeException(update_deleteErr() + dir.getAbsolutePath());
        }
    }

    /**
     * 处理 zip 内的文件条目
     *
     * @param zipIn zip 输入流
     * @param file  zip 内的文件
     * @throws IOException 读取 zip 文件时异常
     */
    private static void extractFile(ZipInputStream zipIn, File file) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    /**
     * 递归删除目录
     *
     * @param path 要删除的目录
     * @throws IOException 删除目录异常
     */
    public static void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * 读取文件夹下的文件名称
     *
     * @param fileConfig 文件查询设置
     * @return 查询到的文件列表
     */
    public static List<File> readAllFiles(FileConfig fileConfig) {
        List<File> fileList = new ArrayList<>();
        readFiles(fileConfig, fileList, new File(fileConfig.getPath()));
        return fileList;
    }

    /**
     * 递归读取文件夹下的文件名称
     *
     * @param fileConfig 文件查询设置
     * @param fileList   上层文件夹查询的文件列表
     * @param directory  最外层文件夹
     */
    public static void readFiles(FileConfig fileConfig, List<? super File> fileList, File directory) {
        File[] files = directory.listFiles();
        String showHideFile = fileConfig.getShowHideFile();
        String showDirectory = fileConfig.getShowDirectory();
        boolean recursion = fileConfig.isRecursion();
        List<String> filterExtensionList = fileConfig.getFilterExtensionList();
        if (files != null) {
            for (File file : files) {
                // 检测是否为符号链接（替身）
                boolean isSymbolicLink = Files.isSymbolicLink(file.toPath());
                if (file.isFile() || isSymbolicLink) {
                    if ((hide_noHideFile().equals(showHideFile) && file.isHidden()) ||
                            (hide_onlyHideFile().equals(showHideFile) && !file.isHidden()) ||
                            fileConfig.isNoDS_Store() && DS_Store.equals(file.getName())) {
                        continue;
                    }
                    if (StringUtils.isEmpty(showDirectory) ||
                            search_onlyFile().equals(showDirectory) ||
                            search_fileDirectory().equals(showDirectory)) {
                        String extension = getExistsFileType(file);
                        boolean matches = CollectionUtils.isEmpty(filterExtensionList) ||
                                filterExtensionList.contains(extension);
                        // 反向匹配文件类型
                        if (fileConfig.isReverseFileType()) {
                            matches = CollectionUtils.isEmpty(filterExtensionList) ||
                                    !filterExtensionList.contains(extension);
                        }
                        if (matches) {
                            filterFileName(fileConfig, fileList, file);
                        }
                    }
                    if (recursion && !isSymbolicLink) {
                        readFiles(fileConfig, fileList, file);
                    }
                } else if (file.isDirectory()) {
                    if ((hide_noHideFile().equals(showHideFile) && file.isHidden()) ||
                            (hide_onlyHideFile().equals(showHideFile) && !file.isHidden())) {
                        continue;
                    }
                    if (search_onlyDirectory().equals(showDirectory) ||
                            search_fileDirectory().equals(showDirectory)) {
                        filterFileName(fileConfig, fileList, file);
                    }
                    if (recursion) {
                        readFiles(fileConfig, fileList, file);
                    }
                }
            }
        }
    }

    /**
     * 筛选文件名称
     *
     * @param fileConfig 文件查询设置
     * @param fileList   上层文件夹查询的文件列表
     * @param file       文件
     */
    private static void filterFileName(FileConfig fileConfig, List<? super File> fileList, File file) {
        String fileNameFilter = fileConfig.getFileNameFilter();
        if (StringUtils.isNotBlank(fileNameFilter) && file.exists()) {
            String fileName = getExistsFileName(file);
            // 区分大小写设置
            if (!fileConfig.isFilterNameCase()) {
                fileName = fileName.toLowerCase();
                fileNameFilter = fileNameFilter.toLowerCase();
            }
            // 匹配文件名称
            boolean matches = isMatchesFileName(fileConfig, fileName, fileNameFilter);
            if (matches) {
                fileList.add(file);
            }
        } else {
            fileList.add(file);
        }
    }

    /**
     * 筛选文件名称
     *
     * @param fileConfig     文件查询设置
     * @param fileName       文件名称
     * @param fileNameFilter 文件名称过滤
     * @return 是否匹配
     */
    private static boolean isMatchesFileName(FileConfig fileConfig, String fileName, String fileNameFilter) {
        boolean matches;
        String fileNameType = fileConfig.getFileNameType();
        if (fileNameType.equals(name_contain())) {
            matches = fileName.contains(fileNameFilter);
        } else if (fileNameType.equals(name_is())) {
            matches = fileNameFilter.equals(fileName);
        } else if (fileNameType.equals(name_start())) {
            matches = fileName.startsWith(fileNameFilter);
        } else if (fileNameType.equals(name_end())) {
            matches = fileName.endsWith(fileNameFilter);
        } else {
            matches = false;
        }
        // 反向查询名称设置
        if (fileConfig.isReverseFileName()) {
            matches = !matches;
        }
        return matches;
    }

    /**
     * 筛选出顶级目录
     *
     * @param directories 要筛选的目录
     * @throws IOException 获取文件属性异常
     */
    public static List<File> filterTopDirectories(List<? extends File> directories) throws IOException {
        List<File> topDirs = new ArrayList<>();
        for (File dir : directories) {
            if (!isPath(dir.getPath())) {
                continue;
            }
            if (dir.isFile()) {
                continue;
            }
            boolean isTop = true;
            for (File other : directories) {
                if (isSubdirectory(other, dir)) {
                    isTop = false;
                    break;
                }
            }
            if (isTop && !topDirs.contains(dir)) {
                topDirs.add(dir);
            }
        }
        return topDirs;
    }

    /**
     * 判断文件是否是子目录
     *
     * @param parent 父目录
     * @param child  子目录
     * @throws IOException 获取文件属性异常
     */
    public static boolean isSubdirectory(File parent, File child) throws IOException {
        // 判断 child 是否是 parent 的子目录
        return !parent.getCanonicalPath().equals(child.getCanonicalPath()) &&
                child.getCanonicalPath().startsWith(parent.getCanonicalPath() + File.separator);
    }

    /**
     * 判断路径是否合法
     *
     * @param path 路径
     * @return true-合法，false-非法
     */
    public static boolean isPath(String path) {
        return FilenameUtils.getPrefixLength(path) != -1;
    }

    /**
     * 获取文件修改时间
     *
     * @param file 要读取的文件
     * @return 格式化后的时间字符串
     */
    public static String getFileUpdateTime(File file) {
        Instant instant = Instant.ofEpochMilli(file.lastModified());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    /**
     * 获取文件创建时间
     *
     * @param file 要读取的文件
     * @return 格式化后的时间字符串
     * @throws IOException 文件创建时间读取异常
     */
    public static String getFileCreatTime(File file) throws IOException {
        Path path = Paths.get(file.getPath());
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        Instant instant = attr.creationTime().toInstant();
        ZonedDateTime creationTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return creationTime.format(formatter);
    }

    /**
     * 将带单位的文件大小字符串转换为课比较类型
     *
     * @param value 带单位的文件大小
     * @return 不带单位的文件大小
     */
    public static double fileSizeCompareValue(String value) {
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        String unit = value.substring(value.indexOf(" ") + 1);
        String size = value.substring(0, value.indexOf(" "));
        double compareValue;
        double win = 1024;
        double mac = 1000;
        double kb;
        // macOS与Windows文件大小进制不同
        if (isMac) {
            kb = mac;
        } else {
            kb = win;
        }
        double mb = kb * kb;
        double gb = mb * kb;
        double tb = gb * kb;
        switch (unit) {
            case Byte: {
                compareValue = Double.parseDouble(size);
                break;
            }
            case KB: {
                compareValue = Double.parseDouble(size) * kb;
                break;
            }
            case MB: {
                compareValue = Double.parseDouble(size) * mb;
                break;
            }
            case GB: {
                compareValue = Double.parseDouble(size) * gb;
                break;
            }
            case TB: {
                compareValue = Double.parseDouble(size) * tb;
                break;
            }
            default: {
                compareValue = Double.parseDouble(size) * tb * kb;
            }
        }
        return compareValue;
    }

}
