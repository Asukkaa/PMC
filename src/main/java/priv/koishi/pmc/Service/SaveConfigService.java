package priv.koishi.pmc.Service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

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
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(SaveConfigService.class);

    /**
     * 保存应用配置任务
     *
     * @return 无返回值的 Task
     */
    public static Task<Void> saveAllConfig() {
        return new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> new MessageBubble("保存配置并关闭应用中", 0));
                // 保存关程序闭前页面状态设置
                if (mainController != null) {
                    try {
                        mainController.saveLastConfig();
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
                // 保存自动操作工具功能最后设置
                if (autoClickController != null) {
                    try {
                        autoClickController.saveLastConfig();
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
                // 保存设置功能最后设置
                if (settingController != null) {
                    try {
                        settingController.saveLastConfig();
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
                // 保存日志文件数量设置
                if (aboutController != null) {
                    try {
                        aboutController.saveLastConfig();
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
                // 保存批量执行 PMC 文件页面设置
                if (listPMCController != null) {
                    try {
                        listPMCController.saveLastConfig();
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
                return null;
            }
        };
    }

}
