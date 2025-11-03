package priv.koishi.pmc.Utils;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import priv.koishi.pmc.Bean.TaskBean;

import static priv.koishi.pmc.Finals.i18nFinal.text_taskFailed;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 多线程任务工具的方法
 *
 * @author KOISHI
 * Date:2024-10-28
 * Time:下午3:14
 */
public class TaskUtils {

    /**
     * 绑定带进度条的线程
     *
     * @param task     要绑定的线程任务
     * @param taskBean 绑定线程任务所需参数
     */
    public static void bindingTaskNode(Task<?> task, TaskBean<?> taskBean) {
        // 设置防重复点击按钮不可点击限制
        changeDisableNodes(taskBean, true);
        ProgressBar progressBar = taskBean.getProgressBar();
        if (progressBar != null) {
            // 绑定进度条的值属性
            progressBar.progressProperty().unbind();
            progressBar.setVisible(true);
            // 给进度条设置初始值
            progressBar.setProgress(0);
            progressBar.progressProperty().bind(task.progressProperty());
        }
        Label massageLabel = taskBean.getMassageLabel();
        if (massageLabel != null && taskBean.isBindingMassageLabel()) {
            // 绑定TextField的值属性
            massageLabel.textProperty().unbind();
            updateLabel(massageLabel, "");
            massageLabel.textProperty().bind(task.messageProperty());
        }
        // 设置默认的异常处理
        throwTaskException(task, taskBean);
    }

    /**
     * 抛出 task 异常
     *
     * @param task     有异常的线程任务
     * @param taskBean 线程任务所需参数
     * @throws RuntimeException 线程的异常
     */
    public static void throwTaskException(Task<?> task, TaskBean<?> taskBean) {
        task.setOnFailed(_ -> {
            taskNotSuccess(taskBean, text_taskFailed());
            // 获取抛出的异常
            throw new RuntimeException(task.getException());
        });
        task.setOnCancelled(_ -> taskNotSuccess(taskBean, text_taskFailed()));
    }

    /**
     * 线程组件解绑
     *
     * @param taskBean 要解绑的线程组件信息
     */
    public static void taskUnbind(TaskBean<?> taskBean) {
        // 解除防重复点击按钮不可点击限制
        changeDisableNodes(taskBean, false);
        // 隐藏和解绑消息通知组件
        Label massageLabel = taskBean.getMassageLabel();
        if (massageLabel != null) {
            massageLabel.textProperty().unbind();
        }
        // 隐藏和解绑进度条
        ProgressBar progressBar = taskBean.getProgressBar();
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.progressProperty().unbind();
        }
        System.gc();
    }

    /**
     * 线程没有完成统一处理方法
     *
     * @param taskBean 线程任务所需参数
     * @param log      要显示的日志
     */
    public static void taskNotSuccess(TaskBean<?> taskBean, String log) {
        taskUnbind(taskBean);
        showErrLabelText(taskBean.getMassageLabel(), log);
    }

}
