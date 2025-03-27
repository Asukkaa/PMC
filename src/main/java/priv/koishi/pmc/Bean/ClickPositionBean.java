package priv.koishi.pmc.Bean;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Interface.UsedByReflection;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static priv.koishi.pmc.Utils.FileUtils.isImgFile;
import static priv.koishi.pmc.Utils.UiUtils.tableViewImageService;

/**
 * 自动操作步骤类
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:45
 */
@Data
@Accessors(chain = true)
public class ClickPositionBean {

    /**
     * 唯一标识符
     */
    String uuid = UUID.randomUUID().toString();

    /**
     * 操作名称
     */
    String name;

    /**
     * 起始横（X）坐标
     */
    String startX;

    /**
     * 起始纵（Y）坐标
     */
    String startY;

    /**
     * 结束横（X）坐标
     */
    String endX;

    /**
     * 结束纵（Y）坐标
     */
    String endY;

    /**
     * 点击时长（单位：毫秒）
     */
    String clickTime;

    /**
     * 操作次数
     */
    String clickNum;

    /**
     * 操作间隔时间（单位：毫秒）
     */
    String clickInterval;

    /**
     * 操作执行前等待时间（单位：毫秒）
     */
    String waitTime;

    /**
     * 操作类型
     */
    String type;

    /**
     * 要点击的图片路径
     */
    String clickImgPath;

    /**
     * 上次所选要点击的图片路径
     */
    String clickImgSelectPath;

    /**
     * 终止操作的图片
     */
    List<ImgFileBean> stopImgFileBeans;

    /**
     * 上次所选终止操作的图片路径
     */
    String stopImgSelectPath;

    /**
     * 要点击的图片识别匹配阈值
     */
    String clickMatchThreshold;

    /**
     * 终止操作的图片识别匹配阈值
     */
    String stopMatchThreshold;

    /**
     * 图像识别匹配最大时长（单位：秒）
     */
    String overtime;

    /**
     * 要点击的图片识别重试次数
     */
    String clickRetryTimes;

    /**
     * 终止操作的图片识别重试次数
     */
    String stopRetryTimes;

    /**
     * 详情页删除标志
     */
    boolean remove;

    /**
     * 缩略图
     */
    Image thumb;

    /**
     * 要点击的图像识别重试设置
     */
    String retryType;

    /**
     * 要显示缩略图的列表
     */
    TableView<ClickPositionBean> tableView;

    /**
     * 加载缩略图线程
     */
    private transient Service<Image> currentThumbService;

    /**
     * 匹配到图像后直接进行下一个操作步骤
     */
    boolean skip;

    /**
     * 获取缩略图
     *
     * @return 当前图片表格的缩略图
     */
    @UsedByReflection
    public Image getThumb() {
        if (thumb == null && StringUtils.isNotBlank(clickImgPath)) {
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
        if (isImgFile(new File(clickImgPath))) {
            // 终止进行中的服务
            if (currentThumbService != null && currentThumbService.isRunning()) {
                currentThumbService.cancel();
            }
            currentThumbService = tableViewImageService(clickImgPath);
            currentThumbService.setOnSucceeded(e -> {
                this.thumb = currentThumbService.getValue();
                Platform.runLater(() -> tableView.refresh());
            });
            currentThumbService.start();
        } else {
            this.thumb = null;
            Platform.runLater(() -> tableView.refresh());
        }
    }

    /**
     * 更新缩略图
     */
    public void updateThumb() {
        if (StringUtils.isNotBlank(clickImgPath)) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync();
        }
    }

}
