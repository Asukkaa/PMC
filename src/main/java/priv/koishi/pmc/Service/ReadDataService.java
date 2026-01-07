package priv.koishi.pmc.Service;

import javafx.concurrent.Task;
import priv.koishi.pmc.Bean.Config.FileConfig;
import priv.koishi.pmc.Bean.TaskBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;

import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Utils.CommonUtils.NATURAL_SORT;
import static priv.koishi.pmc.Utils.FileUtils.getExistsFileType;
import static priv.koishi.pmc.Utils.FileUtils.readAllFiles;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;

/**
 * 读取文件服务类
 *
 * @author KOISHI
 * Date:2025-08-26
 * Time:18:31
 */
public class ReadDataService {

    /**
     * 读取所有文件任务
     *
     * @param fileConfig 文件读取设置
     * @return 文件列表
     */
    public static Task<List<File>> readAllFilesTask(TaskBean<?> taskBean, FileConfig fileConfig) {
        return new Task<>() {
            @Override
            protected List<File> call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                List<File> fileList = readAllFiles(fileConfig);
                comparingData(fileConfig, fileList);
                return fileList;
            }
        };
    }

    /**
     * 数据排序
     *
     * @param fileConfig 文件读取设置设置参数
     * @param fileList   要排序的文件
     */
    public static void comparingData(FileConfig fileConfig, List<File> fileList) {
        String sortType = fileConfig.getSortType();
        // 是否倒序排序
        boolean reverseSort = fileConfig.isReverseSort();
        if (sortType.equals(sort_Name())) {
            comparingByName(fileList, reverseSort);
        } else if (sortType.equals(sort_creatTime())) {
            comparingByCreatTime(fileList, reverseSort);
        } else if (sortType.equals(sort_updateTime())) {
            comparingByUpdateTime(fileList, reverseSort);
        } else if (sortType.equals(sort_size())) {
            comparingBySize(fileList, reverseSort);
        } else if (sortType.equals(sort_type())) {
            comparingByType(fileList, reverseSort);
        }
    }

    /**
     * 按文件类型排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true-倒序，false-正序
     */
    private static void comparingByType(List<File> fileList, boolean reverseSort) {
        Comparator<File> comparator = (o1, o2) -> {
            String ext1 = getExistsFileType(o1);
            String ext2 = getExistsFileType(o2);
            return ext1.compareTo(ext2);
        };
        List<File> sortedList = fileList.stream()
                .sorted(reverseSort ? comparator.reversed() : comparator)
                .toList();
        fileList.clear();
        fileList.addAll(sortedList);
    }

    /**
     * 按文件大小排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true-倒序，false-正序
     */
    private static void comparingBySize(List<File> fileList, boolean reverseSort) {
        Comparator<File> comparator = Comparator.comparing(File::length);
        List<File> sortedList = fileList.stream()
                .sorted(reverseSort ? comparator.reversed() : comparator)
                .toList();
        fileList.clear();
        fileList.addAll(sortedList);
    }

    /**
     * 按文件修改时间排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true-倒序，false-正序
     */
    private static void comparingByUpdateTime(List<File> fileList, boolean reverseSort) {
        Comparator<File> comparator = Comparator.comparing(File::lastModified);
        if (reverseSort) {
            comparator = comparator.reversed();
        }
        List<File> sortedList = fileList.stream()
                .sorted(comparator)
                .toList();
        fileList.clear();
        fileList.addAll(sortedList);
    }

    /**
     * 按文件创建时间排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true-倒序，false-正序
     */
    private static void comparingByCreatTime(List<File> fileList, boolean reverseSort) {
        Comparator<File> comparator = (o1, o2) -> {
            try {
                BasicFileAttributes attr1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
                BasicFileAttributes attr2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
                return Long.compare(attr1.creationTime().toMillis(), attr2.creationTime().toMillis());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        List<File> sortedList = fileList.stream()
                .sorted(reverseSort ? comparator.reversed() : comparator)
                .toList();
        fileList.clear();
        fileList.addAll(sortedList);
    }

    /**
     * 按文件名称排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true-倒序，false-正序
     */
    private static void comparingByName(List<File> fileList, boolean reverseSort) {
        Comparator<File> comparator = (f1, f2) -> {
            String name1 = f1.getName();
            String name2 = f2.getName();
            return NATURAL_SORT.compare(name1, name2);
        };
        List<File> sortedList = fileList.stream()
                .sorted(reverseSort ? comparator.reversed() : comparator)
                .toList();
        fileList.clear();
        fileList.addAll(sortedList);
    }

}
