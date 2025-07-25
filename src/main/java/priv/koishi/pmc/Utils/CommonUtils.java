package priv.koishi.pmc.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * @throws IllegalArgumentException 如果源对象和目标对象类型不匹配，则抛出此异常
     * @throws IllegalAccessException   当字段访问权限不足时抛出
     */
    public static void copyProperties(Object source, Object target) throws IllegalAccessException {
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
     * 获取当前GC类型
     *
     * @return 当前GC类型
     */
    public static String getCurrentGCType() {
        List<String> gcNames = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(GarbageCollectorMXBean::getName).collect(Collectors.toList());
        if (gcNames.contains("G1 Young Generation") || gcNames.contains("G1 Old Generation")) {
            return "G1GC";
        } else if (gcNames.contains("PS Scavenge") || gcNames.contains("PS MarkSweep")) {
            return "ParallelGC";
        } else if (gcNames.contains("ZGC Cycles") || gcNames.contains("ZGC Pauses")) {
            return "ZGC";
        } else if (gcNames.contains("Shenandoah Pauses") || gcNames.contains("Shenandoah Cycles")) {
            return "ShenandoahGC";
        } else if (gcNames.contains("Copy") || gcNames.contains("MarkSweepCompact")) {
            return "SerialGC";
        } else {
            return "未知GC类型: " + String.join(", ", gcNames);
        }
    }

    /**
     * 获取当前进程PID
     *
     * @return 当前进程PID字符串
     * @throws Exception 获取PID时抛出的异常
     */
    public static String getProcessId() throws Exception {
        Class<?> processHandleClass = Class.forName("java.lang.ProcessHandle");
        Object currentProcessHandle = processHandleClass.getMethod("current").invoke(null);
        Object pid = processHandleClass.getMethod("pid").invoke(currentProcessHandle);
        return String.valueOf(pid);
    }

}
