package priv.koishi.pmc.Bean.Annotation;

import java.lang.annotation.*;

import static priv.koishi.pmc.Finals.CommonFinals.disable;
import static priv.koishi.pmc.Finals.CommonFinals.enable;

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
     * 启用状态对应的原始值（默认 {@value priv.koishi.pmc.Finals.CommonFinals#enable}）
     */
    String enabledValue() default enable;

    /**
     * 禁用状态对应的原始值（默认 {@value priv.koishi.pmc.Finals.CommonFinals#disable}）
     */
    String disabledValue() default disable;

}
