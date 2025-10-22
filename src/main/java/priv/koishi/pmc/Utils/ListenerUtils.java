package priv.koishi.pmc.Utils;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

import java.lang.ref.WeakReference;
import java.util.EventListener;
import java.util.Map;

import static priv.koishi.pmc.Finals.i18nFinal.text_errRange;
import static priv.koishi.pmc.Finals.i18nFinal.text_unknownListener;
import static priv.koishi.pmc.Utils.CommonUtils.isInDecimalRange;
import static priv.koishi.pmc.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.pmc.Utils.UiUtils.addValueToolTip;

/**
 * 监听器工具类
 *
 * @author KOISHI
 * Date:2025-06-18
 * Time:16:24
 */
public class ListenerUtils {

    /**
     * 限制滑动条只能输入整数
     *
     * @param slider 要处理的滑动条
     * @param tip    鼠标悬停提示文案
     * @return 监听器删除函数
     */
    public static Runnable integerSliderValueListener(Slider slider, String tip) {
        ChangeListener<Number> listener = (_, _, newValue) -> {
            int rounded = newValue.intValue();
            slider.setValue(rounded);
            addValueToolTip(slider, tip, String.valueOf(rounded));
        };
        slider.valueProperty().addListener(listener);
        return () -> slider.valueProperty().removeListener(listener);
    }

    /**
     * 限制输入框只能输入指定范围内的整数
     *
     * @param textField 要处理的文本输入框
     * @param min       可输入的最小值，为空则不限制
     * @param max       可输入的最大值，为空则不限制
     * @param tip       鼠标悬停提示文案
     * @return 监听器删除函数
     */
    public static Runnable integerRangeTextField(TextField textField, Integer min, Integer max, String tip) {
        ChangeListener<Boolean> focusedListener = (_, oldFocused, newFocused) -> {
            if (oldFocused && !newFocused) {
                String newValue = textField.getText();
                if (!isInIntegerRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                    textField.setText("");
                    new MessageBubble(text_errRange(), 1);
                }
                addValueToolTip(textField, tip);
            }
        };
        textField.focusedProperty().addListener(focusedListener);
        Runnable runnable = textFieldValueListener(textField, tip);
        return () -> {
            textField.focusedProperty().removeListener(focusedListener);
            runnable.run();
        };
    }


    /**
     * 限制输入框只能输入指定范围内的小数
     *
     * @param textField     要处理的文本输入框
     * @param min           可输入的最小值，为空则不限制
     * @param max           可输入的最大值，为空则不限制
     * @param decimalDigits 小数位数，0表示整数
     * @param tip           鼠标悬停提示文案
     * @return 监听器删除函数
     */
    public static Runnable DoubleRangeTextField(TextField textField, Double min, Double max,
                                                int decimalDigits, String tip) {
        ChangeListener<Boolean> focusedListener = (_, oldFocused, newFocused) -> {
            if (oldFocused && !newFocused) {
                String newValue = textField.getText();
                if (!isInDecimalRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                    textField.setText("");
                    new MessageBubble(text_errRange(), 1);
                } else if (newValue.contains(".")) {
                    textField.setText(newValue.substring(0, newValue.lastIndexOf(".") + decimalDigits + 1));
                }
                addValueToolTip(textField, tip);
            }
        };
        textField.focusedProperty().addListener(focusedListener);
        Runnable runnable = textFieldValueListener(textField, tip);
        return () -> {
            textField.focusedProperty().removeListener(focusedListener);
            runnable.run();
        };
    }

    /**
     * 限制输入框只能输入指定范围内的正整数（范围外显示警告信息）
     *
     * @param textField   要处理的文本输入框
     * @param min         可输入的最小值
     * @param max         可输入的最大值，为空则不限制
     * @param tip         鼠标悬停提示文案
     * @param warningNode 警告节点
     * @return 监听器删除函数
     */
    public static Runnable warnIntegerRangeTextField(TextField textField, Integer min, Integer max, String tip, Node warningNode) {
        ChangeListener<String> listener = (_, oldValue, newValue) -> {
            // 这里处理文本变化的逻辑
            if (!isInIntegerRange(newValue, 1, null) && StringUtils.isNotBlank(newValue)) {
                textField.setText(oldValue);
            } else if (!isInIntegerRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                textField.setStyle("-fx-text-fill: red;");
                warningNode.setVisible(true);
            } else {
                textField.setStyle("-fx-text-fill: black;");
                warningNode.setVisible(false);
            }
            addValueToolTip(textField, tip);
        };
        textField.textProperty().addListener(listener);
        return () -> textField.textProperty().removeListener(listener);
    }

    /**
     * 监听输入框内容变化
     *
     * @param textField 要监听的文本输入框
     * @param tip       鼠标悬停提示文案
     * @return 监听器删除函数
     */
    public static Runnable textFieldValueListener(TextField textField, String tip) {
        ChangeListener<String> listener = (_, _, _) ->
                addValueToolTip(textField, tip);
        textField.textProperty().addListener(listener);
        return () -> textField.textProperty().removeListener(listener);
    }

