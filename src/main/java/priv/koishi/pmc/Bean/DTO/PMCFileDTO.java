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
     * 文件拓展名（默认 {@value priv.koishi.pmc.Finals.CommonFinals#PMC}）
     */
    private String ext = CommonFinals.PMC;

    /**
     * PMC 文件版本（默认 {@value priv.koishi.pmc.Finals.CommonFinals#PMCFileVersion}）
     */
    private String version = CommonFinals.PMCFileVersion;

    /**
     * 程序名称（默认 {@value priv.koishi.pmc.Finals.CommonFinals#appName}）
     */
    private String appName = CommonFinals.appName;

    /**
     * 脚本内容
     */
    @JsonSerialize(contentAs = ClickPositionBean.class)
    private List<ClickPositionBean> pmcList;

}
