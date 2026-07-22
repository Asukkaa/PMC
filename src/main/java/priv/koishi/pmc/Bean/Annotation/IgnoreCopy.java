package priv.koishi.pmc.Bean.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 复制类时将跳过标记字段
 *
 * @author Koishi
 * Date:2026-07-22
 * Time:18:53
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreCopy {
}
