package priv.koishi.pmc.Listener;

import java.awt.*;

/**
 * 根据鼠标位置更新ui
 *
 * @author KOISHI
 * Date:2025-03-13
 * Time:15:45
 */
public interface MousePositionUpdater {

    /**
     * 根据鼠标位置调整ui接口
     *
     * @param mousePoint 鼠标位置
     */
    void onMousePositionUpdate(Point mousePoint);

}
