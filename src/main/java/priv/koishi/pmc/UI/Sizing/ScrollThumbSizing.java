package priv.koishi.pmc.UI.Sizing;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;

import java.util.Optional;

/**
 * 滑动条尺寸调整
 *
 * @author Koishi
 * Date:2026-05-08
 * Time:15:03
 */
public class ScrollThumbSizing {

    /**
     * 启用滑动条尺寸调整
     *
     * @param control        要调整滑动条的组件
     * @param thumbMinHeight 最小高度
     */
    public static void enable(Control control, double thumbMinHeight) {
        ChangeListener<Skin<?>> changeListener = (_, _, skin) -> {
            if (skin != null) {
                searchScrollBar(control, thumbMinHeight);
            }
        };
        control.skinProperty().addListener(changeListener);
        if (control.getSkin() != null) {
            searchScrollBar(control, thumbMinHeight);
        }
    }

    /**
     * 寻找组件滑动条
     *
     * @param control        要调整滑动条的组件
     * @param thumbMinHeight 最小高度
     */
    private static void searchScrollBar(Control control, double thumbMinHeight) {
        Platform.runLater(() -> {
            Optional<ScrollBar> scrollBar = control.lookupAll(".scroll-bar").stream()
                    .map(node -> (ScrollBar) node)
                    .filter(sb -> sb.getOrientation() == Orientation.VERTICAL)
                    .findFirst();
            scrollBar.ifPresent(sb -> {
                StackPane thumb = (StackPane) sb.lookup(".thumb");
                if (thumb != null) {
                    ChangeListener<? super Number> listener = (_, _, _) ->
                            adjustThumb(sb, thumb, thumbMinHeight);
                    thumb.heightProperty().addListener(listener);
                    thumb.translateYProperty().addListener(listener);
                    adjustThumb(sb, thumb, thumbMinHeight);
                }
            });
        });
    }

    /**
     * 为滑动条设置最小高度监听
     *
     * @param scrollBar      滑动条
     * @param thumb          滑动条组件容器
     * @param thumbMinHeight 最小高度
     */
    private static void adjustThumb(ScrollBar scrollBar, StackPane thumb, double thumbMinHeight) {
        double thumbHeight = thumb.getHeight();
        final String ACTUAL_HEIGHT = "height";
        final String DISABLE_LISTENERS = "listener";
        // 缓存小于最小值的真实高度
        if (thumbHeight < thumbMinHeight) {
            scrollBar.getProperties().put(ACTUAL_HEIGHT, thumbHeight);
        } else if (thumbHeight > thumbMinHeight) {
            scrollBar.getProperties().remove(ACTUAL_HEIGHT);
        }
        // 当滑块高度小于最小值且监听器未禁用时，调整滑块尺寸
        if (scrollBar.getProperties().get(DISABLE_LISTENERS) == null && thumbHeight <= thumbMinHeight) {
            // 禁用监听器，防止死循环
            scrollBar.getProperties().put(DISABLE_LISTENERS, true);
            // 将滑块高度设置为最小值
            thumb.resize(scrollBar.getWidth(), thumbMinHeight);
            // 根据比例重新计算滑块位置
            Object thumbActualHgtObj = scrollBar.getProperties().get(ACTUAL_HEIGHT);
            double thumbActualHgt = thumbActualHgtObj == null ? 0 : (double) thumbActualHgtObj;
            double ty = thumb.getTranslateY();
            double sbHeight = scrollBar.getHeight();
            double tyMax = sbHeight - thumbActualHgt;
            double tH = sbHeight - thumbMinHeight;
            thumb.setTranslateY(ty / tyMax * tH);
            // 重新启用监听器
            scrollBar.getProperties().remove(DISABLE_LISTENERS);
        }
    }

}
