package priv.koishi.pmc.Event;

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
     * 历史事件
     */
    private static final Map<Class<?>, Object> lastEvents = new ConcurrentHashMap<>();

    /**
     * 订阅事件
     *
     * @param eventType 事件类型
     * @param listener  事件监听器
     * @param <T>       事件类型
     */
    public static <T> void subscribe(Class<T> eventType, Consumer<? super T> listener) {
        listeners.computeIfAbsent(eventType, _ -> new CopyOnWriteArrayList<>()).add(listener);
        // 如果有历史事件，立即触发
        Object lastEvent = lastEvents.get(eventType);
        if (lastEvent != null) {
            Platform.runLater(() -> {
                @SuppressWarnings("unchecked")
                T event = (T) lastEvent;
                listener.accept(event);
            });
        }
    }

    /**
     * 发布事件
     *
     * @param event 事件
     * @param <T>   事件类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void publish(T event) {
        // 存储最后的事件
        lastEvents.put(event.getClass(), event);
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
