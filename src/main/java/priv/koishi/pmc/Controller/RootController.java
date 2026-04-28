package priv.koishi.pmc.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import priv.koishi.pmc.Properties.CommonProperties;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根控制器
 *
 * @author KOISHI
 * Date:2025-05-28
 * Time:11:38
 */
public class RootController extends CommonProperties {

    /**
     * 全局控制器容器
     */
    public static final Map<Class<?>, WeakReference<RootController>> controllers = new ConcurrentHashMap<>();

    /**
     * 由加载器自动设置，存储当前页面的根节点
     */
    private Parent root;

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
        AutoClickController.isSonOpening = false;
        root = null;
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

    /**
     * 设置当前页面的根节点
     *
     * @param root 当前页面的根节点
     */
    void setRoot(Parent root) {
        this.root = root;
    }

    /**
     * 所有子控制器在 FXMLLoader 加载完成后会自动调用此方法（子类不要覆盖）
     * <p>默认行为：给根节点下所有可点击控件设置手型光标
     */
    protected void afterFxmlLoaded() {
        if (root != null) {
            setHandCursorRecursively(root);
        }
    }

    /**
     * 递归设置手型光标
     *
     * @param parent 需要处理的容器
     */
    public static void setHandCursorRecursively(Parent parent) {
        if (parent != null) {
            // 优先处理特殊容器（parent 本身）
            if (parent instanceof TabPane tabPane) {
                for (Tab tab : tabPane.getTabs()) {
                    Node content = tab.getContent();
                    if (content instanceof Parent) {
                        setHandCursorRecursively((Parent) content);
                    }
                }
                // 处理 ScrollPane（parent 本身）
            } else if (parent instanceof ScrollPane scrollPane) {
                Node content = scrollPane.getContent();
                if (content instanceof Parent) {
                    setHandCursorRecursively((Parent) content);
                }
            } else {
                // 遍历普通子节点
                for (Node node : parent.getChildrenUnmodifiable()) {
                    // 可点击控件直接设置光标
                    if (node instanceof ButtonBase
                            || node instanceof ComboBoxBase
                            || node instanceof ChoiceBox
                            || node instanceof Slider
                            || node instanceof MenuBar) {
                        node.setCursor(Cursor.HAND);
                        // 递归进入普通容器（不再需要特殊处理，因为 parent 入口已处理）
                    } else if (node instanceof Parent) {
                        setHandCursorRecursively((Parent) node);
                    }
                }
            }
        }
    }

    /**
     * 统一加载入口：加载 FXML，自动获取控制器并设置根节点，然后执行光标初始化
     *
     * @param loader Fxml 加载器
     * @throws IOException Fxml 文件加载失败
     */
    public static Parent loadFXML(FXMLLoader loader) throws IOException {
        Parent root = loader.load();
        Object rawController = loader.getController();
        if (rawController instanceof RootController controller) {
            controller.setRoot(root);
            controller.afterFxmlLoaded();
        }
        return root;
    }

}
