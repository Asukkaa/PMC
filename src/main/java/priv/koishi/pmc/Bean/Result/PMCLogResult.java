package priv.koishi.pmc.Bean.Result;

import priv.koishi.pmc.Bean.ClickLogBean;
import priv.koishi.pmc.Bean.PMCLogBean;

import java.util.List;

/**
 * PMC 文件执行日志记录类
 *
 * @author KOISHI
 * Date:2026-01-23
 * Time:18:00
 */
public record PMCLogResult(List<ClickLogBean> clickLogBeans, List<PMCLogBean> pmcLogBeans) {
}
