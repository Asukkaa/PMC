package priv.koishi.pmc.bean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CPU 信息类
 *
 * @author Koishi
 * Date:2026-08-10
 * Time:16:29
 */
@Data
@Accessors(chain = true)
public class CPUInfo {

    /**
     * CPU 型号
     */
    private String name;

    /**
     * CPU 厂商类型
     */
    private String type;

    /**
     * CPU 物理核心数
     */
    private int physicalCores;

    /**
     * CPU 逻辑核心
     */
    private int logicalCores;

}
