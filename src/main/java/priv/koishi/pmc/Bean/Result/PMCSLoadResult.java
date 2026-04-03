package priv.koishi.pmc.Bean.Result;

import priv.koishi.pmc.Bean.PMCListBean;

import java.util.List;

/**
 * PMCS 文件导入结果类
 *
 * @param pmcListBeans 导入的 PMCS 文件数据
 * @param lastPMCPath  最后导入的文件路径
 * @author KOISHI
 * Date:2025-08-26
 * Time:17:54
 */
public record PMCSLoadResult(List<PMCListBean> pmcListBeans, String lastPMCPath) {
}
