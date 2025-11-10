package priv.koishi.pmc.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

import static priv.koishi.pmc.Utils.UiUtils.manuallyChangeThemePane;

/**
 * 赞赏页面控制器
 *
 * @author KOISHI
 * Date:2025-11-10
 * Time:21:17
 */
public class AppreciateController extends ManuallyChangeThemeController {

    @FXML
    public ScrollPane scrollPane_Ap;

    /**
     * 手动处理主题切换
     */
    @Override
    void manuallyChangeTheme() {
        manuallyChangeThemePane(scrollPane_Ap, getClass());
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        manuallyChangeTheme();
    }

}
