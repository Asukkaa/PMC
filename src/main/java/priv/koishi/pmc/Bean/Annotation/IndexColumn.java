package priv.koishi.pmc.Bean.Annotation;

import java.lang.annotation.*;

/**
 * 标记该字段对应的表格列为序号列（自动显示行号）
 * <p>适用于 Integer 或 int 类型的字段
 *
 * @author Koishi
 * Date:2026-04-23
 * Time:14:01
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexColumn {
}
