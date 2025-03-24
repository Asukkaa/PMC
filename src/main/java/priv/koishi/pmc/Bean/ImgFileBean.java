package priv.koishi.pmc.Bean;

import javafx.application.Platform;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import static priv.koishi.pmc.Finals.CommonFinals.THUMBNAIL_CACHE;

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

    public Image getThumb() {
        if (THUMBNAIL_CACHE.containsKey(path)) {
            return THUMBNAIL_CACHE.get(path);
        }
        if (thumb == null && StringUtils.isNotBlank(path)) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync();
        }
        return thumb;
    }

    private void loadThumbnailAsync() {
        new Thread(() -> {
            Image image = new Image("file:" + path, 50, 50, true, true, true);
            Platform.runLater(() -> {
                this.thumb = image;
                THUMBNAIL_CACHE.put(path, thumb);
            });
        }).start();
    }

}
