package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

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

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "pid", "windowId", "title",
                "x", "y", "width", "height"
        );
    }

    public WindowInfo toWindowInfo() {
        WindowInfo info = new WindowInfo();
        info.setPid(pid);
        info.setId(windowId);
        info.setX(x);
        info.setY(y);
        info.setWidth(width);
        info.setHeight(height);
        // 转换字节数组为字符串
        String titleStr = new String(title).trim();
        info.setTitle(titleStr);
        // 其他字段保持默认或另行设置
        info.setLayer(0);  // macOS 可以使用层级信息，但需要额外获取
        info.setProcessName("");  // 需要额外获取
        info.setProcessPath("");  // 需要额外获取
        return info;
    }

    /**
     * 从字符串获取标题（处理字节数组）
     */
    public String getTitleString() {
        // 找到第一个空字符
        int length = 0;
        while (length < title.length && title[length] != 0) {
            length++;
        }
        return new String(title, 0, length);
    }

}
