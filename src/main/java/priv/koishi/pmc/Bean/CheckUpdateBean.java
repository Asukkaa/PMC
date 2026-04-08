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
    private String version;

    /**
     * 构建日期
     */
    private String buildDate;

    /**
     * 更新内容
     */
    private String whatsNew;

    /**
     * 阿里云下载链接
     */
    private String aliyunFileLink;

    /**
     * 支付宝云下载链接
     */
    private String alipayFileLink;

    /**
     * 是否全量更新（true-全量更新 false-增量更新）
     */
    private boolean fullUpdate;

}
