package priv.koishi.pmc.ThreadPool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理器
 *
 * @author KOISHI
 * Date:2025-04-28
 * Time:14:43
 */
public class ThreadPoolManager {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(ThreadPoolManager.class);

    /**
     * 线程池集合
     */
    private static final Map<Class<?>, ExecutorService> POOLS = new ConcurrentHashMap<>();

    /**
     * 获取或创建指定类别的线程池（线程安全）
     *
     * @param clazz 线程池关联的业务类，用于区分不同业务场景的线程池
     * @return 已存在的或新建的线程池实例
     */
    public static ExecutorService getPool(Class<?> clazz) {
        return POOLS.computeIfAbsent(clazz, k ->
                new CommonThreadPoolExecutor().createNewThreadPool()
        );
    }

    /**
     * 关闭所有线程池资源
     */
    public static void shutdownAll() {
        POOLS.forEach((clazz, pool) -> {
            if (!pool.isShutdown()) {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                        List<Runnable> dropped = pool.shutdownNow();
                        logger.warn("{} 线程池强制关闭，丢弃 {} 个任务", clazz.getSimpleName(), dropped.size());
                    }
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                } catch (SecurityException e) {
                    logger.error("线程池关闭失败", e);
                }
            }
        });
        POOLS.clear();
    }

}
