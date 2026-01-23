package priv.koishi.pmc.Bean.Result;

import priv.koishi.pmc.Bean.VO.ClickPositionVO;

import java.util.List;

/**
 * PMC 文件导入结果类
 *
 * @author KOISHI
 * Date:2025-08-26
 * Time:17:54
 */
public record PMCLoadResult(List<ClickPositionVO> clickPositionList, String lastPMCPath) {
}
