package priv.koishi.pmc.Service.TaskInterface;

/**
 * 进度更新函数接口
 *
 * @author KOISHI
 * Date:2026-01-19
 * Time:17:05
 */
@FunctionalInterface
public interface ProgressUpdater {

    /**
     * 更新进度
     *
     * @param workDone 已完成的工作
     * @param max      总工作
     */
    void update(long workDone, long max);

}