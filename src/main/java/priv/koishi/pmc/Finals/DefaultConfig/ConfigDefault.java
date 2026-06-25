package priv.koishi.pmc.Finals.DefaultConfig;

import priv.koishi.pmc.Finals.Enum.RepeatTypeEnum;

import java.time.LocalDate;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.disable;
import static priv.koishi.pmc.Finals.CommonFinals.enable;
import static priv.koishi.pmc.Finals.CommonKeys.*;

/**
 * 程序主默认配置类
 *
 * @author Koishi
 * Date:2026-05-06
 * Time:11:42
 */
public class ConfigDefault {

    /**
     * app 配置文件路径
     */
    public static final String configFile = "config/Config.properties";

    /**
     * 程序主默认配置
     */
    public static final Properties configProperties = new Properties();

    static {
        // 应用界面配色
        configProperties.put(key_theme, "2");
        // 日志保留数量
        configProperties.put(key_logsNum, "3");
        // 运行自动操作快捷键
        configProperties.put(key_runKey, "42 87");
        // tessdata 模型文件选择器上次打开路径
        configProperties.put(key_tessdataPath, "");
        // 程序端口号
        configProperties.put(key_appPort, "52514");
        // 第一次打开应用
        configProperties.put(key_firstRun, enable);
        // 取消快捷键
        configProperties.put(key_cancelKey, enable);
        // 应用界面语言
        configProperties.put(key_language, "简体中文");
        // 录制自动操作快捷键
        configProperties.put(key_recordKey, "42 68");
        // 加载配置文件
        configProperties.put(key_loadConfig, enable);
        // 最大化窗口
        configProperties.put(key_maxWindow, disable);
        // 全屏窗口
        configProperties.put(key_fullWindow, disable);
        // 记录窗口最大化设置
        configProperties.put(key_loadMaxWindow, enable);
        // 拓展标题栏设置
        configProperties.put(key_extendedStage, enable);
        // 记录窗口全屏设置
        configProperties.put(key_loadFullWindow, enable);
        // 文件列表计算文件夹大小
        configProperties.put(key_checkDirectory, enable);
        // 程序关闭时的页面
        configProperties.put(key_lastTab, "autoClickTab");
        // 最后检查更新日期
        configProperties.put(key_lastCheck, String.valueOf(LocalDate.now()));
        // 自动检测更新
        configProperties.put(key_autoCheck, RepeatTypeEnum.MONTHLY.getRepeatType());
    }

}
