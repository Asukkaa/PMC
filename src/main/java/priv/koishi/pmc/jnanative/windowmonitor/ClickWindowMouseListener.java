package priv.koishi.pmc.jnanative.windowmonitor;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import priv.koishi.pmc.ui.floatingwindow.FloatingWindowDescriptor;

import static priv.koishi.pmc.finals.i18nFinal.*;
import static priv.koishi.pmc.jnanative.windowmonitor.WindowMonitor.getFocusWindowInfo;
import static priv.koishi.pmc.ui.floatingwindow.FloatingWindow.updateMessageLabel;

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
    private final FloatingWindowDescriptor messageFloating;

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
     * @param messageFloating 信息浮窗
     * @param windowMonitor   关联的窗口监视器
     */
    public ClickWindowMouseListener(FloatingWindowDescriptor messageFloating, WindowMonitor windowMonitor) {
        this.messageFloating = messageFloating;
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
        if (messageFloating != null) {
            updateMessageLabel(messageFloating, findImgSet_released());
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
            if (messageFloating != null) {
                processingEvent = true;
                updateMessageLabel(messageFloating, findImgSet_finding());
                WindowInfo windowInfo = getFocusWindowInfo();
                if (windowInfo != null) {
                    windowMonitor.setWindowInfo(windowInfo);
                    windowMonitor.showWindowInfo();
                    updateMessageLabel(messageFloating, findImgSet_getInfo() + windowInfo.getProcessName());
                } else {
                    updateMessageLabel(messageFloating, findImgSet_notFind());
                }
            }
        } finally {
            processingEvent = false;
        }
    }

}
