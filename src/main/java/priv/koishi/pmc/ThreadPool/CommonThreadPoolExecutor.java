package priv.koishi.pmc.ThreadPool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 公共线程池类
 *
 * @author KOISHI
 * Date:2024-10-30
 * Time:下午8:17
 */
public class CommonThreadPoolExecutor {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(CommonThreadPoolExecutor.class);

    /**
     * 核心线程池大小
     */
    int corePoolSize = 2;

    /**
     * 最大线程池大小
     */
    int maximumPoolSize = 3;

    /**
     * 线程最大空闲时间
     */
    long keepAliveTime = 10;

    /**
     * 时间单位
     */
    TimeUnit unit = TimeUnit.SECONDS;

    /**
     * 线程等待队列
     */
    BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1);

    /**
     * 线程创建工厂
     */
    ThreadFactory threadFactory = new CommonThreadFactory();

    /**
     * 拒绝策略
     */
    RejectedExecutionHandler handler = new CommonIgnorePolicy();

    /**
     * 静态线程池集合，用于跟踪所有创建的ExecutorService实例
     */
    private static final Set<ExecutorService> POOLS = ConcurrentHashMap.newKeySet();

    /**
     * 关闭钩子注册状态标志位
     */
    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    /**
     * 构造函数
     */
    public CommonThreadPoolExecutor() {
    }

    /**
     * 默认线程池
     *
     * @return 线程池
     */
    public ExecutorService createNewThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(this.corePoolSize, this.maximumPoolSize,
                this.keepAliveTime, this.unit, this.workQueue, this.threadFactory, this.handler);
        POOLS.add(executor);
        registerShutdownHook();
        return executor;
    }

    /**
     * 添加关闭钩子
     */
    private void registerShutdownHook() {
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!POOLS.isEmpty()) {
                    shutdownAll();
                }
            }));
        }
    }

    /**
     * 停止所有线程池
     */
    private void shutdownAll() {
        POOLS.removeIf(ExecutorService::isShutdown);
        POOLS.forEach(pool -> {
            try {
                pool.shutdownNow();
            } catch (SecurityException e) {
                Thread.currentThread().interrupt();
                logger.error("线程池关闭过程中断", e);
            } finally {
                POOLS.remove(pool);
            }
        });
    }

}
