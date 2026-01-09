package priv.koishi.pmc.Utils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.pmc.Bean.TaskBean;

import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.disableCursor;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;

/**
 * 节点不可编辑工具类
 *
 * @author KOISHI
 * Date:2025-11-10
 * Time:15:08
 */
public class NodeDisableUtils {

    /**
     * 设置节点的编辑状态
     *
     * @param node    要设置的节点
     * @param disable 是否可编辑（true 不可编辑）
     */
    public static void setNodeDisable(Node node, boolean disable) {
        setNodeDisable(node, disable, null);
    }

    /**
     * 设置节点的编辑状态
     *
     * @param node        要设置的节点
     * @param disable     是否可编辑（true 不可编辑）
     * @param tooltipText 鼠标悬停提示文本
     */
    public static void setNodeDisable(Node node, boolean disable, String tooltipText) {
        if (node != null) {
            if (disable) {
                node.setOpacity(0.4);
                node.setCursor(null);
                // 不可编辑时使用默认光标
                node.setCursor(disableCursor);
                // 为所有子节点也设置相同的光标
                setCursorForChildren(node, disableCursor);
            } else {
                node.setOpacity(1.0);
                // 恢复可编辑时的默认光标
                node.setCursor(getDefaultCursor(node));
                setCursorForChildren(node, null);
            }
            if (tooltipText != null) {
                addToolTip(tooltipText, node);
            }
            // 管理事件拦截
            manageEventHandlers(node, disable);
            // 针对特定组件的特殊处理
            handleSpecificComponents(node, disable);
        }
    }

