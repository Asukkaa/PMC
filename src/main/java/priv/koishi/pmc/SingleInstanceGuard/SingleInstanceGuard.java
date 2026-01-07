package priv.koishi.pmc.SingleInstanceGuard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static priv.koishi.pmc.Finals.CommonFinals.activatePMC;

/**
 * 单实例守护
 *
 * @author KOISHI
 * Date:2025-04-24
 * Time:15:31
 */
public class SingleInstanceGuard {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(SingleInstanceGuard.class);

    /**
     * 单例应用实例锁文件名
     *
     * <p>用于通过文件锁机制保证应用单实例运行的锁定文件名， 默认使用隐藏文件 .app_instance.lock</p>
     */
    private static final String LOCK_FILE_NAME = ".app_instance.lock";

    /**
     * 心跳间隔时间（单位：毫秒）
     *
     * <p>定时向文件锁发送心跳信号的时间间隔，用于维持文件锁有效性</p>
     */
    private static final long HEARTBEAT_INTERVAL = 30_000;

    /**
     * 锁超时时间（单位：毫秒）
     *
     * <p>文件锁的最大持有时间，超过该时间未收到心跳则认为实例已失效</p>
     */
    private static final long LOCK_TIMEOUT = 120_000;

    /**
     * 单例文件锁对象
     *
     * <p>通过 FileLock 实现跨进程的排他锁， 用于保证同一时间只有一个应用实例运行</p>
     */
    private static FileLock fileLock;

    /**
     * 文件通道对象
     *
     * <p>与文件锁关联的 NIO 文件通道，用于维护文件锁的生命周期</p>
     */
    private static FileChannel lockChannel;

    /**
     * 定时任务调度器
     *
     * <p>ScheduledExecutorService 实例， 用于定期执行文件锁心跳维持任务</p>
     */
    private static ScheduledExecutorService scheduler;

    /**
     * 检查应用实例是否已运行
     *
     * @param port 激活信号端口
     * @return true-已有实例运行，false-当前是首个实例
     */
    public static boolean checkRunning(int port, String[] args) {
        try {
            // 获取锁文件路径
            Path lockPath = getLockFilePath();
            // 清理过期锁
            cleanStaleLock(lockPath);
            // 尝试获取文件锁
            lockChannel = FileChannel.open(lockPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
            fileLock = lockChannel.tryLock();
            // 若文件已锁定或心跳守护线程启动失败则返回 true
            if (fileLock == null || !startHeartbeat()) {
                // 发送激活窗口信号
                sendActivationSignal(port, args);
                // 释放文件锁资源
                releaseResources();
                return true;
            }
            // 注册 JVM 关闭钩子
            addShutdownHook();
            return false;
        } catch (IOException e) {
            logger.error("文件锁操作异常", e);
            return true;
        }
    }

    /**
     * 获取跨平台锁文件路径
     *
     * @return 返回位于系统临时目录的锁文件路径
     */
    private static Path getLockFilePath() {
        return Paths.get(System.getProperty("java.io.tmpdir"), LOCK_FILE_NAME);
    }

    /**
     * 清理过期锁文件
     *
     * @param lockPath 要清理的锁文件路径
     * @throws IOException 文件操作异常时抛出
     */
    private static void cleanStaleLock(Path lockPath) throws IOException {
        if (Files.exists(lockPath)) {
            // 尝试获取测试锁
            try (FileChannel ch = FileChannel.open(lockPath, StandardOpenOption.WRITE)) {
                FileLock testLock = ch.tryLock();
                // 测试锁，若文件未被占用则执行删除
                if (testLock != null) {
                    testLock.release();
                    long lastModified = Files.getLastModifiedTime(lockPath).toMillis();
                    // 若文件最后修改时间超过阈值，则删除文件
                    if (System.currentTimeMillis() - lastModified > LOCK_TIMEOUT) {
                        Files.delete(lockPath);
                    }
                }
            } catch (IOException e) {
                logger.error("锁文件验证异常", e);
            }
        }
    }

    /**
     * 启动心跳守护线程
     *
     * @return true-心跳线程启动成功，false-启动失败
     */
    private static boolean startHeartbeat() {
        try {
            // 创建单线程定时调度器（守护线程）
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Heartbeat-Thread");
                t.setDaemon(true);
                return t;
            });
            // 定时执行心跳操作
            scheduler.scheduleAtFixedRate(() -> {
                synchronized (SingleInstanceGuard.class) {
                    try {
                        Path path = getLockFilePath();
                        if (!Files.exists(path)) {
                            logger.info("锁文件不存在，重建文件");
                            lockChannel = FileChannel.open(path,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.READ,
                                    StandardOpenOption.WRITE);
                            fileLock = lockChannel.tryLock();
                        }
                        // 向锁文件写入心跳标记
                        if (lockChannel.isOpen() && Files.isWritable(path)) {
                            // 强制同步文件内容到磁盘
                            int write = lockChannel.write(ByteBuffer.wrap(new byte[]{0x1}));
                            if (write != 1) {
                                logger.error("心跳写入异常，实际写入字节数: {}", write);
                            }
                            lockChannel.force(true);
                            // 更新文件最后修改时间
                            Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
                        }
                    } catch (ClosedChannelException e) {
                        logger.error("通道已关闭，终止心跳");
                        scheduler.shutdownNow();
                    } catch (IOException e) {
                        logger.error("心跳写入失败", e);
                    }
                }
            }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            logger.error("心跳线程启动失败", e);
            return false;
        }
    }

    /**
     * 注册 JVM 关闭钩子
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Path lockPath = getLockFilePath();
                if (Files.exists(lockPath)) {
                    Files.deleteIfExists(lockPath);
                }
            } catch (IOException e) {
                logger.error("锁文件删除失败", e);
            }
            // 释放文件锁资源
            releaseResources();
            if (scheduler != null) {
                scheduler.shutdownNow();
                try {
                    if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                        logger.info("心跳线程强制终止");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
    }

    /**
     * 释放文件锁资源
     */
    private static void releaseResources() {
        try {
            // 释放文件锁
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
            }
            // 关闭通道
            if (lockChannel != null && lockChannel.isOpen()) {
                lockChannel.close();
            }
        } catch (IOException e) {
            logger.error("资源释放异常", e);
        } finally {
            fileLock = null;
            lockChannel = null;
        }
    }

    /**
     * 发送激活正在运行的程序窗口信号
     *
     * @param port 端口号
     */
    private static void sendActivationSignal(int port, String[] args) {
        try (Socket socket = new Socket("localhost", port)) {
            OutputStream out = socket.getOutputStream();
            String payload = activatePMC + "\n" + String.join("\n", args);
            out.write(payload.getBytes());
            logger.info("程序正在运行，发送激活信号及参数: {}", payload);
        } catch (IOException e) {
            logger.error("激活信号发送失败，可能主程序未启动完成", e);
        }
    }

}
