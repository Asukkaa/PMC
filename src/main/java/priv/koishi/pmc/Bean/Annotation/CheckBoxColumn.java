package priv.koishi.pmc.Bean.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 boolean 字段，在 TableView 中生成带文本的 CheckBox 列
 *
 * @author Koishi
 * Date:2026-04-22
 * Time:17:11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckBoxColumn {

    /**
     * CheckBox 显示的文本对应的 key
     */
    String textKey() default "enable";

}