    /**
     * 为所有子节点设置光标
     *
     * @param node   需要修改样式的组件
     * @param cursor 鼠标光标
     */
    private static void setCursorForChildren(Node node, Cursor cursor) {
        if (node instanceof Parent) {
            String originalCursor = "originalCursor";
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                if (!child.getProperties().containsKey(originalCursor)) {
                    child.getProperties().put(originalCursor, child.getCursor());
                }
                child.setCursor(null);
                child.setCursor(cursor);
                setCursorForChildren(child, cursor);
            }
        }
    }

    /**
     * 管理事件拦截器
     *
     * @param node    需要修改样式的组件
     * @param disable 是否可编辑（true 不可编辑）
     */
    private static void manageEventHandlers(Node node, boolean disable) {
        // 移除所有可能存在的自定义事件处理器
        removeAllCustomEventHandlers(node);
        if (disable) {
            EventHandler<MouseEvent> mouseFilter = createMouseEventFilter();
            // 注册多种鼠标事件过滤器
            node.addEventFilter(MouseEvent.ANY, mouseFilter);
            node.getProperties().put("nonEditableMouseFilter", mouseFilter);
            // 阻止焦点获取
            node.setFocusTraversable(false);
            // 特殊处理：对于下拉框，添加额外的事件拦截
            if (node instanceof ComboBoxBase) {
                addComboBoxSpecialHandlers((ComboBoxBase<?>) node);
            }
        } else {
            // 恢复焦点获取能力
            node.setFocusTraversable(true);
            // 移除下拉框的特殊处理器
            if (node instanceof ComboBoxBase) {
                removeComboBoxSpecialHandlers((ComboBoxBase<?>) node);
            }
        }
    }

    /**
     * 为下拉框添加特殊的事件处理器
     *
     * @param comboBox 需要处理的下拉框
     */
    private static void addComboBoxSpecialHandlers(ComboBoxBase<?> comboBox) {
        // 保存原始的事件处理器（如果有）
        if (!comboBox.getProperties().containsKey("originalOnAction")) {
            comboBox.getProperties().put("originalOnAction", comboBox.getOnAction());
        }
        // 设置一个空的事件处理器来阻止默认行为
        comboBox.setOnAction(event -> {
            event.consume();
            // 如果下拉框已经展开，立即关闭它
            if (comboBox.isShowing()) {
                comboBox.hide();
            }
        });
        // 添加额外的鼠标事件过滤器，专门处理下拉框
        EventHandler<MouseEvent> comboBoxMouseFilter = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                event.consume();
                // 如果下拉框已经展开，立即关闭它
                if (comboBox.isShowing()) {
                    comboBox.hide();
                }
            }
        };
        comboBox.addEventFilter(MouseEvent.MOUSE_PRESSED, comboBoxMouseFilter);
        comboBox.getProperties().put("comboBoxMouseFilter", comboBoxMouseFilter);
    }

    /**
     * 移除下拉框的特殊处理器
     *
     * @param comboBox 需要处理的下拉框
     */
    @SuppressWarnings("unchecked")
    private static void removeComboBoxSpecialHandlers(ComboBoxBase<?> comboBox) {
        // 恢复原始的事件处理器
        EventHandler<?> originalOnAction = (EventHandler<?>) comboBox.getProperties().get("originalOnAction");
        comboBox.setOnAction((EventHandler<ActionEvent>) originalOnAction);
        comboBox.getProperties().remove("originalOnAction");
        // 移除鼠标事件过滤器
        EventHandler<MouseEvent> comboBoxMouseFilter =
                (EventHandler<MouseEvent>) comboBox.getProperties().get("comboBoxMouseFilter");
        if (comboBoxMouseFilter != null) {
            comboBox.removeEventFilter(MouseEvent.MOUSE_PRESSED, comboBoxMouseFilter);
            comboBox.getProperties().remove("comboBoxMouseFilter");
        }
    }

    /**
     * 创建鼠标事件过滤器
     */
    private static EventHandler<MouseEvent> createMouseEventFilter() {
        return event -> {
            // 阻止所有类型的鼠标交互
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED ||
                    event.getEventType() == MouseEvent.MOUSE_CLICKED ||
                    event.getEventType() == MouseEvent.MOUSE_RELEASED ||
                    event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                event.consume();
                // 对于某些组件，需要额外处理
                if (event.getTarget() instanceof TextInputControl &&
                        event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    // 阻止文本输入框获得焦点
                    Platform.runLater(() -> ((TextInputControl) event.getTarget()).deselect());
                }
                // 对于下拉框，确保它不会展开
                if (event.getTarget() instanceof ComboBoxBase<?> comboBox) {
                    if (comboBox.isShowing()) {
                        comboBox.hide();
                    }
                }
            }
        };
    }

    /**
     * 移除所有自定义事件处理器
     *
     * @param node 需要修改样式的组件
     */
    private static void removeAllCustomEventHandlers(Node node) {
        // 移除鼠标事件过滤器
        @SuppressWarnings("unchecked")
        EventHandler<MouseEvent> mouseFilter =
                (EventHandler<MouseEvent>) node.getProperties().get("nonEditableMouseFilter");
        if (mouseFilter != null) {
            node.removeEventFilter(MouseEvent.ANY, mouseFilter);
            node.getProperties().remove("nonEditableMouseFilter");
        }
        // 如果是下拉框，移除特殊处理器
        if (node instanceof ComboBoxBase) {
            removeComboBoxSpecialHandlers((ComboBoxBase<?>) node);
        }
    }

    /**
     * 针对特定组件的特殊处理
     *
     * @param node    需要修改样式的组件
     * @param disable 是否可编辑（true 不可编辑）
     */
    private static void handleSpecificComponents(Node node, boolean disable) {
        if (node instanceof TextInputControl textInput) {
            textInput.setEditable(!disable);
            if (disable) {
                Platform.runLater(textInput::deselect);
            }
        }
        if (node instanceof ComboBoxBase<?> combo) {
            combo.setEditable(!disable);
            if (disable && combo.isShowing()) {
                combo.hide();
            }
        }
    }

    /**
     * 获取组件的默认光标
     *
     * @param node 需要修改样式的组件
     * @return CSS 后缀
     */
    private static Cursor getDefaultCursor(Node node) {
        switch (node) {
            case CheckBox _, ButtonBase _, ChoiceBox<?> _ -> {
                return Cursor.HAND;
            }
            case TextInputControl _ -> {
                return Cursor.TEXT;
            }
            default -> {
                return Cursor.DEFAULT;
            }
        }
    }

    /**
     * 改变要防重复点击的组件状态（JavaFX 原生不可点击）
     *
     * @param taskBean 包含防重复点击组件列表的 taskBean
     * @param disable  可点击状态，true 设置为不可点击，false 设置为可点击
     */
    public static void changeNodesDisable(TaskBean<?> taskBean, boolean disable) {
        List<Node> disableNodes = taskBean.getDisableNodes();
        changeNodesDisable(disableNodes, disable);
    }

    /**
     * 改变要防重复点击的组件状态（JavaFX 原生不可点击）
     *
     * @param disableNodes 防重复点击组件列表
     * @param disable      可点击状态，true 设置为不可点击，false 设置为可点击
     */
    public static void changeNodesDisable(List<? extends Node> disableNodes, boolean disable) {
        if (CollectionUtils.isNotEmpty(disableNodes)) {
            disableNodes.forEach(dc -> {
                if (dc != null) {
                    dc.setDisable(disable);
                    if (!disable) {
                        setNodeDisable(dc, false);
                    }
                }
            });
        }
    }

    /**
     * 改变要防重复点击的组件状态（自定义不可点击）
     *
     * @param taskBean 包含防重复点击组件列表的 taskBean
     * @param disable  可点击状态，true 设置为不可点击，false 设置为可点击
     */
    public static void changeDisableNodes(TaskBean<?> taskBean, boolean disable) {
        List<Node> disableNodes = taskBean.getDisableNodes();
        changeDisableNodes(disableNodes, disable);
    }

    /**
     * 改变要防重复点击的组件状态（自定义不可点击）
     *
     * @param disableNodes 防重复点击组件列表
     * @param disable      可点击状态，true 设置为不可点击，false 设置为可点击
     */
    public static void changeDisableNodes(List<? extends Node> disableNodes, boolean disable) {
        if (CollectionUtils.isNotEmpty(disableNodes)) {
            disableNodes.forEach(dc -> {
                if (dc != null) {
                    setNodeDisable(dc, disable);
                    if (!disable) {
                        dc.setDisable(false);
                    }
                }
            });
        }
    }

}
