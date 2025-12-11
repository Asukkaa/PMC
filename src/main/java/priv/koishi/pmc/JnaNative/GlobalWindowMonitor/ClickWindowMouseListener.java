package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;

import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.updateMassageLabel;

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
    private final FloatingWindowDescriptor massageFloating;

    /**
     * 是否正在处理事件（true 正在获取窗口信息）
     */
    private boolean processingEvent;

    /**
     * 关联的窗口监视器
     */
    private final WindowMonitor windowMonitor;

    /**
     * 构造函数
     *
     * @param massageFloating 信息浮窗
     * @param windowMonitor   关联的窗口监视器
     */
    public ClickWindowMouseListener(FloatingWindowDescriptor massageFloating, WindowMonitor windowMonitor) {
        this.massageFloating = massageFloating;
        this.windowMonitor = windowMonitor;
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
            updateMassageLabel(massageFloating, findImgSet_released());
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
                updateMassageLabel(massageFloating, findImgSet_finding());
                WindowInfo windowInfo = windowMonitor.getFocusWindowInfo();
                if (windowInfo != null) {
                    windowMonitor.setWindowInfo(windowInfo);
                    windowMonitor.showWindowInfo();
                    updateMassageLabel(massageFloating, findImgSet_getInfo() + windowInfo.getProcessName());
                } else {
                    updateMassageLabel(massageFloating, findImgSet_notFind());
                }
            }
        } finally {
            processingEvent = false;
        }
    }

}
