package priv.koishi.pmc.Controller;

import priv.koishi.pmc.Properties.CommonProperties;

import java.lang.ref.WeakReference;

import static priv.koishi.pmc.MainApplication.controllers;

/**
 * 根控制器
 *
 * @author KOISHI
 * Date:2025-05-28
 * Time:11:38
 */
public class RootController extends CommonProperties {

    /**
     * 构造方法
     */
    public RootController() {
        controllers.put(getClass(), new WeakReference<>(this));
    }

    /**
     * 删除当前容器
     */
    public void removeController() {
        controllers.remove(getClass());
    }

    /**
     * 获取控制器实例
     *
     * @param type 要获取的控制器的类
     * @param <T>  控制器类型
     */
    public static <T extends RootController> T getController(Class<T> type) {
        synchronized (controllers) {
            WeakReference<RootController> ref = controllers.get(type);
            RootController instance = (ref != null) ? ref.get() : null;
            if (instance == null) {
                return null;
            }
            return type.cast(instance);
        }
    }

}
