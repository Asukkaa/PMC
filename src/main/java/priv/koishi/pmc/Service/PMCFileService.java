package priv.koishi.pmc.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Bean.Config.FileConfig;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.Result.PMCLoadResult;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Utils.CommonUtils.copyAllProperties;
import static priv.koishi.pmc.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.UiUtils.showExceptionAlert;

/**
 * PMC 文件加载与导出任务类
 *
 * @author KOISHI
 * Date:2025-11-05
 * Time:14:40
 */
public class PMCFileService {

    /**
     * 批量加载 PMC 文件
     *
     * @param taskBean 线程任务参数
     * @param files    文件列表
     * @return PMC 文件列表
     */
    public static Task<PMCLoadResult> loadPMCFils(TaskBean<ClickPositionVO> taskBean, List<? extends File> files) {
        return new Task<>() {
            @Override
            protected PMCLoadResult call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                List<ClickPositionVO> clickPositionBeans = new ArrayList<>();
                Map<String, String> imgMap = new HashMap<>();
                String lastPMCPath = "";
                int size = files.size();
                updateProgress(0, size);
                for (int i = 0; i < size; i++) {
                    File file = files.get(i);
                    if (PMC.equals(getExistsFileType(file))) {
                        lastPMCPath = file.getPath();
                        try {
                            clickPositionBeans.addAll(loadPMCFile(file));
                        } catch (IOException e) {
                            showExceptionAlert(e);
                        }
                    } else if (file.isDirectory()) {
                        List<String> filterExtensionList = new ArrayList<>(imageType);
                        filterExtensionList.add(PMC);
                        FileConfig fileConfig = new FileConfig()
                                .setFilterExtensionList(filterExtensionList)
                                .setShowDirectory(search_fileDirectory())
                                .setShowHideFile(hide_noHideFile())
                                .setPath(file.getPath())
                                .setRecursion(true);
                        for (File readFile : readAllFiles(fileConfig)) {
                            if (PMC.equals(getExistsFileType(readFile))) {
                                lastPMCPath = readFile.getPath();
                                try {
                                    clickPositionBeans.addAll(loadPMCFile(readFile));
                                } catch (IOException e) {
                                    showExceptionAlert(e);
                                }
                            } else {
                                imgMap.put(readFile.getPath(), getExistsFileName(readFile));
                            }
                        }
                    } else if (imageType.contains(getExistsFileType(file))) {
                        imgMap.put(file.getPath(), getExistsFileName(file));
                    }
                    updateProgress(i + 1, size);
                }
                // 匹配图片
                matchSameNameImg(imgMap, clickPositionBeans);
                taskBean.getTableView().refresh();
                return new PMCLoadResult(clickPositionBeans, lastPMCPath);
            }

            /**
             * 匹配图片
             *
             * @param imgMap 文件夹中用于匹配的图片
             * @param clickPositionBeans 需要匹配的自动操作步骤
             */
            private void matchSameNameImg(Map<String, String> imgMap, List<? extends ClickPositionVO> clickPositionBeans) {
                if (!imgMap.isEmpty()) {
                    updateMessage(text_matchImg());
                    int clickPositionBeansSize = clickPositionBeans.size();
                    updateProgress(0, clickPositionBeansSize);
                    // 匹配要点击的图片
                    for (int j = 0; j < clickPositionBeansSize; j++) {
                        ClickPositionVO clickPositionVO = clickPositionBeans.get(j);
                        String clickImgPath = clickPositionVO.getClickImgPath();
                        if (StringUtils.isNotBlank(clickImgPath)) {
                            File file = new File(clickImgPath);
                            if (!file.exists()) {
                                // 通过文件获取路径可消除不同操作系统的路径分隔符的差异
                                String imgPath = getSameNameImgPath(imgMap, file);
                                if (StringUtils.isNotBlank(imgPath)) {
                                    clickPositionVO.setClickImgPath(imgPath);
                                }
                            }
                        }
                        // 匹配终止操作图片
                        List<ImgFileBean> stopImgFiles = clickPositionVO.getStopImgFiles();
                        stopImgFiles.forEach(stopImgFile -> {
                            String stopImgPath = stopImgFile.getPath();
                            if (StringUtils.isNotBlank(stopImgPath)) {
                                File stopFile = new File(stopImgPath);
                                if (!stopFile.exists()) {
                                    String imgPath = getSameNameImgPath(imgMap, stopFile);
                                    if (StringUtils.isNotBlank(imgPath)) {
                                        stopImgFile.setPath(imgPath);
                                    }
                                }
                            }
                        });
                        updateProgress(j + 1, clickPositionBeansSize);
                    }
                }
            }
        };
    }

    /**
     * 获取匹配到的同名图片地址
     *
     * @param imgMap 要进行匹配的图片 map
     * @param img    路径改变了的图片文件
     * @return 匹配到的图片地址
     */
    private static String getSameNameImgPath(Map<String, String> imgMap, File img) {
        String imgName = getFileName(img.getPath());
        return imgMap.entrySet()
                .stream()
                .filter(entry -> imgName.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 加载 PMC 文件
     *
     * @param taskBean 线程任务参数
     * @param file     要加载的文件
     * @return PMC 文件列表
     */
    public static Task<List<ClickPositionVO>> loadPMC(TaskBean<ClickPositionVO> taskBean, File file) {
        return new Task<>() {
            @Override
            protected List<ClickPositionVO> call() throws IOException {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                return loadPMCFile(file);
            }
        };
    }

    /**
     * 导出 PMC 文件
     *
     * @param taskBean    线程任务参数
     * @param fileName    要导出的文件名
     * @param outFilePath 导出文件夹路径
     * @return 导出文件路径
     */
    public static Task<String> exportPMC(TaskBean<ClickPositionVO> taskBean, String fileName, String outFilePath, boolean notOverwrite) {
        return new Task<>() {
            @Override
            protected String call() throws IOException {
                changeDisableNodes(taskBean, true);
                updateMessage(text_exportData());
                List<ClickPositionVO> tableViewItems = taskBean.getBeanList();
                String path = outFilePath + File.separator + fileName + PMC;
                if (notOverwrite) {
                    path = notOverwritePath(path);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                // 构建基类类型信息
                JavaType baseType = objectMapper.getTypeFactory().constructParametricType(List.class, ClickPositionBean.class);
                // 使用基类类型进行序列化
                objectMapper.writerFor(baseType).writeValue(new File(path), tableViewItems);
                updateMessage(text_saveSuccess() + path);
                return path;
            }
        };
    }

    /**
     * 批量加载图片文件
     *
     * @param taskBean 线程任务参数
     * @param files    图片文件列表
     */
    public static Task<Void> loadImg(TaskBean<ImgFileVO> taskBean, List<? extends File> files) {
        return new Task<>() {
            @Override
            protected Void call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                TableView<ImgFileVO> tableView = taskBean.getTableView();
                ObservableList<ImgFileVO> items = tableView.getItems();
                int size = files.size();
                updateProgress(0, size);
                for (int i = 0; i < size; i++) {
                    File file = files.get(i);
                    boolean isExist = items.stream().anyMatch(bean -> file.getPath().equals(bean.getPath()));
                    if (!isExist) {
                        ImgFileVO imgFileVO = new ImgFileVO();
                        imgFileVO.setTableView(tableView)
                                .setType(getExistsFileType(file))
                                .setName(file.getName())
                                .setPath(file.getPath());
                        items.add(imgFileVO);
                    }
                    updateProgress(i + 1, size);
                }
                return null;
            }
        };
    }

    /**
     * 导入自动操作流程文件
     *
     * @param jsonFile 要解析的文件
     * @throws IOException 导入自动化流程文件内容格式不正确
     */
    private static List<ClickPositionVO> loadPMCFile(File jsonFile) throws IOException {
        // 读取 JSON 文件并转换为 List<ClickPositionBean>
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = jsonFile.getAbsolutePath();
        List<ClickPositionBean> clickPositionBeans;
        try {
            clickPositionBeans = objectMapper.readValue(jsonFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
        } catch (MismatchedInputException | JsonParseException e) {
            throw new RuntimeException(text_loadAutoClick() + filePath + text_formatError());
        }
        // 定时执行导入自动操作并执行时如果不立刻设置序号会导致运行时找不到序号
        int index = 0;
        List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(clickPositionBeans)) {
            for (ClickPositionBean bean : clickPositionBeans) {
                ClickPositionVO vo = new ClickPositionVO();
                try {
                    // 自动拷贝父类中的属性
                    copyAllProperties(bean, vo);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                // 初始化子类特有属性
                vo.setTableView(autoClickController.tableView_Click)
                        .setRemove(false)
                        .setUuid(UUID.randomUUID().toString());
                vo.setIndex(index++);
                if (!retryTypeMap.containsKey(vo.getRetryTypeEnum())
                        || !clickTypeMap.containsKey(vo.getClickTypeEnum())
                        || !matchedTypeMap.containsKey(vo.getMatchedTypeEnum())
                        || !recordClickTypeMap.containsKey(vo.getClickKeyEnum())
                        || !activationList.contains(vo.getRandomClick())
                        || !activationList.contains(vo.getRandomWaitTime())
                        || !activationList.contains(vo.getRandomClickTime())
                        || !activationList.contains(vo.getRandomTrajectory())
                        || !activationList.contains(vo.getRandomClickInterval())
                        || !isInIntegerRange(vo.getStartX(), 0, null)
                        || !isInIntegerRange(vo.getStartY(), 0, null)
                        || !isInIntegerRange(vo.getRandomX(), 0, null)
                        || !isInIntegerRange(vo.getRandomY(), 0, null)
                        || !isInIntegerRange(vo.getImgX(), null, null)
                        || !isInIntegerRange(vo.getImgY(), null, null)
                        || !isInIntegerRange(vo.getWaitTime(), 0, null)
                        || !isInIntegerRange(vo.getClickNum(), 0, null)
                        || !isInIntegerRange(vo.getClickTime(), 0, null)
                        || !isInIntegerRange(vo.getClickInterval(), 0, null)
                        || !isInIntegerRange(vo.getStopRetryTimes(), 0, null)
                        || !isInIntegerRange(vo.getClickRetryTimes(), 0, null)
                        || !isInIntegerRange(vo.getRandomClickTime(), 0, null)
                        || !isInIntegerRange(vo.getStopMatchThreshold(), 0, 100)
                        || !isInIntegerRange(vo.getClickMatchThreshold(), 0, 100)) {
                    throw new RuntimeException(text_missingKeyData());
                }
                vo.setUuid(UUID.randomUUID().toString());
                clickPositionVOS.add(vo);
            }
        }
        return clickPositionVOS;
    }

}
