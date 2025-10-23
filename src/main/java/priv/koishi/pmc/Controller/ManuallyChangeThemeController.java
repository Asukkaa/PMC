package priv.koishi.pmc.Controller;

/**
 * 需要手动处理主题切换的控制器类
 *
 * @author KOISHI
 * Date:2025-10-23
 * Time:03:02
 */
public abstract class ManuallyChangeThemeController extends RootController {

    /**
     * 手动处理主题切换
     */
    abstract void manuallyChangeTheme();

}
