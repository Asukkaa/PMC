package priv.koishi.pmc.Service.TaskInterface;

/**
 * 消息更新函数接口
 *
 * @author KOISHI
 * Date:2026-01-19
 * Time:17:04
 */
@FunctionalInterface
public interface MessageUpdater {

    /**
     * 更新消息
     *
     * @param message 新消息
     */
    void update(String message);

}