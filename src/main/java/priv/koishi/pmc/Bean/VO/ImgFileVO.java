package priv.koishi.pmc.Bean.VO;

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
import priv.koishi.pmc.Thumb.ThumbnailSupport;

import static priv.koishi.pmc.Thumb.ThumbnailLoader.clearCache;
import static priv.koishi.pmc.Thumb.ThumbnailLoader.loadThumbnail;

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
public class ImgFileVO extends ImgFileBean implements Indexable, ImgBean, ThumbnailSupport {

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
     * 更新缩略图
     */
    public void updateThumb() {
        if (StringUtils.isNotBlank(getPath())) {
            // 异步加载缩略图
            loadThumbnail(this);
        }
    }

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
            // 异步加载缩略图
            loadThumbnail(this);
        }
        return thumb;
    }

    /**
     * 刷新表格中当前行（使用替换自身的方式，只影响一行）
     */
    @Override
    public void refreshTableRow() {
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
     * 设置缩略图
     *
     * @param thumb 缩略图
     */
    @Override
    public void setThumb(Image thumb) {
        this.thumb = thumb;
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
        // 清理所有缓存
        clearCache();
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
