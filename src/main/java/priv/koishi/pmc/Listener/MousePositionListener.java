package priv.koishi.pmc.Listener;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 鼠标位置监听器
 *
 * @author KOISHI
 * Date:2025-03-13
 * Time:15:43
 */
public class MousePositionListener {

    /**
     * 需要接收鼠标位置更新的监听器对象
     */
    private static MousePositionListener instance;

    /**
     * 需要接收鼠标位置更新的监听器集合
     */
    private final Set<MousePositionUpdater> listeners = new HashSet<>();

    /**
     * 当前鼠标位置坐标
     */
    private Point currentMousePoint;

    /**
     * 私有构造函数，初始化鼠标位置监听定时器
     * 创建AnimationTimer定时在JavaFX应用线程中更新鼠标位置
     * 通过MouseInfo获取系统级鼠标坐标，通知所有注册的监听器
     */
    private MousePositionListener() {
        AnimationTimer timer = new AnimationTimer() {
            /**
             * 定时器处理逻辑，将通知操作调度到JavaFX应用线程
             * @param now 当前时间戳（纳秒）
             */
            @Override
            public void handle(long now) {
                Platform.runLater(this::notifyListeners);
            }

            /**
             * 通知所有注册的监听器最新鼠标坐标
             * 先获取系统级鼠标位置，然后遍历回调所有监听器
             */
            private void notifyListeners() {
                currentMousePoint = MouseInfo.getPointerInfo().getLocation();
                for (MousePositionUpdater updater : listeners) {
                    updater.onMousePositionUpdate(currentMousePoint);
                }
            }
        };
        timer.start();
    }

    /**
     * 获取单例实例（线程安全）
     *
     * @return 返回MousePositionListener的唯一实例
     */
    public static synchronized MousePositionListener getInstance() {
        if (instance == null) {
            instance = new MousePositionListener();
        }
        return instance;
    }

    /**
     * 注册鼠标位置更新监听器
     *
     * @param listener 需要接收鼠标位置更新的监听器对象
     */
    public void addListener(MousePositionUpdater listener) {
        listeners.add(listener);
    }

}
