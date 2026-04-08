package priv.koishi.pmc.Bean;

import lombok.Data;

/**
 * UniCloud 返回结构类
 *
 * @author KOISHI
 * Date:2025-06-23
 * Time:17:13
 */
@Data
public class UniCloudResponse {

    /**
     * 状态码
     */
    private int code;

    /**
     * 检查更新数据类
     */
    private CheckUpdateBean data;

    /**
     * 状态信息
     */
    private String message;

}
