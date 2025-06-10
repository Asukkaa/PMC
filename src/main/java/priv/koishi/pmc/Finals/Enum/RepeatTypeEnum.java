package priv.koishi.pmc.Finals.Enum;

import lombok.Getter;

/**
 * 定时任务重复类型枚举类
 *
 * @author KOISHI
 * Date:2025-06-10
 * Time:19:00
 */
@Getter
public enum RepeatTypeEnum {

    /**
     * 每天
     */
    DAILY("DAILY"),

    /**
     * 每周
     */
    WEEKLY("WEEKLY"),

    /**
     * 仅一次
     */
    ONCE("ONCE");

    /**
     * 定时任务重复类型
     */
    private final String repeatType;

    /**
     * 定时任务重复类型枚举类构造函数
     *
     * @param repeatType 定时任务重复类型
     */
    RepeatTypeEnum(String repeatType) {
        this.repeatType = repeatType;
    }

}
