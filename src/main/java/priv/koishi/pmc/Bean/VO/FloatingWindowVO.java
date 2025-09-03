package priv.koishi.pmc.Bean.VO;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;

import java.util.List;

/**
 * 浮窗配置展示类
 *
 * @author KOISHI
 * Date:2025-09-02
 * Time:18:39
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FloatingWindowVO extends FloatingWindowConfig {

    /**
     * 浮窗名称
     */
    String name;

    /**
     * 浮窗所在矩形
     */
    Rectangle rectangle;

    /**
     * 浮窗Stage
     */
    Stage stage;

    /**
     * 信窗信息展示栏
     */
    Label floatingLabel;

    /**
     * 浮窗坐标展示栏
     */
    Label floatingPosition;

    /**
     * 修改设置的按钮
     */
    Button button;

    /**
     * 要防重复点击的组件
     */
    List<Node> disableNodes;

    /**
     * 信窗信息展示栏文本
     */
    String floatingLabelText;

    /**
     * 显示浮窗按钮文本
     */
    String showButtonText;

    /**
     * 显示浮窗按钮鼠标悬浮提示
     */
    String showButtonToolTip;

    /**
     * 隐藏浮窗按钮文本
     */
    String hideButtonText;

    /**
     * 隐藏浮窗按钮鼠标悬浮提示
     */
    String hideButtonToolTip;

    /**
     * 是否允许拖拽移动（true 允许）
     */
    boolean enableDrag = true;

    /**
     * 是否允许调整大小（true 允许）
     */
    boolean enableResize = true;

    /**
     * 是否被修改过（true 修改过）
     */
    boolean modified;

    /**
     * 销毁浮窗
     */
    public void dispose() {
        if (stage != null) {
            // 移除事件监听器
            if (stage.getScene() != null) {
                Parent parent = stage.getScene().getRoot();
                parent.setOnMousePressed(null);
                parent.setOnMouseDragged(null);
                // 遍历移除所有子节点的监听器
                parent.getChildrenUnmodifiable().forEach(node -> {
                    node.setOnMousePressed(null);
                    node.setOnMouseDragged(null);
                });
            }
            stage.close();
            stage = null;
        }
    }

}
