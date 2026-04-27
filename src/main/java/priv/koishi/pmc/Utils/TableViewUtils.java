package priv.koishi.pmc.Utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Annotation.CheckBoxColumn;
import priv.koishi.pmc.Bean.Annotation.IndexColumn;
import priv.koishi.pmc.Bean.Annotation.PathColumn;
import priv.koishi.pmc.Bean.Interface.CopyBean;
import priv.koishi.pmc.Bean.Interface.FilePath;
import priv.koishi.pmc.Bean.Interface.ImgBean;
import priv.koishi.pmc.Bean.Interface.Indexable;
import priv.koishi.pmc.Bean.TessdataBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Controller.AutoClickController;
import priv.koishi.pmc.Finals.Enum.ClickTypeEnum;
import priv.koishi.pmc.Finals.Enum.RecognitionTypeEnum;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.Utils.CommonUtils.NATURAL_SORT;
import static priv.koishi.pmc.Utils.CommonUtils.moveAllToFirst;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.ToolTipUtils.creatTooltip;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 表格工具类
 *
 * @author KOISHI
 * Date:2025-11-10
 * Time:15:02
 */
public class TableViewUtils {

    /**
     * 拖拽数据格式
     */
    public static final DataFormat dragDataFormat = new DataFormat("application/x-java-serialized-object");

    /**
     * 反射获取的字段缓存
     */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 为 JavaFX 单元格赋值并添加鼠标悬停提示
     *
     * @param tableColumn 要处理的 JavaFX 列表列
     * @param param       JavaFX 列表列对应的数据属性名
     */
    public static void buildCellValue(TableColumn<?, ?> tableColumn, String param) {
        tableColumn.setCellValueFactory(new PropertyValueFactory<>(param));
        // 为 JavaFX 单元格和表头添加鼠标悬停提示
        addTableCellToolTip(tableColumn);
    }

