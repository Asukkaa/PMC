package priv.koishi.pmc.Utils;

import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.PopupWindow;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import static priv.koishi.pmc.Finals.i18nFinal.text_nowValue;

/**
 * 鼠标停留提示框工具类
 *
 * @author KOISHI
 * Date:2025-11-10
 * Time:15:01
 */
public class ToolTipUtils {

    /**
     * 鼠标停留提示框
     *
     * @param nodes 需要显示提示框的组件
     * @param tip   提示卡信息
     */
    public static void addToolTip(String tip, Node... nodes) {
        for (Node node : nodes) {
            if (tip == null) {
                Tooltip.install(node, null);
            } else {
                Tooltip.install(node, creatTooltip(tip));
            }
        }
    }

    /**
     * 鼠标停留提示框（组件文字为提示内容）
     *
     * @param labels 需要显示提示框的组件
     */
    public static void addToolTip(Labeled... labels) {
        for (Labeled labeled : labels) {
            String tip = labeled.getText();
            Tooltip.install(labeled, creatTooltip(tip));
        }
    }

    /**
     * 设置永久显示的鼠标停留提示框参数
     *
     * @param tip 提示文案
     * @return 设置参数后的 Tooltip 对象
     */
    public static Tooltip creatTooltip(String tip) {
        return creatTooltip(tip, Duration.INDEFINITE);
    }

    /**
     * 设置鼠标停留提示框参数
     *
     * @param tip      提示文案
     * @param duration 显示时长
     * @return 设置参数后的 Tooltip 对象
     */
    public static Tooltip creatTooltip(String tip, Duration duration) {
        Tooltip tooltip = new Tooltip(tip);
        tooltip.setWrapText(true);
        tooltip.setShowDuration(duration);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        tooltip.getStyleClass().add("tooltip-font-size");
        tooltip.setMaxWidth(500);
        return tooltip;
    }

    /**
     * 文本输入框鼠标停留提示输入值
     *
     * @param textField 要添加提示的文本输入框
     * @param text      要展示的提示文案
     */
    public static void addValueToolTip(TextField textField, String text) {
        addValueToolTip(textField, text, text_nowValue());
    }

    /**
     * 文本输入框鼠标停留提示输入值
     *
     * @param textField 要添加提示的文本输入框
     * @param text      要展示的提示文案
     * @param valueText 当前所填值提示文案
     */
    public static void addValueToolTip(TextField textField, String text, String valueText) {
        String value = textField.getText();
        addValueToolTip(textField, text, valueText, value);
    }

    /**
     * 为组件添加鼠标悬停提示框
     *
     * @param node  要添加提示的组件
     * @param text  提示文案
     * @param value 当前所填值
     */
    public static void addValueToolTip(Node node, String text, String value) {
        addValueToolTip(node, text, text_nowValue(), value);
    }

    /**
     * 为组件添加鼠标悬停提示框
     *
     * @param node      要添加提示的组件
     * @param text      提示文案
     * @param valueText 当前所填值提示文案
     * @param value     当前所填值
     */
    public static void addValueToolTip(Node node, String text, String valueText, String value) {
        if (StringUtils.isNotEmpty(text)) {
            if (StringUtils.isNotEmpty(value)) {
                addToolTip(text + "\n" + valueText + value, node);
            } else {
                addToolTip(text, node);
            }
        } else {
            if (StringUtils.isNotEmpty(value)) {
                addToolTip(value, node);
            } else {
                addToolTip(null, node);
            }
        }
    }

}
