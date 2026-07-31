package priv.koishi.pmc.Thumb;

import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Utils.FileUtils.isImgFile;

/**
 * 缩略图异步加载公共服务
 *
 * @author Koishi
 * Date:2026-07-31
 * Time:13:36
 */
public class ThumbnailLoader {

    /**
     * 缩略图缓存
     */
    private static final ConcurrentHashMap<String, WeakReference<Image>> THUMB_CACHE = new ConcurrentHashMap<>();

    /**
     * 正在加载的任务缓存（防止同一路径重复解码）
     */
    private static final ConcurrentHashMap<String, CompletableFuture<Image>> LOADING_TASKS = new ConcurrentHashMap<>();

    /**
     * 虚拟线程执行器
     */
    private static final Executor VIRTUAL_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 异步加载缩略图
     *
     * @param support 实现缩略图支持接口的对象
     */
    public static void loadThumbnail(ThumbnailSupport support) {
        String path = support.getPath();
        if (path == null || path.isBlank()) {
            return;
        }
        // 如果已存在缩略图则跳过
        if (support.getThumb() != null) {
            return;
        }
        File file = new File(path);
        if (!isImgFile(file)) {
            return;
        }
        long lastModified = file.lastModified();
        String cacheKey = path + "_" + lastModified;
        // 1检查弱引用缓存
        WeakReference<Image> ref = THUMB_CACHE.get(cacheKey);
        Image cached = ref != null ? ref.get() : null;
        if (cached != null) {
            applyThumbnail(support, cached);
            return;
        }
        // 检查是否已有正在进行的加载任务（去重）
        CompletableFuture<Image> future = LOADING_TASKS.get(cacheKey);
        if (future == null) {
            boolean quickThumb = settingController.quickThumb_Set.isSelected();
            future = CompletableFuture.supplyAsync(() ->
                            new Image("file:" + path,
                                    100,
                                    100,
                                    true,
                                    true,
                                    quickThumb),
                    VIRTUAL_THREAD_EXECUTOR);
            CompletableFuture<Image> existing = LOADING_TASKS.putIfAbsent(cacheKey, future);
            if (existing != null) {
                future = existing;
            } else {
                // 任务完成/异常时清理 LOADING_TASKS 并缓存结果
                CompletableFuture<Image> finalFuture = future;
                future.thenAccept(img -> {
                    if (img != null) {
                        THUMB_CACHE.put(cacheKey, new WeakReference<>(img));
                    }
                    LOADING_TASKS.remove(cacheKey, finalFuture);
                }).exceptionally(_ -> {
                    LOADING_TASKS.remove(cacheKey, finalFuture);
                    return null;
                });
            }
        }
        // 注册回调，将结果应用到 support
        CompletableFuture<Image> finalFuture = future;
        finalFuture.thenAccept(img -> {
            if (img != null) {
                Platform.runLater(() -> applyThumbnail(support, img));
            }
        });
    }

    /**
     * 将缩略图应用到实体并刷新表格行
     */
    private static void applyThumbnail(ThumbnailSupport support, Image thumb) {
        if (thumb != null) {
            support.setThumb(thumb);
            support.refreshTableRow();
        }
    }

    /**
     * 清理所有缓存
     */
    public static void clearCache() {
        THUMB_CACHE.clear();
        LOADING_TASKS.clear();
    }

}
