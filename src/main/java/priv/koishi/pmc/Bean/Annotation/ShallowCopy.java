package priv.koishi.pmc.Bean.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段在复制时仅进行浅拷贝（引用复制），不创建深拷贝副本
 *
 * @author Koishi
 * Date:2026-08-19
 * Time:21:47
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShallowCopy {
}
