package priv.koishi.pmc.Bean.VO;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Annotation.IndexColumn;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.Interface.ImgBean;
import priv.koishi.pmc.Bean.Interface.Indexable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Utils.FileUtils.isImgFile;

/**
 * 图片文件信息展示类
 *
 * @author KOISHI
 * Date:2025-03-21
 * Time:16:50
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ImgFileVO extends ImgFileBean implements Indexable, ImgBean {

    /**
     * 序号
     */
    @IndexColumn
    private Integer index;

    /**
     * 缩略图
     */
    private Image thumb;

    /**
     * 要显示缩略图的列表
     */
    private TableView<ImgFileVO> tableView;

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
     * 获取缩略图
     *
     * @return 当前图片表格的缩略图
     */
    @Override
    public Image loadThumb() {
        if (StringUtils.isBlank(getPath())) {
            return null;
        }
        if (thumb == null) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync();
        }
        return thumb;
    }

    /**
     * 异步加载并更新缩略图
     */
    private void loadThumbnailAsync() {
        try {
            String path = getPath();
            if (isImgFile(new File(path))) {
                File file = new File(path);
                long lastModified = file.lastModified();
                // 构建带时间戳的缓存键
                String cacheKey = path + "_" + lastModified;
                // 尝试从弱引用缓存获取
                WeakReference<Image> ref = THUMB_CACHE.get(cacheKey);
                Image cached = ref != null ? ref.get() : null;
                if (cached != null) {
                    setThumbAndRefresh(cached);
                    return;
                }
                // 检查是否已有正在进行的加载任务（去重）
                CompletableFuture<Image> future = LOADING_TASKS.get(cacheKey);
                if (future == null) {
                    // 快速加载缩略图开关
                    boolean quickThumb = settingController.quickThumb_Set.isSelected();
                    // 使用虚拟线程执行器
                    future = CompletableFuture.supplyAsync(() ->
                                    new Image("file:" + path,
                                            100, 100,
                                            true,
                                            true,
                                            quickThumb),
                            VIRTUAL_THREAD_EXECUTOR);
                    // 原子性地放入全局映射
                    CompletableFuture<Image> existing = LOADING_TASKS.putIfAbsent(cacheKey, future);
                    if (existing != null) {
                        // 其他线程已创建，复用
                        future = existing;
                    } else {
                        // 注册清理回调（任务完成或异常时从全局映射移除）
                        CompletableFuture<Image> finalFuture1 = future;
                        CompletableFuture<Image> finalFuture2 = future;
                        future.thenAccept(img -> {
                            if (img != null) {
                                THUMB_CACHE.put(cacheKey, new WeakReference<>(img));
                            }
                            LOADING_TASKS.remove(cacheKey, finalFuture1);
                        }).exceptionally(_ -> {
                            LOADING_TASKS.remove(cacheKey, finalFuture2);
                            return null;
                        });
                    }
                }
                // 取消当前 Bean 上旧的加载任务（防止内存泄漏和重复回调）
                CompletableFuture<Image> finalFuture = future;
                finalFuture.thenAccept(img -> {
                    if (img != null) {
                        Platform.runLater(() -> setThumbAndRefresh(img));
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新当前 Bean 的缩略图并刷新表格
     */
    private void setThumbAndRefresh(Image img) {
        if (img == null) {
            return;
        }
        thumb = img;
        refreshTableRow();
    }

    /**
     * 刷新表格中当前行（使用替换自身的方式，只影响一行）
     */
    private void refreshTableRow() {
        if (tableView != null) {
            try {
                ObservableList<ImgFileVO> items = tableView.getItems();
                int idx = items.indexOf(this);
                if (idx >= 0) {
                    // 通过 set 操作触发该行单元格重绘，性能远优于全表 refresh()
                    items.set(idx, this);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 更新缩略图
     */
    public void updateThumb() {
        if (StringUtils.isNotBlank(getPath())) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync();
        }
    }

    /**
     * 清理资源
     */
    @Override
    public void clearResources() {
        if (thumb != null) {
            thumb = null;
        }
        tableView = null;
        THUMB_CACHE.clear();
        LOADING_TASKS.clear();
    }

    /**
     * 解除表格引用
     */
    @Override
    public void unbindTableView() {
        tableView = null;
    }

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
