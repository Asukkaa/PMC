package priv.koishi.pmc.Service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

import java.io.IOException;

import static priv.koishi.pmc.Controller.MainController.*;
import static priv.koishi.pmc.MainApplication.mainController;

/**
 * 配置文件保存服务类
 *
 * @author Koishi
 * Date:2026-07-02
 * Time:14:49
 */
public class SaveConfigService {

    /**
     * 保存应用配置任务
     *
     * @return 无返回值的 Task
     */
    public static Task<Void> saveAllConfig() {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
                Platform.runLater(() -> new MessageBubble("保存配置并关闭应用中", 0));
                // 保存自动操作工具功能最后设置
                if (autoClickController != null) {
                    autoClickController.saveLastConfig();
                }
                // 保存设置功能最后设置
                if (settingController != null) {
                    settingController.saveLastConfig();
                }
                // 保存日志文件数量设置
                if (aboutController != null) {
                    aboutController.saveLastConfig();
                }
                // 保存批量执行 PMC 文件页面设置
                if (listPMCController != null) {
                    listPMCController.saveLastConfig();
                }
                // 保存关程序闭前页面状态设置
                if (mainController != null) {
                    mainController.saveLastConfig();
                }
                return null;
            }
        };
    }

}
