package priv.koishi.pmc.Bean.Result;

import priv.koishi.pmc.Bean.PMCListBean;

import java.util.List;

/**
 * PMCS 文件导入结果类
 *
 * @author KOISHI
 * Date:2025-08-26
 * Time:17:54
 */
public record PMCSLoadResult(List<PMCListBean> pmcListBeans, String lastPMCPath) {
}
