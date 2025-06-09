package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import priv.koishi.pmc.Bean.VO.Indexable;

import java.time.LocalDateTime;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.repeatType_weekly;
import static priv.koishi.pmc.Finals.CommonFinals.text_onlyLaunch;

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

    /**
     * 判断两个定时任务数据是否相同
     *
     * @param bean 要判断的定时任务数据
     * @return true表示相同，false表示不同
     */
    public boolean equals(TimedTaskBean bean) {
        if (bean == this) {
            return true;
        }
        if (bean == null) {
            return false;
        }
        String equalsPath = path;
        if (text_onlyLaunch().equals(equalsPath)) {
            equalsPath = "";
        }
        String beanPath = bean.getPath();
        if (text_onlyLaunch().equals(beanPath)) {
            beanPath = "";
        }
        String equalsDays = days;
        if (!repeatType_weekly().equals(repeat)) {
            equalsDays = "";
        }
        String beanDays = bean.getDays();
        if (!repeatType_weekly().equals(bean.getRepeat())) {
            beanDays = "";
        }
        return new EqualsBuilder()
                .append(taskName, bean.getTaskName())
                .append(equalsPath, beanPath)
                .append(dateTime, bean.getDateTime())
                .append(repeat, bean.getRepeat())
                .append(equalsDays, beanDays)
                .isEquals();
    }

    /**
     * 获取哈希码
     *
     * @return 哈希码
     */
    public int hashCode() {
        return taskName.hashCode();
    }

}