    /**
     * 自定义单元格工厂，为单元格添加 Tooltip
     *
     * @param column 要处理的 JavaFX 表格单元格
     * @param <S>    表格单元格数据类型
     * @param <T>    表格单元格类型
     */
    public static <S, T> void addTableCellToolTip(TableColumn<S, T> column) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || StringUtils.isEmpty(item.toString())) {
                            setText(null);
                            setTooltip(null);
                        } else if (!item.toString().isEmpty()) {
                            setText(item.toString());
                            setTooltip(creatTooltip(getTipText(column, item.toString())));
                        }
                    }
                };
            }
        });
    }

    /**
     * 为表头添加鼠标悬停提示
     *
     * @param column 要处理的 JavaFX 表格列
     * @param <S>    表格单元格数据类型
     * @param <T>    表格单元格类型
     */
    public static <S, T> void addTableColumnToolTip(TableColumn<S, T> column) {
        addTableColumnToolTip(column, column.getText());
    }

    /**
     * 为表头添加鼠标悬停提示
     *
     * @param column  要处理的 JavaFX 表格列
     * @param tooltip 要展示的提示文案
     * @param <S>     表格单元格数据类型
     * @param <T>     表格单元格类型
     */
    public static <S, T> void addTableColumnToolTip(TableColumn<S, T> column, String tooltip) {
        String columnText = column.getText();
        if (StringUtils.isNotBlank(columnText)) {
            Label label = new Label(columnText);
            // 完全绑定 Labe l宽度到 TableColumn 宽度
            label.prefWidthProperty().bind(column.widthProperty());
            label.setMaxWidth(Control.USE_PREF_SIZE);
            label.setMinWidth(Control.USE_PREF_SIZE);
            // 确保文本居中显示
            label.setAlignment(Pos.CENTER);
            // 文本过长时自动调整
            label.setTextOverrun(OverrunStyle.ELLIPSIS);
            addToolTip(tooltip, label);
            column.setGraphic(label);
            column.setText(null);
        }
    }

    /**
     * 根据 bean 属性名自动填充 JavaFX 表格
     *
     * @param tableView 要处理的 JavaFX 表格
     * @param beanClass 要处理的 JavaFX 表格的数据 bean 类
     * @param tabId     用于区分不同列表的 id，要展示的数据 bean 属性名加上 tabId 即为 JavaFX 列表的列对应的 id
     * @param <T>       要处理的 JavaFX 表格的数据 bean 类
     */
    public static <T> void autoBuildTableViewData(TableView<T> tableView, Class<?> beanClass, String tabId) {
        autoBuildTableViewData(tableView, beanClass, tabId, null);
    }

    /**
     * 根据 bean 属性名自动填充 JavaFX 表格
     *
     * @param tableView        要处理的 JavaFX 表格
     * @param beanClass        要处理的 JavaFX 表格的数据 bean 类
     * @param tabId            用于区分不同列表的 id，要展示的数据 bean 属性名加上 tabId 即为 JavaFX 列表的列对应的 id
     * @param checkBoxCallback 开关单元格回调函数
     * @param <T>              要处理的 JavaFX 表格的数据 bean 类
     */
    @SuppressWarnings("unchecked")
    public static <T> void autoBuildTableViewData(TableView<T> tableView, Class<?> beanClass, String tabId,
                                                  Runnable checkBoxCallback) {
        // 递归获取类及其父类的所有字段
        List<Field> fields = getAllFields(beanClass);
        ObservableList<? extends TableColumn<?, ?>> columns = tableView.getColumns();
        fields.forEach(f -> {
            String fieldName = f.getName();
            String finalFieldName;
            if (StringUtils.isNotEmpty(tabId)) {
                finalFieldName = fieldName + tabId;
            } else {
                finalFieldName = fieldName;
            }
            Optional<? extends TableColumn<?, ?>> matched = columns.stream().filter(c ->
                    finalFieldName.equals(c.getId())).findFirst();
            matched.ifPresent(m -> {
                // 添加列名 Tooltip
                addTableColumnToolTip(m);
                if (f.isAnnotationPresent(CheckBoxColumn.class)) {
                    tableView.setEditable(true);
                    CheckBoxColumn annotation = f.getAnnotation(CheckBoxColumn.class);
                    String textKey = annotation.textKey();
                    String labelText = bundle.getString(textKey);
                    if (StringUtils.isEmpty(labelText)) {
                        // 若未指定文本使用字段名
                        labelText = fieldName;
                    }
                    Class<?> fieldType = f.getType();
                    boolean isBooleanType = fieldType == boolean.class
                            || fieldType == Boolean.class
                            || fieldType == BooleanProperty.class;
                    if (isBooleanType) {
                        @SuppressWarnings("unchecked")
                        TableColumn<T, Boolean> boolColumn = (TableColumn<T, Boolean>) m;
                        buildCheckBoxCell(boolColumn, labelText, fieldName, checkBoxCallback);
                    }
                } else if (f.getType() == Image.class) {
                    // 创建图片表格
                    buildThumbnailCell((TableColumn<T, Image>) m, bean -> {
                        if (bean instanceof ImgBean imgBean) {
                            return imgBean.loadThumb();
                        }
                        return null;
                    });
                } else if (f.isAnnotationPresent(IndexColumn.class)) {
                    // 设置列为序号列
                    buildIndexCellValue((TableColumn<T, Integer>) m);
                } else if (beanClass == ImgFileVO.class && f.isAnnotationPresent(PathColumn.class)) {
                    TableColumn<ImgFileVO, String> pathColumn = (TableColumn<ImgFileVO, String>) m;
                    pathColumn.setCellValueFactory(cellData ->
                            cellData.getValue().pathProperty());
                    // 为 JavaFX 单元格和表头添加鼠标悬停提示
                    addTableCellToolTip(pathColumn);
                } else {
                    // 为 JavaFX 单元格赋值并添加鼠标悬停提示
                    buildCellValue(m, fieldName);
                }
            });
        });
    }

    /**
     * 创建 CheckBox 列
     *
     * @param column           目标列
     * @param labelText        CheckBox 显示的文本
     * @param fieldName        字段名，用于反射访问
     * @param checkBoxCallback 开关单元格回调函数
     * @param <T>              表格数据类型
     */
    public static <T> void buildCheckBoxCell(TableColumn<T, Boolean> column, String labelText, String fieldName,
                                             Runnable checkBoxCallback) {
        // 设置 CellValueFactory，通过反射读取字段当前值
        column.setCellValueFactory(cellData -> {
            T bean = cellData.getValue();
            if (bean == null) {
                return new SimpleBooleanProperty(false);
            }
            try {
                Field field = getCachedField(bean.getClass(), fieldName);
                return new SimpleBooleanProperty(field.getBoolean(bean));
            } catch (Exception e) {
                return new SimpleBooleanProperty(false);
            }
        });

        column.setCellFactory(_ -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox(labelText);
            private T currentBean;

            {
                checkBox.setAlignment(Pos.CENTER);
                checkBox.setCursor(Cursor.HAND);
                // 当 CheckBox 被点击时，提交编辑
                checkBox.setOnAction(_ -> {
                    if (currentBean != null) {
                        commitEdit(checkBox.isSelected());
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                    currentBean = null;
                } else {
                    currentBean = getTableRow().getItem();
                    checkBox.setSelected(item != null && item);
                    setGraphic(checkBox);
                    setText(null);
                    String value = ((item != null && item) ? text_enable() : text_disable());
                    String tipText = getTipText(column, value);
                    setTooltip(creatTooltip(tipText));
                    addToolTip(tipText, checkBox);
                }
            }

            @Override
            public void commitEdit(Boolean newValue) {
                super.commitEdit(newValue);
                if (currentBean != null) {
                    try {
                        Field field = getCachedField(currentBean.getClass(), fieldName);
                        field.setBoolean(currentBean, newValue);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (checkBoxCallback != null) {
                    checkBoxCallback.run();
                }
            }
        });

        column.setEditable(true);
    }

    /**
     * 从缓存获取字段对象
     *
     * @param clazz     要获取字段的类
     * @param fieldName 要获取的属性名
     */
    private static Field getCachedField(Class<?> clazz, String fieldName) {
        return FIELD_CACHE
                .computeIfAbsent(clazz, _ -> new ConcurrentHashMap<>())
                .computeIfAbsent(fieldName, name -> {
                    Class<?> current = clazz;
                    while (current != null && current != Object.class) {
                        try {
                            Field field = current.getDeclaredField(name);
                            field.setAccessible(true);
                            return field;
                        } catch (NoSuchFieldException e) {
                            current = current.getSuperclass();
                        }
                    }
                    throw new RuntimeException(new NoSuchFieldException(name));
                });
    }

    /**
     * 设置列为序号列
     *
     * @param column 要处理的列
     * @param <T>    列对应的数据类型
     */
    public static <T> void buildIndexCellValue(TableColumn<T, Integer> column) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<T, Integer> call(TableColumn<T, Integer> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            // 获取当前行的索引并加 1 （行号从 1 开始）
                            int rowIndex = getIndex() + 1;
                            T itemData = getTableRow().getItem();
                            if (itemData instanceof Indexable indexable) {
                                indexable.setIndex(rowIndex);
                            }
                            setText(String.valueOf(rowIndex));
                            setTooltip(creatTooltip(getTipText(column, String.valueOf(rowIndex))));
                        }
                    }
                };
            }
        });
    }

    /**
     * 处理单元格鼠标悬停提示文本
     *
     * @param column 要处理的列
     * @param value  单元格名称
     * @param <S>    表格单元格数据类型
     * @param <T>    表格单元格类型
     * @return 根据单元格是否为空返回不同提示文本
     */
    private static <S, T> String getTipText(TableColumn<S, T> column, String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        Label label = (Label) column.getGraphic();
        return label.getText() + "：" + value;
    }

    /**
     * 递归获取类及其父类的所有字段
     *
     * @param clazz 要获取字段的类
     * @return 当前类和父类所有字段
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 创建图片表格
     *
     * @param column        要创建图片表格的列
     * @param thumbSupplier 获取图片的函数
     * @param <T>           列对应的数据类型
     */
    public static <T> void buildThumbnailCell(TableColumn<T, Image> column, Function<? super T, ? extends Image> thumbSupplier) {
        column.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(thumbSupplier.apply(cellData.getValue())));
        column.setCellFactory(_ -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);
                textFillProperty().bind(textColorProperty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (image == null) {
                    TableRow<T> tableRow = getTableRow();
                    T bean = tableRow.getItem();
                    String target = null;
                    if (bean instanceof ImgFileVO imgFileVO) {
                        target = imgFileVO.getPath();
                    } else if (bean instanceof ClickPositionVO clickPositionVO) {
                        target = clickPositionVO.getClickImgTarget();
                        int recognitionType = clickPositionVO.getRecognitionType();
                        if (RecognitionTypeEnum.COLOR.ordinal() == recognitionType) {
                            Color color = Color.valueOf(target);
                            if (color != null) {
                                // 颜色预览方块
                                Rectangle colorRect = new Rectangle(30, 30, color);
                                colorRect.setStroke(Color.valueOf("#1b2026"));
                                colorRect.setStrokeWidth(1);
                                // 颜色值文本
                                Label colorLabel = new Label(target);
                                HBox hbox = new HBox(5, colorRect, colorLabel);
                                hbox.setAlignment(Pos.CENTER_LEFT);
                                setGraphic(hbox);
                                setText(null);
                                String text = recognitionType_color() + "：" + target;
                                setTooltip(creatTooltip(text));
                                return;
                            }
                        } else if (RecognitionTypeEnum.TEXT.ordinal() == recognitionType) {
                            String text = recognitionType_text() + "：" + target;
                            setText(text);
                            setTooltip(creatTooltip(text));
                            return;
                        }
                    }
                    if (target != null && !new File(target).exists()) {
                        setText(text_badImg());
                        textFillProperty().unbind();
                        setTextFill(Color.RED);
                        setTooltip(creatTooltip(text_badImg()));
                    } else {
                        setText(text_noImg());
                        setTooltip(creatTooltip(text_noImg()));
                    }
                    setGraphic(null);
                } else {
                    setText(null);
                    imageView.setImage(image);
                    setGraphic(imageView);
                    setTooltip(creatTooltip(image.getUrl().replace("file:", text_imgPath())));
                }
            }
        });
    }

    /**
     * 清空 JavaFX 列表数据
     *
     * @param tableView  要清空的 JavaFX 列表
     * @param fileNumber 用于展示列表数据数量的文本框
     * @param <T>        数据类型
     */
    public static <T> void removeTableViewData(TableView<T> tableView, Label fileNumber) {
        tableView.getItems().clear();
        updateLabel(fileNumber, listText_dataListNull());
        System.gc();
    }

    /**
     * 向列表指定位置添加数据
     *
     * @param data           要添加的数据
     * @param addType        添加位置类型
     * @param tableView      要添加数据的列表
     * @param dataNumber     用于展示列表数据数量的文本框
     * @param dataNumberUnit 数据数量单位
     * @param <T>            数据类型
     */
    public static <T> void addData(List<? extends T> data, int addType, TableView<T> tableView, Label dataNumber, String dataNumberUnit) {
        addData(data, addType, tableView, dataNumber, dataNumberUnit, true);
    }

    /**
     * 向列表指定位置添加数据
     *
     * @param data           要添加的数据
     * @param addType        添加位置类型
     * @param tableView      要添加数据的列表
     * @param dataNumber     用于展示列表数据数量的文本框
     * @param dataNumberUnit 数据数量单位
     * @param selected       true 表示选中添加的数据，false 表示不选中添加的数据
     * @param <T>            数据类型
     */
    public static <T> void addData(List<? extends T> data, int addType, TableView<T> tableView, Label dataNumber,
                                   String dataNumberUnit, boolean selected) {
        ObservableList<T> tableViewItems = tableView.getItems();
        TableView.TableViewSelectionModel<T> selectionModel = tableView.getSelectionModel();
        List<T> selectedItem = selectionModel.getSelectedItems();
        switch (addType) {
            // 在列表所选行第一行上方插入
            case upAdd: {
                // 获取首个选中行的索引
                int selectedIndex = tableViewItems.indexOf(selectedItem.getFirst());
                // 在选中行上方插入数据
                tableView.getItems().addAll(selectedIndex, data);
                // 滚动到插入位置
                tableView.scrollTo(selectedIndex);
                // 选中新插入的数据
                if (selected) {
                    selectionModel.selectRange(selectedIndex, selectedIndex + data.size());
                }
                break;
            }
            // 在列表所选行最后一行下方插入
            case downAdd: {
                // 获取最后一个选中行的索引
                int selectedIndex = tableViewItems.indexOf(selectedItem.getLast()) + 1;
                // 在选中行下方插入数据
                tableView.getItems().addAll(selectedIndex, data);
                // 滚动到插入位置
                tableView.scrollTo(selectedIndex);
                // 选中新插入的数据
                if (selected) {
                    selectionModel.selectRange(selectedIndex, selectedIndex + data.size());
                }
                break;
            }
            // 向列表第一行上方插入
            case topAdd: {
                // 向列表第一行追加数据
                tableView.getItems().addAll(0, data);
                // 滚动到插入位置
                tableView.scrollTo(0);
                // 选中新插入的数据
                if (selected) {
                    selectionModel.selectRange(0, data.size());
                }
                break;
            }
            // 向列表最后一行追加
            case append: {
                int lastIndex = tableViewItems.size();
                // 向列表最后一行追加数据
                tableViewItems.addAll(data);
                // 滚动到插入位置
                tableView.scrollTo(tableViewItems.size());
                // 选中新插入的数据
                if (selected) {
                    selectionModel.selectRange(lastIndex, lastIndex + data.size());
                }
                break;
            }
        }
        // 同步表格数据量
        updateTableViewSizeText(tableView, dataNumber, dataNumberUnit);
        tableView.refresh();
    }

    /**
     * 设置列表通过拖拽排序行
     *
     * @param tableView 要处理的列表
     * @param <T>       列表数据类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void tableViewDragRow(TableView<T> tableView) {
        tableView.setRowFactory(_ -> {
            TableRow<T> row = new TableRow<>();
            ObservableList<Integer> draggedIndices = FXCollections.observableArrayList();
            // 拖拽检测
            row.setOnDragDetected(e -> {
                if (!row.isEmpty()) {
                    // 获取所有选中的行索引
                    draggedIndices.setAll(tableView.getSelectionModel().getSelectedIndices().stream()
                            .sorted().collect(Collectors.toList()));
                    // 只允许非空且选中多行时拖拽
                    if (!draggedIndices.isEmpty()) {
                        Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                        db.setDragView(row.snapshot(null, null));
                        // 使用自定义数据格式存储多个索引
                        ClipboardContent cc = new ClipboardContent();
                        cc.put(dragDataFormat, new ArrayList<>(draggedIndices));
                        db.setContent(cc);
                        e.consume();
                    }
                }
            });
            // 拖拽悬停验证
            row.setOnDragOver(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dragDataFormat)) {
                    // 禁止拖拽到选中行内部
                    List<?> indices = (List<?>) db.getContent(dragDataFormat);
                    int dropIndex = row.isEmpty() ? tableView.getItems().size() : row.getIndex();
                    if (!indices.contains(dropIndex)) {
                        e.acceptTransferModes(TransferMode.MOVE);
                        e.consume();
                    }
                }
            });
            // 拖拽释放处理
            row.setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dragDataFormat)) {
                    List<Integer> indices = (List<Integer>) db.getContent(dragDataFormat);
                    int maxIndex = tableView.getItems().size();
                    int dropIndex = row.isEmpty() ? maxIndex : row.getIndex();
                    // 计算有效插入位置
                    int adjustedDropIndex = calculateAdjustedIndex(indices, dropIndex, maxIndex);
                    if (adjustedDropIndex != -1) {
                        // 批量移动数据
                        moveRows(tableView, indices, adjustedDropIndex);
                        // 更新选中状态
                        selectMovedRows(tableView, indices, adjustedDropIndex);
                        e.setDropCompleted(true);
                        e.consume();
                    } else {
                        // 确保拖拽失败
                        e.setDropCompleted(false);
                        // 消费事件以避免进一步传播
                        e.consume();
                    }
                }
            });
            return row;
        });
    }

    /**
     * 计算调整后的插入位置
     *
     * @param draggedIndices 被拖拽行的原始索引列表（需保证有序）
     * @param dropIndex      拖拽操作的目标放置位置原始索引
     * @param maxIndex       表格数据项总数
     * @return 调整后的有效插入位置，返回-1表示无效拖拽位置
     */
    private static int calculateAdjustedIndex(List<Integer> draggedIndices, int dropIndex, int maxIndex) {
        int firstDragged = draggedIndices.getFirst();
        int lastDragged = draggedIndices.getLast();
        if (dropIndex + 1 >= maxIndex) {
            return maxIndex - draggedIndices.size();
        }
        if (dropIndex >= firstDragged && dropIndex <= lastDragged) {
            return -1;
        }
        return dropIndex;
    }

    /**
     * 批量移动行数据
     *
     * @param tableView   目标表格视图对象
     * @param indices     需要移动的行索引列表（需保证有序）
     * @param targetIndex 移动的目标插入位置（经过调整后的有效位置）
     * @param <T>         表格数据项类型
     */
    private static <T> void moveRows(TableView<T> tableView, List<Integer> indices, int targetIndex) {
        ObservableList<T> items = tableView.getItems();
        List<T> movedItems = indices.stream().map(items::get).toList();
        // 批量操作减少刷新次数
        items.removeAll(movedItems);
        items.addAll(targetIndex, movedItems);
    }

    /**
     * 重新选中移动后的行
     *
     * @param tableView       目标表格视图对象
     * @param originalIndices 移动前的原始行索引列表
     * @param targetIndex     移动后的起始插入位置
     * @param <T>             表格数据项类型
     */
    private static <T> void selectMovedRows(TableView<T> tableView, List<Integer> originalIndices, int targetIndex) {
        tableView.getSelectionModel().clearSelection();
        for (int i = 0; i < originalIndices.size(); i++) {
            tableView.getSelectionModel().select(targetIndex + i);
        }
    }

    /**
     * 移动所选行选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param <T>         表格数据项类型
     */
    public static <T> void buildMoveDataMenu(TableView<T> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_moveSelected());
        // 创建二级菜单项
        MenuItem up = new MenuItem(menuItem_moveUp());
        MenuItem down = new MenuItem(menuItem_moveDown());
        MenuItem top = new MenuItem(menuItem_moveTop());
        MenuItem bottom = new MenuItem(menuItem_moveBottom());
        // 为每个菜单项添加事件处理
        up.setOnAction(_ -> upMoveDataMenuItem(tableView));
        down.setOnAction(_ -> downMoveDataMenuItem(tableView));
        top.setOnAction(_ -> topMoveDataMenuItem(tableView));
        bottom.setOnAction(_ -> bottomMoveDataMenuItem(tableView));
        // 将菜单添加到菜单列表
        menu.getItems().setAll(up, down, top, bottom);
        contextMenu.getItems().add(menu);
    }

    /**
     * 所选行上移一行选项
     *
     * @param tableView 要处理的数据列表
     * @param <T>       表格数据项类型
     */
    private static <T> void upMoveDataMenuItem(TableView<T> tableView) {
        // getSelectedCells处理上移操作有bug，通过getSelectedItems拿到的数据是实时变化的，需要一个新的list来存
        List<T> selectionList = tableView.getSelectionModel().getSelectedItems();
        List<T> selections = new ArrayList<>(selectionList);
        List<T> fileList = tableView.getItems();
        List<T> tempList = new ArrayList<>(fileList);
        // 上移所选数据位置
        for (int i = 0; i < selectionList.size(); i++) {
            T t = selectionList.get(i);
            int index = fileList.indexOf(t);
            if (index - i > 0) {
                tempList.set(index, tempList.get(index - 1));
                tempList.set(index - 1, t);
            }
        }
        fileList.clear();
        fileList.addAll(tempList);
        // 重新选中移动后的数据
        for (T t : selections) {
            int index = fileList.indexOf(t);
            if (index != -1) {
                tableView.getSelectionModel().select(index);
            }
        }
    }

    /**
     * 所选行下移一行选项
     *
     * @param tableView 要处理的数据列表
     * @param <T>       表格数据项类型
     */
    private static <T> void downMoveDataMenuItem(TableView<T> tableView) {
        var selectedCells = tableView.getSelectionModel().getSelectedCells();
        int loopTime = 0;
        for (int i = selectedCells.size(); i > 0; i--) {
            int row = selectedCells.get(i - 1).getRow();
            List<T> fileList = tableView.getItems();
            loopTime++;
            if (row + loopTime < fileList.size()) {
                fileList.add(row, fileList.remove(row + 1));
            }
        }
    }

    /**
     * 所选行置顶
     *
     * @param tableView 要处理的数据列表
     * @param <T>       表格数据项类型
     */
    private static <T> void topMoveDataMenuItem(TableView<T> tableView) {
        ObservableList<T> items = tableView.getItems();
        List<T> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
        if (!selectedItems.isEmpty()) {
            // 移除所有选中项
            items.removeAll(selectedItems);
            // 插入到列表顶部（保持原有顺序）
            items.addAll(0, selectedItems);
            // 刷新表格显示
            tableView.refresh();
            // 重新选中被移动的项
            tableView.getSelectionModel().clearSelection();
            tableView.getSelectionModel().selectRange(0, selectedItems.size());
        }
    }

    /**
     * 所选行置底
     *
     * @param tableView 要处理的数据列表
     * @param <T>       表格数据项类型
     */
    private static <T> void bottomMoveDataMenuItem(TableView<T> tableView) {
        ObservableList<T> items = tableView.getItems();
        List<T> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
        if (!selectedItems.isEmpty()) {
            // 移除所有选中项
            items.removeAll(selectedItems);
            // 插入到列表末尾（保持原有顺序）
            items.addAll(selectedItems);
            // 刷新表格显示
            tableView.refresh();
            // 重新选中被移动的项
            tableView.getSelectionModel().clearSelection();
            int lastIndex = items.size() - 1;
            int startIndex = lastIndex - selectedItems.size() + 1;
            if (startIndex >= 0 && lastIndex >= startIndex) {
                tableView.getSelectionModel().selectRange(startIndex, lastIndex + 1);
            }
        }
    }

    /**
     * 查看文件选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildFilePathMenu(TableView<? extends FilePath> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_viewFile());
        // 创建二级菜单项
        MenuItem openFile = new MenuItem(menuItem_openSelected());
        MenuItem openDirector = new MenuItem(menuItem_openDirectory());
        MenuItem copyFilePath = new MenuItem(menuItem_copyFilePath());
        // 为每个菜单项添加事件处理
        openFile.setOnAction(_ -> openFileMenuItem(tableView));
        openDirector.setOnAction(_ -> openDirectorMenuItem(tableView));
        copyFilePath.setOnAction(_ -> copyFilePathItem(tableView));
        // 将菜单添加到菜单列表
        menu.getItems().setAll(openFile, openDirector, copyFilePath);
        contextMenu.getItems().add(menu);
    }

    /**
     * 打开所选文件选项
     *
     * @param tableView 文件列表
     */
    private static void openFileMenuItem(TableView<? extends FilePath> tableView) {
        List<? extends FilePath> fileBeans = tableView.getSelectionModel().getSelectedItems();
        fileBeans.forEach(fileBean -> openFile(fileBean.getPath()));
    }

    /**
     * 打开所选文件所在文件夹选项
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void openDirectorMenuItem(TableView<? extends FilePath> tableView) {
        List<? extends FilePath> fileBeans = tableView.getSelectionModel().getSelectedItems();
        List<String> pathList = fileBeans.stream().map(FilePath::getPath).distinct().toList();
        pathList.forEach(FileUtils::openParentDirectory);
    }

    /**
     * 复制文件路径选项
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void copyFilePathItem(TableView<? extends FilePath> tableView) {
        FilePath fileBean = tableView.getSelectionModel().getSelectedItem();
        copyText(fileBean.getPath());
    }

    /**
     * 复制所选数据选项
     *
     * @param tableView      要添加右键菜单的列表
     * @param contextMenu    右键菜单集合
     * @param dataNumber     列表数据数量文本框
     * @param dataNumberUnit 数据数量单位
     */
    public static <T extends CopyBean> void buildCopyDataMenu(TableView<T> tableView, ContextMenu contextMenu,
                                                              Label dataNumber, String dataNumberUnit) {
        Menu menu = new Menu(menu_copy());
        // 创建二级菜单项
        MenuItem upCopy = new MenuItem(menuItem_upCopy());
        MenuItem downCopy = new MenuItem(menuItem_downCopy());
        MenuItem appendCopy = new MenuItem(menuItem_appendCopy());
        MenuItem topCopy = new MenuItem(menuItem_topCopy());
        // 为每个菜单项添加事件处理
        upCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_upCopy(), dataNumber, dataNumberUnit));
        downCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_downCopy(), dataNumber, dataNumberUnit));
        appendCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_appendCopy(), dataNumber, dataNumberUnit));
        topCopy.setOnAction(_ -> copyDataMenuItem(tableView, menuItem_topCopy(), dataNumber, dataNumberUnit));
        // 将菜单添加到菜单列表
        menu.getItems().setAll(upCopy, downCopy, appendCopy, topCopy);
        contextMenu.getItems().add(menu);
    }

    /**
     * 复制所选数据二级菜单选项
     *
     * @param tableView      要处理的数据列表
     * @param copyType       复制类型
     * @param dataNumber     列表数据数量文本框
     * @param dataNumberUnit 数据数量单位
     */
    private static <T extends CopyBean> void copyDataMenuItem(TableView<T> tableView, String copyType,
                                                              Label dataNumber, String dataNumberUnit) {
        List<T> copiedList = getCopyList(tableView.getSelectionModel().getSelectedItems());
        if (menuItem_upCopy().equals(copyType)) {
            addData(copiedList, upAdd, tableView, dataNumber, dataNumberUnit);
        } else if (menuItem_downCopy().equals(copyType)) {
            addData(copiedList, downAdd, tableView, dataNumber, dataNumberUnit);
        } else if (menuItem_appendCopy().equals(copyType)) {
            addData(copiedList, append, tableView, dataNumber, dataNumberUnit);
        } else if (menuItem_topCopy().equals(copyType)) {
            addData(copiedList, topAdd, tableView, dataNumber, dataNumberUnit);
        }
    }

    /**
     * 获取复制的数据
     *
     * @param selectedItem 选中的数据
     * @return 复制的数据
     */
    private static <T extends CopyBean> List<T> getCopyList(List<? extends T> selectedItem) {
        List<T> copiedList = new ArrayList<>();
        selectedItem.forEach(bean -> {
            T copyBean;
            try {
                copyBean = bean.createCopy();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            copiedList.add(copyBean);
        });
        return copiedList;
    }

    /**
     * 修改所选项终止操作图片地址
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param dataNumber  列表数据数量文本框
     * @param unit        列表数据数量单位
     */
    public static MenuItem buildEditStopImgPathMenu(TableView<ImgFileVO> tableView, ContextMenu contextMenu,
                                                    Label dataNumber, String unit) {
        MenuItem upMoveDataMenuItem = new MenuItem(menu_changeFirstImg());
        upMoveDataMenuItem.setOnAction(_ -> {
            ObservableList<ImgFileVO> selectedItems = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItems)) {
                ImgFileVO selectedItem = selectedItems.getFirst();
                Window window = tableView.getScene().getWindow();
                if (tableView == settingController.tableView_Set) {
                    AutoClickController.isSonOpening = true;
                }
                File file = creatImgFileChooser(window, selectedItem.getPath());
                if (file != null) {
                    List<ImgFileVO> allImg = tableView.getItems();
                    List<ImgFileVO> checkList = new ArrayList<>(allImg);
                    checkList.remove(selectedItem);
                    boolean isExist = checkList.stream().anyMatch(bean ->
                            file.getPath().equals(bean.getPath()));
                    if (isExist) {
                        ButtonType buttonType = creatConfirmDialog(
                                confirm_imageExist(),
                                confirm_imageExistConfirm(),
                                confirm_delete(),
                                confirm_cancel());
                        if (!buttonType.getButtonData().isCancelButton()) {
                            tableView.getItems().remove(selectedItem);
                        }
                    } else {
                        selectedItem.setPath(file.getPath());
                        selectedItem.setType(getExistsFileType(file));
                        selectedItem.setName(getExistsFileName(file));
                    }
                    selectedItem.updateThumb();
                    updateTableViewSizeText(tableView, dataNumber, unit);
                }
                if (tableView == settingController.tableView_Set) {
                    AutoClickController.isSonOpening = false;
                }
            }
        });
        contextMenu.getItems().add(upMoveDataMenuItem);
        return upMoveDataMenuItem;
    }

    /**
     * 修改所选项要点击的图片地址
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    public static void buildEditClickImgPathMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        MenuItem upMoveDataMenuItem = new MenuItem(menu_changeFirstImg());
        upMoveDataMenuItem.setOnAction(_ -> {
            ObservableList<ClickPositionVO> selectedItems = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItems)) {
                ClickPositionVO selectedItem = selectedItems.getFirst();
                if (canChangeImg(selectedItem)) {
                    Window window = tableView.getScene().getWindow();
                    AutoClickController.isSonOpening = true;
                    File file = creatImgFileChooser(window, selectedItem.getClickImgTarget());
                    if (file != null) {
                        selectedItem.setClickImgTarget(file.getAbsolutePath());
                        selectedItem.updateThumb();
                    }
                    AutoClickController.isSonOpening = false;
                }
            }
        });
        contextMenu.getItems().add(upMoveDataMenuItem);
    }

    /**
     * 判断是否可以修改图片地址
     *
     * @param vo 需要判断的数据
     * @return true 可修改图片地址 false 不可修改图片地址
     */
    private static boolean canChangeImg(ClickPositionVO vo) {
        int recognitionType = vo.getRecognitionType();
        int clickType = vo.getClickTypeEnum();
        return recognitionType == RecognitionTypeEnum.IMAGE.ordinal()
                && clickType != ClickTypeEnum.OPEN_FILE.ordinal()
                && clickType != ClickTypeEnum.OPEN_URL.ordinal()
                && clickType != ClickTypeEnum.RUN_SCRIPT.ordinal()
                && clickType != ClickTypeEnum.MOVE_TRAJECTORY.ordinal()
                && clickType != ClickTypeEnum.MOVE_WINDOW.ordinal();
    }

    /**
     * 取消选中选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     * @param <T>         列表数据类型
     */
    public static <T> void buildClearSelectedMenu(TableView<T> tableView, ContextMenu contextMenu) {
        MenuItem clearSelectedDataMenuItem = new MenuItem(menu_cancelSelected());
        clearSelectedDataMenuItem.setOnAction(_ -> tableView.getSelectionModel().clearSelection());
        contextMenu.getItems().add(clearSelectedDataMenuItem);
    }

    /**
     * 删除所选数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param dataNumber  列表对应的统计信息展示栏
     * @param contextMenu 右键菜单集合
     * @param unit        统计信息展示栏数量单位
     * @param <T>         列表数据类型
     */
    public static <T> void buildDeleteDataMenu(TableView<T> tableView, Label dataNumber, ContextMenu contextMenu, String unit) {
        MenuItem deleteDataMenuItem = new MenuItem(menu_deleteMenu());
        deleteDataMenuItem.setOnAction(_ -> {
            TableView.TableViewSelectionModel<T> selectionModel = tableView.getSelectionModel();
            // 要删除的选中项
            ObservableList<T> selectedItems = selectionModel.getSelectedItems();
            ObservableList<T> items = tableView.getItems();
            // 获取首个选中行的索引
            int selectedIndex = items.indexOf(selectedItems.getFirst());
            items.removeAll(selectedItems);
            if (selectedIndex > 0) {
                // 选中删除项的上一行
                tableView.getSelectionModel().clearSelection();
                tableView.getSelectionModel().select(selectedIndex - 1);
                // 滚动到插入位置
                tableView.scrollTo(selectedIndex - 1);
            }
            updateTableViewSizeText(tableView, dataNumber, unit);
        });
        contextMenu.getItems().add(deleteDataMenuItem);
    }

    /**
     * 为列表添加右键菜单并设置可选择多行
     *
     * @param contextMenu 右键菜单
     * @param tableView   要处理的列表
     * @param <T>         列表数据类型
     */
    public static <T> void setContextMenu(ContextMenu contextMenu, TableView<T> tableView) {
        setContextMenu(contextMenu, tableView, SelectionMode.MULTIPLE);
    }

    /**
     * 为列表添加右键菜单
     *
     * @param contextMenu   右键菜单
     * @param tableView     要处理的列表
     * @param selectionMode 选中模式
     * @param <T>           列表数据类型
     */
    public static <T> void setContextMenu(ContextMenu contextMenu, TableView<T> tableView, SelectionMode selectionMode) {
        // 设置是否可以选中多行
        tableView.getSelectionModel().setSelectionMode(selectionMode);
        tableView.setOnMousePressed(event -> {
            // 点击位置判断
            Node source = event.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || ((TableRow<?>) source).isEmpty()) {
                tableView.getSelectionModel().clearSelection();
                tableView.setContextMenu(null);
            } else if (event.isSecondaryButtonDown()) {
                if (CollectionUtils.isNotEmpty(tableView.getSelectionModel().getSelectedItems())) {
                    tableView.setContextMenu(contextMenu);
                } else {
                    tableView.setContextMenu(null);
                }
            }
        });
    }

    /**
     * 更新列表数据数量提示框
     *
     * @param tableView      列表对象
     * @param dataNumber     提示框对象
     * @param dataNumberUnit 数据单位
     * @param <T>            列表数据类型
     */
    public static <T> void updateTableViewSizeText(TableView<T> tableView, Label dataNumber, String dataNumberUnit) {
        int tableSize = tableView.getItems().size();
        if (tableSize > 0) {
            dataNumber.setText(text_allHave() + tableSize + dataNumberUnit);
        } else {
            dataNumber.setText(listText_dataListNull());
        }
    }

    /**
     * 图片列表拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    public static void acceptDropImg(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        files.forEach(file -> {
            if (isImgFile(file)) {
                // 接受拖放
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        });
        dragEvent.consume();
    }

    /**
     * 构建文件信息表格右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     */
    public static ContextMenu buildFileTableViewContextMenu(TableView<? extends FilePath> tableView) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 移动所选行选项
        buildMoveDataMenu(tableView, contextMenu);
        // 查看文件选项
        buildFilePathMenu(tableView, contextMenu);
        // 取消选中选项
        buildClearSelectedMenu(tableView, contextMenu);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView);
        return contextMenu;
    }

    /**
     * 构建图片文件信息表格右键菜单
     *
     * @param tableView  要添加右键菜单的列表
     * @param dataNumber 数据数量信息栏
     */
    public static void buildTableViewContextMenu(TableView<ImgFileVO> tableView, Label dataNumber) {
        // 添加右键菜单
        ContextMenu contextMenu = buildFileTableViewContextMenu(tableView);
        // 修改图片路径选项
        MenuItem menuItem = buildEditStopImgPathMenu(tableView, contextMenu, dataNumber, unit_img());
        // 删除所选数据选项
        buildDeleteDataMenu(tableView, dataNumber, contextMenu, unit_img());
        ObservableList<MenuItem> items = contextMenu.getItems();
        moveAllToFirst(items, menuItem);
    }

    /**
     * 文件大小排序
     *
     * @param sizeColumn 要进行文件大小排序的列
     */
    public static void fileSizeComparator(TableColumn<?, String> sizeColumn) {
        // 自定义比较器
        Comparator<String> customComparator = Comparator.comparingDouble(FileUtils::fileSizeCompareValue);
        // 应用自定义比较器
        sizeColumn.setComparator(customComparator);
    }

    /**
     * 文件名称排序
     *
     * @param nameColumn 要进行文件名称排序的列
     */
    public static void fileNameComparator(TableColumn<?, String> nameColumn) {
        // 应用自定义比较器
        nameColumn.setComparator(NATURAL_SORT);
    }

    /**
     * 修改模型启用状态选项
     *
     * @param tableMenu         右键菜单
     * @param tessdataTableView 模型文件列表
     * @param runnable          回调函数（可用来保存设置）
     */
    public static void buildTessdataActiveMenu(ContextMenu tableMenu, TableView<TessdataBean> tessdataTableView,
                                               Runnable runnable) {
        Menu menu = new Menu(menu_status());
        MenuItem activeItem = new MenuItem(menu_activeMenu());
        MenuItem unactiveItem = new MenuItem(menu_unActiveMenu());
        activeItem.setOnAction(_ -> buildSetActiveMenu(tableMenu, tessdataTableView, runnable, true));
        unactiveItem.setOnAction(_ -> buildSetActiveMenu(tableMenu, tessdataTableView, runnable, false));
        menu.getItems().setAll(activeItem, unactiveItem);
        tableMenu.getItems().add(menu);
    }

    /**
     * 启用所选选项
     *
     * @param contextMenu       右键菜单
     * @param tessdataTableView 模型文件列表
     * @param runnable          回调函数（可用来保存设置）
     * @param active            启用状态（true 启用）
     */
    public static void buildSetActiveMenu(ContextMenu contextMenu, TableView<TessdataBean> tessdataTableView,
                                          Runnable runnable, boolean active) {
        MenuItem menuItem = new MenuItem(menu_activeMenu());
        menuItem.setOnAction(_ -> {
            ObservableList<TessdataBean> selectedItems = tessdataTableView.getSelectionModel().getSelectedItems();
            selectedItems.forEach(item -> item.setActive(active));
            if (runnable != null) {
                runnable.run();
            } else {
                tessdataTableView.refresh();
            }
        });
        contextMenu.getItems().addFirst(menuItem);
    }

}
