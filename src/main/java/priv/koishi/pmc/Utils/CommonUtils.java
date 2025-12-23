package priv.koishi.pmc.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static priv.koishi.pmc.Finals.i18nFinal.text_unknowGC;

/**
 * 通用工具类
 *
 * @author KOISHI
 * Date:2024-10-10
 * Time:下午1:14
 */
public class CommonUtils {

    /**
     * 自然排序比较器（数字按数值大小排序）
     */
    public static final Comparator<String> NATURAL_SORT = Comparator
            .comparing((String str) -> {
                if (str == null) return "";
                return str;
            }, CommonUtils::naturalCompare);

    /**
     * 自然排序的核心比较方法
     *
     * @param s1 要排序的字符串1
     * @param s2 要排序的字符串2
     */
    private static int naturalCompare(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        int i1 = 0, i2 = 0;
        int len1 = s1.length(), len2 = s2.length();
        while (i1 < len1 && i2 < len2) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                int num1 = 0, num2 = 0;
                while (i1 < len1 && Character.isDigit(s1.charAt(i1))) {
                    num1 = num1 * 10 + (s1.charAt(i1++) - '0');
                }
                while (i2 < len2 && Character.isDigit(s2.charAt(i2))) {
                    num2 = num2 * 10 + (s2.charAt(i2++) - '0');
                }
                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } else {
                // 不区分大小写比较
                int cmp = Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
                if (cmp != 0) return cmp;
                i1++;
                i2++;
            }
        }
        return Integer.compare(len1 - i1, len2 - i2);
    }

    /**
     * 正则表达式用于匹配指定范围的整数
     *
     * @param str 要校验的字符串
     * @param min 最小值，为空则不限制
     * @param max 最大值，为空则不限制
     * @return 在设置范围内为 true，不在范围内为 false
     */
    public static boolean isInIntegerRange(String str, Integer min, Integer max) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        // 禁止出现0开头的非0数字
        if (str.indexOf("0") == 0 && str.length() > 1) {
            return false;
        }
        // 禁止出现负数开头的0
        if (str.indexOf("-0") == 0) {
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
     * 正则表达式用于匹配指定范围的小数
     *
     * @param str 要校验的字符串
     * @param min 最小值，为空则不限制
     * @param max 最大值，为空则不限制
     * @return 在设置范围内为 true，不在范围内为 false
     */
    public static boolean isInDecimalRange(String str, Double min, Double max) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        // 处理负号情况
        boolean isNegative = str.startsWith("-");
        String absStr = isNegative ? str.substring(1) : str;
        // 禁止出现0开头的非0数字（整数部分）
        if (absStr.indexOf("0") == 0 && absStr.length() > 1 && absStr.charAt(1) != '.') {
            return false;
        }
        // 构建数字正则表达式（允许任意小数位数）
        String decimalPattern = "^-?\\d{1,10}(\\.\\d+)?$";
        Pattern pattern = Pattern.compile(decimalPattern);
        if (!pattern.matcher(str).matches()) {
            return false;
        }
        // 将字符串转换为小数并判断是否在指定范围内
        double value;
        try {
            value = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        // 只判断是否为有效数字，不限定范围
        if (max == null && min == null) {
            return !Double.isNaN(value) && Double.isFinite(value);
        }
        // 限定最小值
        if (max == null) {
            return value >= min && Double.isFinite(value);
        }
        // 限定最大值
        if (min == null) {
            return value <= max && Double.isFinite(value);
        }
        return value >= min && value <= max && Double.isFinite(value);
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
     * 自动复制同名属性（包含父类）
     *
     * @param source 源对象
     * @param target 目标对象
     * @throws IllegalAccessException 当字段访问权限不足时抛出
     */
    public static void copyAllProperties(Object source, Object target) throws IllegalAccessException {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        // 遍历源对象继承链
        for (Class<?> clazz = sourceClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field sourceField : clazz.getDeclaredFields()) {
                try {
                    // 遍历目标对象继承链查找同名字段
                    Field targetField = findFieldInHierarchy(targetClass, sourceField.getName());
                    copyFieldValue(source, target, sourceField, targetField);
                } catch (NoSuchFieldException e) {
                    // 忽略目标类不存在的字段
                }
            }
        }
    }

    /**
     * 在类继承链中递归查找指定字段
     *
     * @param targetClass 查找字段的起始目标类
     * @param fieldName   需要查找的字段名称
     * @return 查找到的字段对象
     * @throws NoSuchFieldException 若整个继承链中均未找到字段时抛出
     */
    private static Field findFieldInHierarchy(Class<?> targetClass, String fieldName) throws NoSuchFieldException {
        for (Class<?> clazz = targetClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // 继续向上查找父类
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * 复制字段值
     *
     * @param source      源对象，从中获取字段值
     * @param target      目标对象，将值设置到该对象
     * @param sourceField 源对象中需要复制的字段
     * @param targetField 目标对象中需要设置的字段
     * @throws IllegalAccessException 当字段访问权限不足时抛出
     */
    private static void copyFieldValue(Object source, Object target, Field sourceField, Field targetField) throws IllegalAccessException {
        sourceField.setAccessible(true);
        targetField.setAccessible(true);
        Object value = sourceField.get(source);
        if (value != null) {
            // 处理集合类型深拷贝
            if (value instanceof List) {
                targetField.set(target, new ArrayList<>((List<?>) value));
            } else {
                targetField.set(target, value);
            }
        }
    }

    /**
     * 获取当前 GC 类型
     *
     * @return 当前 GC 类型
     */
    public static String getCurrentGCType() {
        List<String> gcNames = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(GarbageCollectorMXBean::getName).collect(Collectors.toList());
        if (gcNames.contains("G1 Young Generation") || gcNames.contains("G1 Old Generation")) {
            return "G1GC";
        } else if (gcNames.contains("PS Scavenge") || gcNames.contains("PS MarkSweep")) {
            return "ParallelGC";
        } else if (gcNames.contains("ZGC Cycles") || gcNames.contains("ZGC Pauses") || gcNames.contains("ZGC Minor Cycles")) {
            return "ZGC";
        } else if (gcNames.contains("Shenandoah Pauses") || gcNames.contains("Shenandoah Cycles")) {
            return "ShenandoahGC";
        } else if (gcNames.contains("Copy") || gcNames.contains("MarkSweepCompact")) {
            return "SerialGC";
        } else {
            return text_unknowGC() + String.join(", ", gcNames);
        }
    }

    /**
     * 获取当前进程 PID
     *
     * @return 当前进程 PID 字符串
     * @throws Exception 获取 PID 时抛出的异常
     */
    public static String getProcessId() throws Exception {
        Class<?> processHandleClass = Class.forName("java.lang.ProcessHandle");
        Object currentProcessHandle = processHandleClass.getMethod("current").invoke(null);
        Object pid = processHandleClass.getMethod("pid").invoke(currentProcessHandle);
        return String.valueOf(pid);
    }

    /**
     * 验证 URL 是否有效
     *
     * @param url 要验证的 URL
     * @return true 表示 URL 有效，false 表示无效
     */
    public static boolean isValidUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        //  协议
        String urlRegex = "^(https?|ftp|file)://" +
                // 域名或 IP
                "([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|localhost|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})" +
                // 端口
                "(:[0-9]{1,5})?" +
                // 路径
                "(/[\\w.-]*)*" +
                // 查询参数
                "(\\?[\\w.-=&]*)?$";
        Pattern urlPattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        String trimmedUrl = url.trim();
        // 如果没有协议前缀，自动添加 https:// 进行测试
        if (!trimmedUrl.matches("^[a-zA-Z]+://.*")) {
            trimmedUrl = "https://" + trimmedUrl;
        }
        return urlPattern.matcher(trimmedUrl).matches();
    }

}
