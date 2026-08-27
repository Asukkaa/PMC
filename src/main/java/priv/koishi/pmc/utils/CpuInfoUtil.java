package priv.koishi.pmc.utils;

import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.ffm.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import priv.koishi.pmc.bean.CPUInfo;

import static priv.koishi.pmc.finals.CommonFinals.AMD;
import static priv.koishi.pmc.finals.CommonFinals.Intel;
import static priv.koishi.pmc.finals.i18nFinal.text_unknow;

/**
 * CPU 信息工具类
 *
 * @author Koishi
 * Date:2026-08-05
 * Time:14:53
 */
public class CpuInfoUtil {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(CpuInfoUtil.class);

    /**
     * CPU 信息
     */
    public static CPUInfo cpuInfo;

    /**
     * 获取 CPU 参数任务
     *
     * @return 无返回值的 Task
     */
    public static Task<Void> checkCPU() {
        return new Task<>() {
            @Override
            protected Void call() {
                fetchCpuInfo();
                logger.info("CPU 信息: {}", cpuInfo);
                return null;
            }
        };
    }

    /**
     * 获取 CPU 信息（型号、核心数、类型）
     */
    private static void fetchCpuInfo() {
        String model = text_unknow();
        // 使用 OSHI 获取硬件信息
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        // 获取 CPU 型号
        String name = processor.getProcessorIdentifier().getName();
        if (StringUtils.isNotBlank(name)) {
            model = name;
        }
        // 获取物理核心数
        int physical = processor.getPhysicalProcessorCount();
        // 获取逻辑核心数
        int logical = processor.getLogicalProcessorCount();
        // 解析 CPU 类型（Intel / AMD / 未知）
        String type = parseTypeFromModel(model);
        cpuInfo = new CPUInfo()
                .setPhysicalCores(physical)
                .setLogicalCores(logical)
                .setName(model)
                .setType(type);
    }

    /**
     * 从 CPU 型号字符串中解析类型（Intel / AMD / 未知）
     *
     * @param model CPU 型号
     * @return 类型字符串
     */
    private static String parseTypeFromModel(String model) {
        if (model == null) {
            return text_unknow();
        }
        if (model.contains(AMD) || model.contains("Ryzen") || model.contains("EPYC") || model.contains("Athlon")) {
            return AMD;
        }
        if (model.contains(Intel) || model.contains("Core") || model.contains("Xeon") || model.contains("Pentium") || model.contains("Celeron")) {
            return Intel;
        }
        return text_unknow();
    }

}
