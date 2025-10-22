package priv.koishi.pmc.Bean.VO;

import javafx.scene.image.Image;

/**
 * 带有图片的信息展示类接口
 *
 * @author KOISHI
 * Date:2025-10-23
 * Time:04:03
 */
public interface ImgBean {

    /**
     * 获取缩略图
     *
     * @return 当前图片表格的缩略图
     */
    Image loadThumb();

}
