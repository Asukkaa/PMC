package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.app;

/**
 *
 * @author applesaucepenguin
 * Date 2026-01-30
 * time 16:13
 */
public class MacNativeWindowInfo extends Structure {

    /**
     * 进程 ID
     */
    public int pid;

    /**
     * 窗口 ID
     */
    public int windowId;

    /**
     * 窗口标题
     */
    public byte[] title = new byte[256];

    /**
     * 窗口横坐标
     */
    public int x;

    /**
     * 窗口纵坐标
     */
    public int y;

    /**
     * 窗口宽度
     */
    public int width;

    /**
     * 窗口高度
     */
    public int height;

    /**
     * 窗口层级
     */
    public int layer;

    /**
     * 进程名称
     */
    public byte[] processName = new byte[256];

    /**
     * 进程路径
     */
    public byte[] processPath = new byte[1024];

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "pid",
                "windowId",
                "title",
                "x",
                "y",
                "width",
                "height",
                "layer",
                "processName",
                "processPath"
        );
    }

    public static class ByValue extends MacNativeWindowInfo implements Structure.ByValue {

        @Override
        protected List<String> getFieldOrder() {
            return super.getFieldOrder();
        }

    }

    /**
     * 将 MacNativeWindowInfo 转换为 WindowInfo
     *
     * @return 转换后的窗口信息类
     */
    public WindowInfo toWindowInfo() {
        if (windowId == 0) {
            return null;
        }
        return new WindowInfo()
                .setProcessName(getProcessNameString())
                .setProcessPath(getProcessPathString())
                .setTitle(getTitleString())
                .setWindowId(windowId)
                .setHeight(height)
                .setWidth(width)
                .setLayer(layer)
                .setPid(pid)
                .setX(x)
                .setY(y);
    }

    /**
     * 从字符串获取标题（处理字节数组）
     */
    public String getTitleString() {
        return getNullTerminatedString(title);
    }

    /**
     * 获取进程名称字符串
     */
    public String getProcessNameString() {
        return getNullTerminatedString(processName);
    }

    /**
     * 获取进程路径字符串
     */
    public String getProcessPathString() {
        String path = getNullTerminatedString(processPath);
        if (path.contains(app)) {
            path = path.substring(0, path.indexOf(app) + app.length());
        }
        return path;
    }

    /**
     * 从字节数组获取以 null 结尾的字符串
     *
     * @param byteArray 需要处理的字节数组
     */
    private String getNullTerminatedString(byte[] byteArray) {
        int length = 0;
        while (length < byteArray.length && byteArray[length] != 0) {
            length++;
        }
        return new String(byteArray, 0, length);
    }

}
