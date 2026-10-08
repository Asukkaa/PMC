package priv.koishi.pmc.controller;

/**
 * 需要手动处理主题切换的控制器类
 * <p>
 * 未开启拓展标题栏可能会导致子页面无法正常切换主题，需要手动切换
 *
 * @author KOISHI
 * Date:2025-10-23
 * Time:03:02
 */
public abstract class ChangeThemeController extends RootController {

    /**
     * 手动处理主题切换
     */
    abstract void manuallyChangeTheme();

}
