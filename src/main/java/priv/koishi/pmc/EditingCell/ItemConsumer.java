package priv.koishi.pmc.EditingCell;

/**
 * @author KOISHI
 * Date:2024-11-04
 * Time:下午6:14
 */
public interface ItemConsumer<T> {

    /**
     * 将编辑后的对象属性进行保存.
     * 如果不将属性保存到cell所在表格的ObservableList集合中对象的相应属性中,
     * 则只是改变了表格显示的值,一旦表格刷新,则仍会表示旧值.
     */
    void setTProperties(T t, String value);

}
