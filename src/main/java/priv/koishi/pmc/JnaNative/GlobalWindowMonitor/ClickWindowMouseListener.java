package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import javafx.application.Platform;
import javafx.scene.control.Label;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;

import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.getFocusWindowInfo;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.getMainWindowInfo;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.updateMassageLabel;
import static priv.koishi.pmc.Utils.UiUtils.addToolTip;

/**
 * 记录焦点窗口鼠标点击监听器
 *
 * @author KOISHI
 * Date:2025-09-12
 * Time:12:38
 */
public class ClickWindowMouseListener implements NativeMouseListener {

    /**
     * 信息浮窗
     */
    FloatingWindowDescriptor massageFloating;

    /**
     * 是否正在处理事件（ture 正在获取窗口信息）
     */
    private boolean processingEvent;

    /**
     * 窗口信息
     */
    public static WindowInfo windowInfo;

    /**
     * 构造函数
     *
     * @param massageFloating 信息浮窗
     */
    public ClickWindowMouseListener(FloatingWindowDescriptor massageFloating) {
        this.massageFloating = massageFloating;
    }

    /**
     * 监听鼠标按下
     *
     * @param e 鼠标按下事件
     */
    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        if (processingEvent) {
            return;
        }
        if (massageFloating != null) {
            updateMassageLabel(massageFloating, "松开鼠标即可记录窗口信息");
        }
    }

    /**
     * 监听鼠标松开
     *
     * @param e 鼠标抬起事件
     */
    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        if (processingEvent) {
            return;
        }
        try {
            if (massageFloating != null) {
                processingEvent = true;
                updateMassageLabel(massageFloating, "正在记录窗口信息");
                windowInfo = getFocusWindowInfo();
                showWindowInfo();
                updateMassageLabel(massageFloating, "已记录窗口信息" + windowInfo.getProcessName());
            }
        } finally {
            processingEvent = false;
        }
    }

    /**
     * 显示窗口信息
     */
    public static void showWindowInfo() {
        System.out.println(windowInfo);
        Platform.runLater(() -> {
            Label label = autoClickController.windowInfo_Click;
            label.setText(windowInfo.getProcessName());
            String info = "进程名称：" + windowInfo.getProcessName() + "\n" +
                    "进程路径：" + windowInfo.getProcessPath() + "\n" +
                    "窗口标题：" + windowInfo.getTitle() + "\n" +
                    "窗口位置： X: " + windowInfo.getX() + " Y: " + windowInfo.getY() + "\n" +
                    "窗口大小： W: " + windowInfo.getWidth() + " H: " + windowInfo.getHeight();
            addToolTip(info, label);
        });
    }

    public static void updateWindowInfo() {
        if (windowInfo != null) {
            windowInfo = getMainWindowInfo(windowInfo.getProcessPath());
            showWindowInfo();
        }
    }

}
