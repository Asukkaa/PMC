package priv.koishi.pmc.RowNumberCellFactory;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import static priv.koishi.pmc.Utils.UiUtils.creatTooltip;

/**
 * 序号列工厂
 *
 * @author KOISHI
 * Date:2025-04-02
 * Time:20:11
 */
public class RowNumberCellFactory<S> implements Callback<TableColumn<S, Integer>, TableCell<S, Integer>> {

    @Override
    public TableCell<S, Integer> call(TableColumn<S, Integer> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    // 获取当前行的索引并加1（行号从1开始）
                    int rowIndex = getIndex() + 1;
                    setText(String.valueOf(rowIndex));
                    setTooltip(creatTooltip(String.valueOf(rowIndex)));
                }
            }
        };
    }

}