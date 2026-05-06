package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.Interface.Indexable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 操作记录数据类
 *
 * @author KOISHI
 * Date:2025-05-08
 * Time:12:24
 */
@Data
@Accessors(chain = true)
public class ClickLogBean implements Indexable {

    /**
     * 序号
     */
    private Integer index;

    /**
     * 操作时间（默认当前时间）
     */
    private String date = LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

    /**
     * 操作名称
     */
    private String name;

    /**
     * 操作类型
     */
    private String type;

    /**
     * 横坐标
     */
    private String X;

    /**
     * 纵坐标
     */
    private String Y;

    /**
     * 操作时长
     */
    private String clickTime;

    /**
     * 操作按键
     */
    private String clickKey;

    /**
     * 操作结果
     */
    private String result;

    /**
     * 为列表数据设置序号接口
     *
     * @param index 要设置的序号
     */
    @Override
    public void setIndex(int index) {
        this.index = index;
    }

}
