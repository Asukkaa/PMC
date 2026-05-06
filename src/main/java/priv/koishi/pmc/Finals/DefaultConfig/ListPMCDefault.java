package priv.koishi.pmc.Finals.DefaultConfig;

import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.disable;
import static priv.koishi.pmc.Finals.CommonFinals.enable;
import static priv.koishi.pmc.Finals.CommonKeys.*;

/**
 * 批量执行自动任务默认配置类
 *
 * @author Koishi
 * Date:2026-05-06
 * Time:11:43
 */
public class ListPMCDefault {

    /**
     * 批量执行自动任务配置文件路径
     */
    public static final String configFile_List = "config/ListPMCConfig.properties";

    /**
     * 批量执行自动任务默认配置
     */
    public static final Properties listPMCProperties = new Properties();

    static {
        // 循环次数
        listPMCProperties.put(key_loopTime, "");
        // 导入文件的选择器默认选中路径
        listPMCProperties.put(key_inFilePath, "");
        // 选择导出文件夹时选择器的默认路径
        listPMCProperties.put(key_outFilePath, "");
        // 导出自动流程文件名称
        listPMCProperties.put(key_outFileName, "");
        // 自动保存操作列表
        listPMCProperties.put(key_autoSave, enable);
        // 加载配置文件
        listPMCProperties.put(key_loadConfig, enable);
        // 以文件夹的形式导入操作流程
        listPMCProperties.put(key_loadFolder, disable);
        // 文件重名不覆盖
        listPMCProperties.put(key_notOverwrite, enable);
        // 导出后打开文件夹
        listPMCProperties.put(key_openDirectory, enable);
        // 运行自动操作前准备时间
        listPMCProperties.put(key_preparationRunTime, "");
    }

}
