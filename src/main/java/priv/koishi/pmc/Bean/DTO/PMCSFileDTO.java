package priv.koishi.pmc.Bean.DTO;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.PMCListBean;
import priv.koishi.pmc.Finals.CommonFinals;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * PMCS 导出文件描述
 *
 * @author KOISHI
 * Date:2026-01-04
 * Time:15:11
 */
@Data
@Accessors(chain = true)
public class PMCSFileDTO {

    /**
     * 文件拓展名
     */
    String ext = CommonFinals.PMCS;

    /**
     * PMC 文件版本
     */
    String version = CommonFinals.PMCSFileVersion;

    /**
     * 程序名称
     */
    String appName = CommonFinals.appName;

    /**
     * 脚本内容
     */
    @JsonSerialize(contentAs = PMCListBean.class)
    List<PMCListBean> pmcsList;

}
