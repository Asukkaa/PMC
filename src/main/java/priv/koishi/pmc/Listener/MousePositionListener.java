package priv.koishi.pmc.Listener;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import lombok.Getter;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private final List<MousePositionUpdater> listeners = new CopyOnWriteArrayList<>();

    /**
     * 需要接收鼠标位置更新的监听器类的集合（用来防止重复添加）
     */
    private final List<Class<?>> classList = new CopyOnWriteArrayList<>();

    /**
     * 当前鼠标位置坐标
     */
    @Getter
    private static Point mousePoint;

    /**
     * 私有构造函数，初始化鼠标位置监听定时器
     * 创建AnimationTimer定时在JavaFX应用线程中更新鼠标位置
     * 通过MouseInfo获取系统级鼠标坐标，通知所有注册的监听器
     */
    private MousePositionListener() {
        AnimationTimer timer = new AnimationTimer() {
            /**
             * 定时器处理逻辑，将通知操作调度到JavaFX应用线程
             *
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
                mousePoint = MouseInfo.getPointerInfo().getLocation();
                for (MousePositionUpdater updater : listeners) {
                    updater.onMousePositionUpdate(mousePoint);
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
        if (!classList.contains(listener.getClass())) {
            listeners.add(listener);
            classList.add(listener.getClass());
        }
    }

    /**
     * 移除鼠标位置更新监听器
     *
     * @param listener 需要移除的监听器对象
     */
    public void removeListener(MousePositionUpdater listener) {
        listeners.remove(listener);
        classList.remove(listener.getClass());
    }

}
