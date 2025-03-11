package priv.koishi.pmc.Utils;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EventListener;
import java.util.regex.Pattern;

/**
 * @author KOISHI
 * Date:2024-10-10
 * Time:下午1:14
 */
public class CommonUtils {


    /**
     * 正则表达式用于匹配指定范围的整数
     *
     * @param str 要校验的字符串
     * @param min 最小值，为空则不限制
     * @param max 最大值，为空则不限制
     * @return 在设置范围内为true，不在范围内为false
     */
    public static boolean isInIntegerRange(String str, Integer min, Integer max) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        // 禁止出现0开头的非0数字
        if (str.indexOf("0") == 0 && str.length() > 1) {
            return false;
        }
        Pattern integerPattern = Pattern.compile("^-?\\d{1,10}$");
        // 使用正则表达式判断字符串是否为整数
        if (!integerPattern.matcher(str).matches()) {
            return false;
        }
        // 将字符串转换为整数并判断是否在指定范围内
        int value = Integer.parseInt(str);
        // 只判断是否为整数，不限定范围
        if (max == null && min == null) {
            return true;
        }
        // 限定最小值
        if (max == null) {
            return value >= min;
        }
        // 限定最大值
        if (min == null) {
            return value <= max;
        }
        return value >= min && value <= max;
    }

    /**
     * 获取详细的异常信息
     *
     * @param e 要获取的异常
     * @return 详细异常详细
     */
    public static String errToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * 移除全局输入监听
     *
     * @param listener 要移除的监听器
     */
    public static void removeNativeListener(EventListener listener) {
        if (listener != null) {
            if (listener instanceof NativeMouseListener) {
                GlobalScreen.removeNativeMouseListener((NativeMouseListener) listener);
            } else if (listener instanceof NativeKeyListener) {
                GlobalScreen.removeNativeKeyListener((NativeKeyListener) listener);
            }
        }
    }

}
