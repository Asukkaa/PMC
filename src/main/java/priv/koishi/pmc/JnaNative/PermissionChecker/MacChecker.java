package priv.koishi.pmc.JnaNative.PermissionChecker;

import priv.koishi.pmc.JnaNative.NativeInterface.CoreGraphics;

import java.awt.*;

import static priv.koishi.pmc.Finals.CommonFinals.isMac;
import static priv.koishi.pmc.Service.ImageRecognitionService.checkScreenCapturePermission;

/**
 * macOS屏幕录制权限检查工具
 *
 * @author koishi
 * Date 2025/04/21
 * Time 14:30
 */
public class MacChecker {

    /**
     * 检查macOS屏幕录制权限方法
     */
    public static boolean hasScreenCapturePermission() {
        // 非macOS系统直接返回true
        if (!isMac) {
            return true;
        }
        try {
            // 尝试调用macOS原生API检查屏幕录制权限
            boolean hasAccess = CoreGraphics.INSTANCE.CGPreflightScreenCaptureAccess() == 1;
            // 没有权限时创建一个空的屏幕截图以便申请截屏权限
            if (!hasAccess) {
                new java.awt.Robot().createScreenCapture(new java.awt.Rectangle(0, 0, 1, 1));
            }
            return hasAccess;
            // 处理旧版本macOS（10.15以下）
        } catch (UnsatisfiedLinkError e) {
            // 检查是否能正常截图
            return checkScreenCapturePermission();
            // 截图失败认为没有权限
        } catch (AWTException e) {
            return false;
        }
    }

    /**
     * 校验 macOS 自动化权限
     *
     * @return true 拥有权限
     */
    public static boolean hasAutomationPermission() {
        // 非macOS系统直接返回true
        if (!isMac) {
            return true;
        }
        try {
            String script = "tell application \"System Events\" to get name of every process";
            Process process = Runtime.getRuntime().exec(new String[]{
                    "osascript", "-e", script
            });
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 申请 macOS 自动化权限
     */
    public static void getAutomationPermission() {
        if (isMac) {
            String script = "tell application \"System Events\" to get name of processes";
            try {
                Process process = Runtime.getRuntime().exec(new String[]{
                        "osascript", "-e", script
                });
                process.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
