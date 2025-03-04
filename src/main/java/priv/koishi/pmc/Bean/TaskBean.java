package priv.koishi.pmc.Bean;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;

/**
 * 多线程任务所需设置类
 *
 * @author KOISHI
 * Date:2024-10-24
 * Time:下午3:15
 */
@Data
@Accessors(chain = true)
public class TaskBean<T> {

    /**
     * 要处理的文件夹文件
     */
    List<File> inFileList;

    /**
     * 要处理的数据
     */
    List<T> beanList;

    /**
     * 用来更新数据的列表
     */
    TableView<T> tableView;

    /**
     * 线程进度条
     */
    ProgressBar progressBar;

    /**
     * 线程信息栏
     */
    Label massageLabel;

    /**
     * 页面标识符
     */
    String tabId;

    /**
     * 要防重复点击的组件
     */
    List<Control> disableControls;

}
