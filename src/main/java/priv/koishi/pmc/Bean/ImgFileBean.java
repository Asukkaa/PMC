package priv.koishi.pmc.Bean;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Interface.UsedByReflection;

import static priv.koishi.pmc.Utils.UiUtils.tableViewImageService;

/**
 * 图片文件信息类
 *
 * @author KOISHI
 * Date:2025-03-21
 * Time:16:50
 */
@Data
@Accessors(chain = true)
public class ImgFileBean {

    /**
     * 文件名称
     */
    String name;

    /**
     * 文件地址
     */
    String path;

    /**
     * 文件类型
     */
    String type;

    /**
     * 缩略图
     */
    Image thumb;

    /**
     * 要显示缩略图的列表
     */
    TableView<ImgFileBean> tableView;

    private transient Service<Image> currentThumbService;

    /**
     * 获取缩略图
     *
     * @return 当前图片表格的缩略图
     */
    @UsedByReflection
    public Image getThumb() {
//        if (THUMBNAIL_CACHE.containsKey(path)) {
//            return THUMBNAIL_CACHE.get(path);
//        }
        if (thumb == null) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync(path);
        }
        return thumb;
    }

    /**
     * 异步加载并更新缩略图
     */
    private void loadThumbnailAsync(String path) {
        Service<Image> service = tableViewImageService(path);
        service.setOnSucceeded(e -> {
            this.thumb = service.getValue();
//            if (StringUtils.isNotBlank(path)) {
//                THUMBNAIL_CACHE.put(path, thumb);
//            }
            Platform.runLater(() -> tableView.refresh());
        });
        service.start();
    }

    /**
     * 更新缩略图
     */
    public void updateThumb() {
        // 终止进行中的服务
        if (currentThumbService != null && currentThumbService.isRunning()) {
            currentThumbService.cancel();
        }
        System.out.println(path);
        // 路径为空时的清理
        if (StringUtils.isBlank(path)) {
            Platform.runLater(() -> {
                // 清理旧缓存
//                THUMBNAIL_CACHE.entrySet().removeIf(entry ->
//                        entry.getValue().equals(this.thumb));
                this.thumb = null;
                // 精准刷新当前行
                int index = tableView.getItems().indexOf(this);
                if (index != -1) {
                    tableView.getItems().set(index, this);
                }
            });
            return;
        }
        // 路径有效时的加载
        currentThumbService = tableViewImageService(path);
        currentThumbService.setOnSucceeded(e -> {
            Image newImage = currentThumbService.getValue();
            System.out.println(newImage.getUrl());
            Platform.runLater(() -> {
                // 清理旧缓存
//                THUMBNAIL_CACHE.entrySet().removeIf(entry ->
//                        entry.getValue().equals(this.thumb));
                // 更新数据
                this.thumb = newImage;
//                THUMBNAIL_CACHE.put(path, newImage);
                // 精准刷新
                int index = tableView.getItems().indexOf(this);
                if (index != -1) {
                    tableView.getItems().set(index, this);
                }
            });
        });
        // 异常处理
        currentThumbService.setOnFailed(e ->
                Platform.runLater(() -> tableView.getItems().remove(this)));
        currentThumbService.start();
    }

}
