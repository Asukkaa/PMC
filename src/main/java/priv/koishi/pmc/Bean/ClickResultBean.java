package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 操作结果类
 *
 * @author KOISHI
 * Date:2025-05-08
 * Time:14:14
 */
@Data
@Accessors(chain = true)
public class ClickResultBean {

    /**
     * 要跳转的步骤索引，0 为不跳转，-1 为再次执行
     */
    int stepIndex;

    /**
     * 操作日志
     */
    List<ClickLogBean> clickLogs;

}
