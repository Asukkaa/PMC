package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.VO.Indexable;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static priv.koishi.pmc.Finals.CommonFinals.DTF;

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
    Integer index;

    /**
     * 操作时间
     */
    String date = LocalDateTime.now(ZoneId.systemDefault()).format(DTF);

    /**
     * 操作名称
     */
    String name;

    /**
     * 操作类型
     */
    String type;

    /**
     * 横坐标
     */
    String X;

    /**
     * 纵坐标
     */
    String Y;

    /**
     * 操作时长
     */
    String clickTime;

    /**
     * 操作按键
     */
    String clickKey;

    /**
     * 操作结果
     */
    String result;

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
