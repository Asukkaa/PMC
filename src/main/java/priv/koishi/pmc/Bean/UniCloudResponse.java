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
    int code;

    /**
     * 检查更新数据类
     */
    CheckUpdateBean data;

    /**
     * 状态信息
     */
    String message;

}
