package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.Interface.Indexable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * PMC 文件执行日志类
 *
 * @author KOISHI
 * Date:2026-01-23
 * Time:17:57
 */
@Data
@Accessors(chain = true)
public class PMCLogBean implements Indexable {

    /**
     * 序号
     */
    Integer index;

    /**
     * 操作时间
     */
    String date = LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

    /**
     * 文件名称
     */
    String name;

    /**
     * 文件路径
     */
    String path;

    /**
     * 操作结果
     */
    String result;

    /**
     * 操作时长（单位毫秒）
     */
    String time;

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
