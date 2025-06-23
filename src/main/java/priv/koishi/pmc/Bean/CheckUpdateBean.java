package priv.koishi.pmc.Bean;

import lombok.Data;

/**
 * 检查更新数据类
 *
 * @author KOISHI
 * Date:2025-06-23
 * Time:15:25
 */
@Data
public class CheckUpdateBean {

    /**
     * 版本号
     */
    String version;

    /**
     * 构建日期
     */
    String buildDate;

    /**
     * 更新内容
     */
    String whatsNew;

    /**
     * 下载链接
     */
    String downloadLink;

}
