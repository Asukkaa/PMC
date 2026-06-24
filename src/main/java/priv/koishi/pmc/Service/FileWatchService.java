package priv.koishi.pmc.Service;

import javafx.application.Platform;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * 基于 {@link WatchService} 的文件系统监听器，可手动控制启停。
 * <p>
 * 本类提供独立的后台监听线程，对指定目录（可选递归）进行文件事件监听。
 * 当发生创建、删除、修改（包括大小、隐藏属性等元数据变化）时，
 * 会自动通过 {@link Platform#runLater(Runnable)} 将刷新逻辑调度回 JavaFX 线程执行。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 创建监听服务
 * FileWatchService watcher = new FileWatchService();
 * fileWatchService.setRecursive(true);
 * fileWatchService.setRootPath(Path.of("/your/target/directory"));
 *  // 开始监听
 * watcher.start();
 * // 程序退出或需要停止时
 * watcher.stop();
 * }</pre>
 *
 * @author Koishi
 * Date: 2026-06-24
 * Time:18:50
 */
public class FileWatchService {

    /**
     * 要监听的根目录路径
     */
    @Setter
    private Path rootPath;

    /**
     * 是否递归监听所有子目录
     */
    @Setter
    private boolean recursive;

    /**
     * 底层文件监听服务实例
     */
    private WatchService watchService;

    /**
     * 记录每个 WatchKey 对应的目录路径，用于事件定位
     */
    private final Map<WatchKey, Path> keyMap = new HashMap<>();

    /**
     * 后台监听线程
     */
    private Thread watcherThread;

    /**
     * 标记是否正在运行，用于安全停止
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 文件变更回调
     */
    @Setter
    private Runnable onFileChanged;

    /**
     * 启动文件监听服务。
     * <p>
     * 该方法会创建一个守护线程，初始化 {@link WatchService} 并开始阻塞等待文件事件。
     * 如果服务已在运行中，调用此方法无效果。
     * </p>
     *
     * @throws IllegalStateException 如果监听根目录无效或无法访问
     */
    public synchronized void start() {
        if (running.get()) {
            return;
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("无法创建 WatchService", e);
        }
        // 注册目录
        try {
            if (recursive) {
                registerAll(rootPath);
            } else {
                register(rootPath);
            }
        } catch (IOException e) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
            throw new RuntimeException("无法注册监听目录: " + rootPath, e);
        }
        running.set(true);
        // 允许 JVM 退出时自动终止
        watcherThread = Thread.ofVirtual()
                .name("FileWatchService-Virtual")
                .start(this::watchLoop);
    }

    /**
     * 停止文件监听并释放资源。
     * <p>
     * 会中断后台监听线程并关闭底层的 {@link WatchService}。
     * 可以重复调用，不会抛出异常。停止后如需重新监听需要创建新实例。
     * </p>
     *
     * @throws IOException 服务关闭异常
     */
    public synchronized void stop() throws IOException {
        if (!running.getAndSet(false)) {
            return;
        }
        // 关闭 WatchService 以中断 take() 阻塞
        if (watchService != null) {
            watchService.close();
        }
        // 中断线程（防止某些极端情况未被 close 唤醒）
        if (watcherThread != null && watcherThread.isAlive()) {
            watcherThread.interrupt();
        }
        keyMap.clear();
    }

    /**
     * 以新的根目录和递归设置重启监听。
     *
     * @throws IOException 服务关闭异常
     */
    public synchronized void restart() throws IOException {
        // 先完全停止当前监听
        stop();
        // 重新启动
        start();
    }

    /**
     * 后台监听主循环。
     * 不断阻塞等待文件事件，收到有效事件后通过 {@link Platform#runLater} 调用刷新方法。
     */
    private void watchLoop() {
        while (running.get()) {
            WatchKey key;
            try {
                // 阻塞等待事件
                key = watchService.take();
            } catch (ClosedWatchServiceException e) {
                // WatchService 被关闭，退出循环
                break;
            } catch (InterruptedException e) {
                // 线程被中断，退出循环
                Thread.currentThread().interrupt();
                break;
            }
            Path dir = keyMap.get(key);
            if (dir == null) {
                key.reset();
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                // 忽略溢出事件
                if (kind == OVERFLOW) {
                    continue;
                }
                // 只要发生任何感兴趣的事件，就触发刷新
                Platform.runLater(this::refresh);
            }
            // 重置 WatchKey，若目录不可访问则从 map 中移除
            if (!key.reset()) {
                keyMap.remove(key);
                if (keyMap.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * 为单个目录注册监听事件。
     * 监听事件包括：创建、删除、修改（内容、大小、属性含隐藏属性）。
     *
     * @param dir 要注册监听的目录路径
     * @throws IOException 如果注册失败或目录不可访问
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY
        );
        keyMap.put(key, dir);
    }

    /**
     * 递归遍历目录树，为所有已存在的子目录注册监听。
     *
     * @param start 起始目录路径
     * @throws IOException 如果遍历或注册过程中发生 I/O 错误
     */
    private void registerAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 文件变更后执行的刷新操作。
     * <p>
     * 本方法保证在 JavaFX 线程中执行，具体实现应根据业务需求替换为实际的刷新逻辑。
     * 例如重新加载文件列表、更新 UI 组件等。
     * </p>
     */
    private void refresh() {
        if (onFileChanged != null) {
            onFileChanged.run();
        }
    }

}
