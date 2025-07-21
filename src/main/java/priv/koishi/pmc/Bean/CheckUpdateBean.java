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
     * 阿里云下载链接
     */
    String aliyunFileLink;

    /**
     * 支付宝云下载链接
     */
    String alipayFileLink;

    /**
     * 是否全量更新（true-全量更新 false-增量更新）
     */
    boolean fullUpdate;

}