    /**
     * 注册一个弱引用的属性变更监听器，避免因监听器未被释放而导致的内存泄漏。
     *
     * @param key                 用于标识监听器的唯一键值，通常使用持有监听器的对象作为键
     * @param property            需要监听的属性对象，监听器将绑定到该属性上
     * @param listener            原始的属性变更监听器，将被包装为弱引用版本
     * @param weakChangeListeners 存储弱引用监听器的映射表，用于维护监听器的弱引用关系
     * @param <T>                 属性值的泛型类型
     */
    public static <T> void registerWeakListener(Object key, Property<T> property, ChangeListener<? super T> listener,
                                                Map<Object, ? super WeakReference<ChangeListener<?>>> weakChangeListeners) {
        // 将原始监听器包装为弱引用版本，确保不影响垃圾回收
        ChangeListener<? super T> weakListener = new WeakChangeListener<>(listener);
        property.addListener(weakListener);
        // 存储弱引用监听器以便后续管理
        weakChangeListeners.put(key, new WeakReference<>(weakListener));
    }

    /**
     * 注册一个弱引用的失效事件监听器，防止因监听器持有导致的对象无法回收（处理滑块用）
     *
     * @param key                       用于标识监听器的唯一键值，建议使用监听器持有者对象
     * @param observable                被监听的可观察值对象
     * @param listener                  原始的失效事件监听器，将被弱引用包装
     * @param weakInvalidationListeners 维护弱引用监听器的存储映射表
     */
    public static void registerWeakInvalidationListener(Object key, ObservableValue<?> observable, InvalidationListener listener,
                                                        Map<Object, ? super WeakReference<InvalidationListener>> weakInvalidationListeners) {
        // 创建弱引用包装器，解除对原始监听器的强引用
        InvalidationListener weakListener = new WeakInvalidationListener(listener);
        observable.addListener(weakListener);
        // 记录弱引用监听器用于后续清理操作
        weakInvalidationListeners.put(key, new WeakReference<>(weakListener));
    }

    /**
     * 移除修改内容变化标志监听器
     *
     * @param weakInvalidationListeners 监听器集合
     */
    public static void removeInvalidationListeners(Map<Object, ? extends WeakReference<InvalidationListener>> weakInvalidationListeners) {
        // 处理失效监听器集合，遍历所有entry，根据不同类型移除对应的属性监听器
        weakInvalidationListeners.entrySet().removeIf(entry -> {
            Object key = entry.getKey();
            WeakReference<InvalidationListener> ref = entry.getValue();
            InvalidationListener listener = ref.get();
            if (key instanceof ImgFileVO imgFileVO) {
                if (listener != null) {
                    imgFileVO.pathProperty().removeListener(listener);
                }
                return true;
            } else if (key instanceof TextInputControl textInput) {
                if (listener != null) {
                    textInput.textProperty().removeListener(listener);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * 移除修改内容变化标志监听器（滑块组件专用）
     *
     * @param weakChangeListeners 监听器集合
     */
    @SuppressWarnings("unchecked")
    public static void removeWeakReferenceChangeListener(Map<Object, ? extends WeakReference<ChangeListener<?>>> weakChangeListeners) {
        // 处理变更监听器集合，遍历所有entry，根据不同类型移除对应的选择/数值监听器
        weakChangeListeners.forEach((key, ref) -> {
            ChangeListener<?> listener = ref.get();
            if (listener == null) {
                return;
            }
            if (key instanceof ChoiceBox<?> choiceBox) {
                choiceBox.getSelectionModel().selectedItemProperty().removeListener((InvalidationListener) listener);
            } else if (key instanceof Slider slider) {
                slider.valueProperty().removeListener((ChangeListener<? super Number>) listener);
            }
        });
    }

    /**
     * 移除全局输入监听
     *
     * @param listener 要移除的监听器
     * @throws IllegalArgumentException 如果监听器类型不匹配，则抛出此异常
     */
    public static void removeNativeListener(EventListener listener) {
        if (listener != null) {
            switch (listener) {
                case NativeMouseListener nativeMouseListener ->
                        GlobalScreen.removeNativeMouseListener(nativeMouseListener);
                case NativeMouseMotionListener nativeMouseMotionListener ->
                        GlobalScreen.removeNativeMouseMotionListener(nativeMouseMotionListener);
                case NativeKeyListener nativeKeyListener -> GlobalScreen.removeNativeKeyListener(nativeKeyListener);
                default -> throw new IllegalArgumentException(text_unknownListener());
            }
        }
    }

    /**
     * 添加全局输入监听
     *
     * @param listener 要添加的监听器
     * @throws IllegalArgumentException 如果监听器类型不匹配，则抛出此异常
     */
    public static void addNativeListener(EventListener listener) {
        switch (listener) {
            case NativeMouseListener nativeMouseListener -> GlobalScreen.addNativeMouseListener(nativeMouseListener);
            case NativeMouseMotionListener nativeMouseMotionListener ->
                    GlobalScreen.addNativeMouseMotionListener(nativeMouseMotionListener);
            case NativeKeyListener nativeKeyListener -> GlobalScreen.addNativeKeyListener(nativeKeyListener);
            default -> throw new IllegalArgumentException(text_unknownListener());
        }
    }

}
