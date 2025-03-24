package priv.koishi.pmc.Bean;

import javafx.concurrent.Service;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.experimental.Accessors;

import static priv.koishi.pmc.Finals.CommonFinals.THUMBNAIL_CACHE;
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
    TableView<?> tableView;

    /**
     * 获取缩略图
     *
     * @return 当前图片表格的缩略图
     */
    public Image getThumb() {
        if (THUMBNAIL_CACHE.containsKey(path)) {
            return THUMBNAIL_CACHE.get(path);
        }
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
            THUMBNAIL_CACHE.put(path, thumb);
            tableView.refresh();
        });
        service.start();
    }

    /**
     * 更新缩略图
     */
    public void updateThumb() {
        // 异步加载缩略图（防止阻塞UI）
        loadThumbnailAsync(path);
    }

}
