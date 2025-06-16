package priv.koishi.pmc.Finals.Enum;

import lombok.Getter;

/**
 * 语言枚举类
 *
 * @author KOISHI
 * Date:2025-06-16
 * Time:16:31
 */
@Getter
public enum LanguageEnum {

    /**
     * 简体中文
     */
    zh_CN("简体中文"),

    /**
     * 繁体中文
     */
    zh_TW("繁體中文"),

    /**
     * 英文
     */
    en("English");

    /**
     * 语言名称
     */
    private final String string;

    /**
     * 语言枚举构造函数
     *
     * @param string 语言名称
     */
    LanguageEnum(String string) {
        this.string = string;
    }

}
