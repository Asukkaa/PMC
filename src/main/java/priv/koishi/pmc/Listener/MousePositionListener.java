package priv.koishi.pmc.Listener;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

/**
 * 鼠标位置监听器
 *
 * @author KOISHI
 * Date:2025-03-13
 * Time:15:43
 */
public class MousePositionListener {

    AnimationTimer timer;

    /**
     * 鼠标位置监听器
     *
     * @param mousePositionUpdater ui更新器
     */
    public MousePositionListener(MousePositionUpdater mousePositionUpdater) {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Platform.runLater(() -> {
                    if (mousePositionUpdater != null) {
                        mousePositionUpdater.onMousePositionUpdate();
                    }
                });
            }
        };
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

}
