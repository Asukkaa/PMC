package priv.koishi.pmc.Bean.VO;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.FileBean;
import priv.koishi.pmc.Bean.Interface.ImgBean;
import priv.koishi.pmc.Bean.Interface.Indexable;

import java.io.File;

import static priv.koishi.pmc.Utils.FileUtils.isImgFile;

/**
 * JavaFX 列表展示文件信息类
 *
 * @author KOISHI
 * Date 2024-10-01
 * Time 下午1:50
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FileVO extends FileBean implements Indexable, ImgBean {

    /**
     * 序号
     */
    Integer index;

    /**
     * 缩略图
     */
    Image thumb;

    /**
     * 要显示缩略图的列表
     */
    TableView<FileVO> tableView;

    /**
     * 加载缩略图线程
     */
    private transient Thread currentThumbThread;

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
                // 终止进行中的线程
                if (currentThumbThread != null && currentThumbThread.isAlive()) {
                    currentThumbThread.interrupt();
                }
                // 创建新的虚拟线程
                currentThumbThread = Thread.ofVirtual()
                        .name("thumbnail-loader-")
                        .unstarted(() -> {
                            try {
                                if (StringUtils.isNotBlank(path)) {
                                    Image image = new Image("file:" + path,
                                            100,
                                            100,
                                            true,
                                            true,
                                            true);
                                    Platform.runLater(() -> {
                                        thumb = image;
                                        if (tableView != null) {
                                            tableView.refresh();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                if (!Thread.currentThread().isInterrupted()) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                currentThumbThread.start();
            } else {
                thumb = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清理资源
     */
    @Override
    public void clearResources() {
        if (currentThumbThread != null) {
            currentThumbThread.interrupt();
            currentThumbThread = null;
        }
        if (thumb != null) {
            thumb.cancel();
        }
        thumb = null;
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
