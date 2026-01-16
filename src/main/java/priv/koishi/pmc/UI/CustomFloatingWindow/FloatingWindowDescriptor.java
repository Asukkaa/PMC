package priv.koishi.pmc.UI.CustomFloatingWindow;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;

import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.defaultFloatingHeightInt;
import static priv.koishi.pmc.Finals.CommonFinals.defaultFloatingWidthInt;
import static priv.koishi.pmc.Service.ImageRecognitionService.screenHeight;
import static priv.koishi.pmc.Service.ImageRecognitionService.screenWidth;

/**
 * 浮窗属性类
 *
 * @author KOISHI
 * Date:2025-09-02
 * Time:18:39
 */
@Data
@Accessors(chain = true)
public class FloatingWindowDescriptor {

    /**
     * 浮窗基础属性配置
     */
    FloatingWindowConfig config;

    /**
     * 浮窗名称
     */
    String name;

    /**
     * 浮窗所在矩形
     */
    Rectangle rectangle;

    /**
     * 浮窗 Stage
     */
    Stage stage;

    /**
     * 浮窗是否只隐藏（true 只隐藏，默认直接关闭而不是隐藏）
     */
    boolean onlyHide;

    /**
     * 信窗信息展示栏
     */
    Label massageLabel;

    /**
     * 浮窗坐标展示栏
     */
    Label floatingPosition;

    /**
     * 浮窗名称展示栏
     */
    Label nameeLabel;

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
    String massage;

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
     * 字体对齐方式（默认左对齐）
     */
    TextAlignment textAlignment = TextAlignment.LEFT;

    /**
     * 浮窗组件对齐方式（默认左上角）
     */
    Pos pos = Pos.TOP_LEFT;

    /**
     * 是否允许拖拽移动（true 允许，默认允许）
     */
    boolean enableDrag = true;

    /**
     * 是否允许调整大小（true 允许，默认允许）
     */
    boolean enableResize = true;

    /**
     * 是否被修改过（true 修改过）
     */
    boolean modified;

    /**
     * 是否显示名称（true 显示，默认显示）
     */
    boolean showName = true;

    /**
     * 浮窗文本颜色（默认百色）
     */
    Color textFill = Color.WHITE;

    /**
     * 浮窗离屏幕边界距离
     */
    int margin;

    /**
     * 浮窗文本大小（默认 18）
     */
    int fontSize = 18;

    /**
     * 浮窗透明度（默认 0.5）
     */
    double opacity = 0.5;

    /**
     * 浮窗背景颜色
     */
    Color backgroundColor;

    /**
     * 浮窗是否可透明（true 透明）
     */
    boolean transparent;

    /**
     * 保存设置时浮窗横坐标 key
     */
    String xKey;

    /**
     * 保存设置时浮窗纵坐标 key
     */
    String yKey;

    /**
     * 浮窗保存设置时宽度 key
     */
    String widthKey;

    /**
     * 浮窗保存设置时高度 key
     */
    String heightKey;

    /**
     * 浮窗保存设置时配置文件路径
     */
    String configFile;

    /**
     * 关闭时保存浮窗位置（true 保存）
     */
    boolean closeSave = true;

    /**
     * 浮窗最小宽度（默认 {@value priv.koishi.pmc.Finals.CommonFinals#defaultFloatingWidth}）
     */
    int minWidth = defaultFloatingWidthInt;

    /**
     * 浮窗最小高度（默认 {@value priv.koishi.pmc.Finals.CommonFinals#defaultFloatingHeight}）
     */
    int minHeight = defaultFloatingHeightInt;

    /**
     * 浮窗最大宽度（默认屏幕宽度）
     */
    int maxWidth = screenWidth;

    /**
     * 浮窗最大高度（默认屏幕高度）
     */
    int maxHeight = screenHeight;

    /**
     * 浮窗最小横轴坐标
     */
    int minX;

    /**
     * 浮窗最小纵轴坐标
     */
    int minY;

    /**
     * 浮窗最大横轴坐标
     */
    int maxX;

    /**
     * 浮窗最大纵轴坐标
     */
    int maxY;

    /**
     * 浮窗是否添加关闭快捷键键监听（true 添加，默认添加）
     */
    boolean addCloseKey = true;

    /**
     * 是否格式化文本容器长度（true 格式化，默认不格式化）
     */
    boolean formattingText;

    /**
     * 是否显示窗口相对位置信息（true 显示，默认不显示）
     */
    boolean showRelativeInfo;

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
            FloatingWindow.floatingWindows.remove(this);
            stage.close();
            stage = null;
        }
    }

}
