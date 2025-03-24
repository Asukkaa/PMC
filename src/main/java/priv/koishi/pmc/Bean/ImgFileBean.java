package priv.koishi.pmc.Bean;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.experimental.Accessors;
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
        if (thumb == null) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync(path);
        }
        return thumb;
    }

    /**
     * 异步加载并更新缩略图
     *
     * @param path 缩略图路径
     */
    private void loadThumbnailAsync(String path) {
        // 终止进行中的服务
        if (currentThumbService != null && currentThumbService.isRunning()) {
            currentThumbService.cancel();
        }
        currentThumbService = tableViewImageService(path);
        currentThumbService.setOnSucceeded(e -> {
            this.thumb = currentThumbService.getValue();
            Platform.runLater(() -> tableView.refresh());
        });
        currentThumbService.start();
    }

    /**
     * 更新缩略图
     */
    public void updateThumb() {
        // 异步加载缩略图（防止阻塞UI）
        loadThumbnailAsync(path);
    }

}
