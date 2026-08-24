package priv.koishi.pmc.Thumb;

import javafx.scene.image.Image;

/**
 * 支持缩略图加载的实体接口
 *
 * @author Koishi
 * Date: 2026-07-31
 */
public interface ThumbnailSupport {

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    String getPath();

    /**
     * 获取当前缩略图（可能为 null）
     *
     * @return 当前缩略图
     */
    Image getThumb();

    /**
     * 设置缩略图（由加载器回调，需在 UI 线程执行）
     *
     * @param thumb 缩略图
     */
    void setThumb(Image thumb);

    /**
     * 刷新表格中对应的行（需在 UI 线程执行）
     */
    void refreshTableRow();

}
