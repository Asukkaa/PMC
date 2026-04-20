package priv.koishi.pmc.Service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.TessdataBean;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static priv.koishi.pmc.Finals.CommonFinals.traineddata;
import static priv.koishi.pmc.Finals.i18nFinal.text_readData;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;

/**
 * 文字识别相关文件服务类
 *
 * @author applesaucepenguin
 * Date 2026-04-03
 * time 17:31
 */
public class TessdataService {

    /**
     * 批量加载 .traineddata 模型文件任务线程
     *
     * @param taskBean 线程任务参数
     * @param files    .traineddata 模型文件列表
     * @return 带有最后一个符合条件的文件的 task
     */
    public static Task<File> loadTessdata(TaskBean<TessdataBean> taskBean, List<? extends File> files) {
        return new Task<>() {
            @Override
            protected File call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                File selectedFile = null;
                // 过滤不符合的文件格式
                List<File> selectedFiles = new ArrayList<>();
                for (File file : files) {
                    if (traineddata.equals(getExistsFileType(file))) {
                        selectedFile = file;
                        selectedFiles.add(file);
                    }
                }
                int size = selectedFiles.size();
                updateProgress(0, size);
                for (int i = 0; i < size; i++) {
                    File file = selectedFiles.get(i);
                    Path tessdataPath = Path.of(getTessdataPath() + File.separator + file.getName());
                    try {
                        // 将模型文件复制到 tessdata 目录
                        Files.copy(file.toPath(), tessdataPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    updateProgress(i + 1, size);
                }
                return selectedFile;
            }
        };
    }

    /**
     * 更新 .traineddata 模型文件列表任务线程
     *
     * @param taskBean 线程任务参数
     * @return 无返回值 task
     */
    public static Task<Void> updateTessdata(TaskBean<TessdataBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData());
                // 读取模型文件
                List<TessdataBean> list = new ArrayList<>();
                File tessdataPathFile = new File(getTessdataPath());
                File[] files = tessdataPathFile.listFiles();
                List<TessdataBean> configList = taskBean.getBeanList();
                if (files != null) {
                    int size = files.length;
                    updateProgress(0, size);
                    for (int i = 0; i < size; i++) {
                        File file = files[i];
                        if (traineddata.equals(getExistsFileType(file))) {
                            list.add(new TessdataBean(file));
                        }
                        updateProgress(i + 1, size);
                    }
                    // 如果没有传配置数据则读取配置文件
                    if (CollectionUtils.isEmpty(configList)) {
                        ObjectMapper objectMapper = JsonMapper.builder()
                                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                .build();
                        String configPath = getTessdataConfigPath();
                        configList = objectMapper.readValue(new File(configPath), new TypeReference<>() {
                        });
                    }
                    int configListSize = configList.size();
                    updateProgress(0, configListSize);
                    // 构建配置数据与模型文件的映射
                    Map<String, TessdataBean> configMap = new HashMap<>();
                    for (int i = 0; i < configListSize; i++) {
                        TessdataBean config = configList.get(i);
                        String name = config.getName();
                        if (name != null) {
                            configMap.put(name, config);
                        }
                        updateProgress(i + 1, configListSize);
                    }
                    // 根据配置排序
                    list.sort((a, b) -> {
                        String nameA = a.getName();
                        String nameB = b.getName();
                        boolean inConfigA = configMap.containsKey(nameA);
                        boolean inConfigB = configMap.containsKey(nameB);
                        if (inConfigA && inConfigB) {
                            // 两个都在配置中，按 index 升序
                            int cmp = Integer.compare(configMap.get(nameA).getIndex(), configMap.get(nameB).getIndex());
                            if (cmp != 0) {
                                return cmp;
                            }
                            return nameA.compareTo(nameB);
                        } else if (inConfigA) {
                            // 只有 A 在配置中，A 排在前面
                            return -1;
                        } else if (inConfigB) {
                            // 只有 B 在配置中，B 排在前面
                            return 1;
                        } else {
                            // 都不在配置中，按文件名排序
                            return a.getName().compareTo(b.getName());
                        }
                    });
                    int listSize = list.size();
                    updateProgress(0, listSize);
                    // 填充备注
                    for (int i = 0; i < listSize; i++) {
                        TessdataBean bean = list.get(i);
                        String name = bean.getName();
                        if (configMap.containsKey(name)) {
                            TessdataBean tessdataBean = configMap.get(name);
                            bean.setRemark(tessdataBean.getRemark())
                                    .setActive(tessdataBean.isActive());
                        }
                        updateProgress(i + 1, listSize);
                    }
                    // 展示数据
                    TableView<? super TessdataBean> tableView = taskBean.getTableView();
                    Platform.runLater(() -> {
                        tableView.getItems().clear();
                        tableView.getItems().addAll(list);
                    });
                }
                return null;
            }
        };
    }

    /**
     * 保存 tessdata 设置任务线程
     *
     * @param taskBean 线程任务参数
     * @return 无返回值 task
     */
    public static Task<Void> saveTessdataConfig(TaskBean<TessdataBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() {
                changeDisableNodes(taskBean, true);
                List<TessdataBean> tessdataBeans = taskBean.getBeanList();
                for (int i = 0; i < tessdataBeans.size(); i++) {
                    TessdataBean tessdataBean = tessdataBeans.get(i);
                    tessdataBean.setIndex(i + 1);
                }
                String configPath = getTessdataConfigPath();
                // 序列化数据
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(new File(configPath), tessdataBeans);
                return null;
            }
        };
    }

}
