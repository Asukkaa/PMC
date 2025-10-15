package priv.koishi.pmc.JnaNative.GlobalWindowMonitor;

/**
 * 窗口信息处理器接口类
 *
 * @author KOISHI
 * Date:2025-10-15
 * Time:15:41
 */
public interface WindowInfoHandler {

    /**
     * 显示窗口信息接口
     *
     * @param windowInfo 窗口信息
     */
    void showInfo(WindowInfo windowInfo);

    /**
     * 删除窗口信息接口
     */
    void removeInfo();

}
