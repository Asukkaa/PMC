package priv.koishi.pmc.Bean.VO;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Annotate.UsedByReflection;
import priv.koishi.pmc.Bean.ClickPositionBean;

import java.io.File;
import java.io.IOException;

import static priv.koishi.pmc.Utils.FileUtils.isImgFile;
import static priv.koishi.pmc.Utils.UiUtils.tableViewImageService;

/**
 * 自动操作步骤展示类
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:45
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClickPositionVO extends ClickPositionBean implements Indexable {

    /**
     * 序号
     */
    Integer index;

    /**
     * 上次所选要点击的图片路径
     */
    String clickImgSelectPath;

    /**
     * 上次所选终止操作的图片路径
     */
    String stopImgSelectPath;

    /**
     * 详情页删除标志
     */
    boolean remove;

    /**
     * 缩略图
     */
    Image thumb;

    /**
     * 要显示缩略图的列表
     */
    TableView<ClickPositionVO> tableView;

    /**
     * 加载缩略图线程
     */
    private transient Service<Image> currentThumbService;

    /**
     * 获取缩略图
     *
     * @return 当前图片表格的缩略图
     */
    @UsedByReflection
    public Image loadThumb() {
        if (StringUtils.isBlank(getClickImgPath())) {
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
        // 文件不是图片时会实时刷新列表缩略图
        try {
            if (isImgFile(new File(getClickImgPath()))) {
                // 终止进行中的服务
                if (currentThumbService != null && currentThumbService.isRunning()) {
                    currentThumbService.cancel();
                }
                currentThumbService = tableViewImageService(getClickImgPath());
                currentThumbService.setOnSucceeded(e -> {
                    thumb = currentThumbService.getValue();
                    Platform.runLater(() -> tableView.refresh());
                });
                currentThumbService.start();
            } else {
                thumb = null;
                Platform.runLater(() -> tableView.refresh());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新缩略图
     */
    public void updateThumb() {
        if (StringUtils.isNotBlank(getClickImgPath())) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync();
        }
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
