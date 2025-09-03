package priv.koishi.pmc.CustomUI.CustomEditingCell;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static priv.koishi.pmc.Finals.i18nFinal.text_editingCellTip;
import static priv.koishi.pmc.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 可编辑的javafx列表单元格
 *
 * @author KOISHI
 * Date:2024-11-04
 * Time:下午6:11
 */
public class EditingCell<T> extends TableCell<T, String> {

    /**
     * 在进入编辑状态时,所显示的输入框
     */
    private TextField textField;

    /**
     * 用于引入lambda表达式的对象
     */
    private final ItemConsumer<? super T> itemConsumer;

    /**
     * 单元格鼠标悬停提示
     */
    private final String tip = text_editingCellTip();

    /**
     * 输入框可输入的最小值
     */
    private Integer min;

    /**
     * 输入框可输入的最大值
     */
    private Integer max;

    /**
     * 输入框限制只能输入整数
     */
    private boolean integerRange;

    /**
     * 输入框文本改变监听器（限制只能输入数字）
     */
    private ChangeListener<String> textChangeListener;

    /**
     * 输入框文本改变监听器（更新鼠标悬浮提示）
     */
    private ChangeListener<String> stringChangeListener;

    /**
     * 输入框失去焦点时,提交编辑监听器
     */
    private ChangeListener<? super Boolean> textFocusedPropertyListener;

    /**
     * 单元格列名
     */
    private String tableColumnText;

    /**
     * 构造EditingCell对象,并且明确将该cell的值保存进相应的JavaBean的属性值的方法
     *
     * @param itemConsumer 用于引入lambda表达式的对象
     */
    public EditingCell(ItemConsumer<? super T> itemConsumer) {
        this.itemConsumer = itemConsumer;
    }

    /**
     * 构造EditingCell对象,并且明确将该cell的值保存进相应的JavaBean的属性值的方法
     *
     * @param itemConsumer 用于引入lambda表达式的对象
     * @param integerRange 输入框是否限制只能输入整数
     * @param min          输入框可输入的最小值
     * @param max          输入框可输入的最大值
     */
    public EditingCell(ItemConsumer<? super T> itemConsumer, boolean integerRange, Integer min, Integer max) {
        this.itemConsumer = itemConsumer;
        this.integerRange = integerRange;
        this.min = min;
        this.max = max;
    }

    /**
     * 进入编辑状态
     */
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            if (Objects.isNull(textField)) {
                createTextField();
            }
            // 绑定监听器
            bindListeners();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }
    }

    /**
     * 退出编辑状态
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setGraphic(null);
        // 移除监听器
        removeListeners();
    }

    /**
     * 更新单元格显示的值
     *
     * @param item  显示的值
     * @param empty 是否为空
     */
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            // 移除监听器
            removeListeners();
            textField = null;
            setTooltip(null);
            setText(null);
            setGraphic(null);
        } else {
            tableColumnText = getTableColumn().getText();
            Node graphic = getTableColumn().getGraphic();
            if (graphic != null) {
                if (graphic instanceof Label label) {
                    tableColumnText = label.getText();
                }
            }
            if (tableColumnText == null) {
                tableColumnText = "";
            }
            setTooltip(creatTooltip(tip + tableColumnText + "\n" + getString()));
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    /**
     * 提交编辑
     *
     * @param newValue 要提交的新值
     */
    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        updateItem(newValue, false);
        setTProperties(newValue);
        // 移除监听器
        removeListeners();
    }

    /**
     * 将编辑后的对象属性进行保存.
     * 如果不将属性保存到cell所在表格的ObservableList集合中对象的相应属性中,
     * 则只是改变了表格显示的值,一旦表格刷新,则仍会表示旧值.
     *
     * @param newValue 新值
     */
    private void setTProperties(String newValue) {
        TableView<T> tableView = getTableView();
        T t = tableView.getItems().get(getIndex());
        itemConsumer.setTProperties(t, newValue);
    }

    /**
     * 在单元格中创建输入框
     */
    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(getWidth() - getGraphicTextGap() * 2);
        addValueToolTip(textField, tip + tableColumnText);
    }

    /**
     * 绑定监听器
     */
    private void bindListeners() {
        if (textField != null) {
            // 先移除旧的再绑定新的
            removeListeners();
            stringChangeListener = textFieldValueListener(textField, tip);
            // 限制只能输入整数
            if (integerRange) {
                textChangeListener = (observable, oldValue, newValue) -> {
                    if (!isInIntegerRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                        textField.setText(oldValue);
                    }
                };
                textField.textProperty().addListener(textChangeListener);
            }
            // 输入框失去焦点时,提交编辑
            textFocusedPropertyListener = (ob, old, now) -> {
                if (!now) {
                    commitEdit(textField.getText());
                }
            };
            textField.focusedProperty().addListener(textFocusedPropertyListener);
        }
    }

    /**
     * 获取单元格值的字符串
     *
     * @return 单元格值的字符串
     */
    private String getString() {
        return getItem() == null ? "" : getItem();
    }

    /**
     * 移除监听器
     */
    private void removeListeners() {
        if (textField != null && textFocusedPropertyListener != null) {
            textField.focusedProperty().removeListener(textFocusedPropertyListener);
            textFocusedPropertyListener = null;
        }
        if (textField != null && textChangeListener != null) {
            textField.textProperty().removeListener(textChangeListener);
            textChangeListener = null;
        }
        if (textField != null && stringChangeListener != null) {
            textField.textProperty().removeListener(stringChangeListener);
            stringChangeListener = null;
        }
    }

}
