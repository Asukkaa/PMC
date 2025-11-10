package priv.koishi.pmc.UI.CustomEditingCell;

/**
 * 可编辑的 JavaFX 列表单元格接口
 *
 * @author KOISHI
 * Date:2024-11-04
 * Time:下午6:14
 */
public interface ItemConsumer<T> {

    /**
     * 将编辑后的对象属性进行保存.
     * 如果不将属性保存到 cell 所在表格的 ObservableList 集合中对象的相应属性中,
     * 则只是改变了表格显示的值,一旦表格刷新,则仍会表示旧值.
     *
     * @param t     当前行数据
     * @param value 新值
     */
    void setTProperties(T t, String value);

    /**
     * 检查单元格是否可编辑
     *
     * @param t 当前行数据
     * @return true-可编辑，false-不可编辑
     */
    default boolean isEditable(T t) {
        return true;
    }

    /**
     * 获取单元格禁用时的显示值
     *
     * @param t 当前行数据
     * @return 禁用时显示的值
     */
    default String getDisabledValue(T t) {
        return null;
    }

}
