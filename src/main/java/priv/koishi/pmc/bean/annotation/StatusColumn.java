package priv.koishi.pmc.bean.annotation;

import java.lang.annotation.*;

import static priv.koishi.pmc.finals.CommonFinals.disable;
import static priv.koishi.pmc.finals.CommonFinals.enable;

/**
 * 标记字段在表格中需渲染为状态模式
 *
 * @author Koishi
 * Date:2026-04-16
 * Time:18:19
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StatusColumn {

    /**
     * @return 启用状态对应的原始值（默认 {@value priv.koishi.pmc.finals.CommonFinals#enable}）
     */
    String enabledValue() default enable;

    /**
     * @return 禁用状态对应的原始值（默认 {@value priv.koishi.pmc.finals.CommonFinals#disable}）
     */
    String disabledValue() default disable;

}
