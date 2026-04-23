package priv.koishi.pmc.Bean.Annotation;

import java.lang.annotation.*;

/**
 * 标记该字段对应的表格列为文件路径列
 * <p>将使用 JavaFX 属性绑定方式显示（要求 Bean 实现 FilePath 接口并提供 pathProperty 方法）
 * <p>适用于 String 类型字段
 *
 * @author Koishi
 * Date:2026-04-23
 * Time:14:03
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathColumn {
}
