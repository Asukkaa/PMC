package priv.koishi.pmc.EventBus;

import javafx.application.Platform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 事件总线
 *
 * @author KOISHI
 * Date:2025-06-04
 * Time:13:18
 */
public class EventBus {

    /**
     * 事件监听器
     */
    private static final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * 订阅事件
     *
     * @param eventType 事件类型
     * @param listener  事件监听器
     * @param <T>       事件类型
     */
    public static <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * 发布事件
     *
     * @param event 事件
     * @param <T>   事件类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void publish(T event) {
        List<Consumer<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            Platform.runLater(() -> {
                for (Consumer<?> listener : eventListeners) {
                    ((Consumer<T>) listener).accept(event);
                }
            });
        }
    }

}
