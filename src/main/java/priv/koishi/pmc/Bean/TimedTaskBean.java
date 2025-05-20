package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.VO.Indexable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务数据类
 *
 * @author KOISHI
 * Date:2025-05-19
 * Time:16:44
 */
@Data
@Accessors(chain = true)
public class TimedTaskBean implements Indexable {

    /**
     * 序号
     */
    Integer index;

    /**
     * 任务名称
     */
    String taskName;

    /**
     * 起始日期
     */
    String date;

    /**
     * 触发时间
     */
    String time;

    /**
     * 重复类型
     */
    String repeat;

    /**
     * 重复日
     */
    String days;

    /**
     * 执行流程
     */
    String name;

    /**
     * 文件地址
     */
    String path;

    /**
     * 完整的时间信息
     */
    LocalDateTime dateTime;

    /**
     * 设置的星期信息
     */
    List<Integer> dayList;

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
