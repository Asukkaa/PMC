package priv.koishi.pmc.Bean.DTO;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Finals.CommonFinals;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * PMC 导出文件描述
 *
 * @author KOISHI
 * Date:2026-01-04
 * Time:15:11
 */
@Data
@Accessors(chain = true)
public class PMCFileDTO {

    /**
     * PMC 文件版本
     */
    String version = CommonFinals.PMCFileVersion;

    /**
     * 程序名称
     */
    String appName = CommonFinals.appName;

    /**
     * 脚本内容
     */
    @JsonSerialize(contentAs = ClickPositionBean.class)
    List<ClickPositionBean> pmcList;

}
