package priv.koishi.pmc.Utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import priv.koishi.pmc.Bean.TaskBean;

import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Utils.NodeDisableUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.showErrLabelText;
import static priv.koishi.pmc.Utils.UiUtils.updateLabel;

/**
 * 多线程任务工具的方法
 *
 * @author KOISHI
 * Date:2024-10-28
 * Time:下午3:14
 */
public class TaskUtils {

    /**
     * 绑定任务程
     *
     * @param taskBean 绑定线程任务所需参数
     */
    public static void bindingTaskNode(TaskBean<?> taskBean) {
        bindingTaskNode(taskBean, false);
    }

    /**
     * 绑定任务程
     *
     * @param taskBean 绑定线程任务所需参数
     * @param disable  true 禁止逻辑为调用组件自身方法，false 调用自定义禁用方法
     */
    public static void bindingTaskNode(TaskBean<?> taskBean, boolean disable) {
        // 设置防重复点击按钮不可点击限制
        if (disable) {
            changeNodesDisable(taskBean, true);
        } else {
            changeDisableNodes(taskBean, true);
        }
        bindingTaskNodes(taskBean);
    }

    /**
     * 绑定任务程
     *
     * @param taskBean 绑定线程任务所需参数
     */
    public static void bindingTaskNodes(TaskBean<?> taskBean) {
        Task<?> task = taskBean.getWorkingTask();
        ProgressBar progressBar = taskBean.getProgressBar();
        if (progressBar != null) {
            // 绑定进度条的值属性
            progressBar.progressProperty().unbind();
            progressBar.setVisible(true);
            // 给进度条设置初始值
            progressBar.setProgress(0);
            Platform.runLater(() -> progressBar.progressProperty().bind(task.progressProperty()));
        }
        Label messageLabel = taskBean.getMessageLabel();
        if (messageLabel != null && taskBean.isBindingMessageLabel()) {
            // 绑定 TextField 的值属性
            messageLabel.textProperty().unbind();
            updateLabel(messageLabel, "");
            // 必须在 Platform.runLater 下绑定，不然可能无法更新文本
            Platform.runLater(() -> messageLabel.textProperty().bind(task.messageProperty()));
        }
        Button cancelButton = taskBean.getCancelButton();
        if (cancelButton != null) {
            setNodeDisable(cancelButton, false, tip_cancelButton());
            cancelButton.setVisible(true);
        }
        // 设置默认的异常处理
        setTaskCallBack(taskBean);
    }

    /**
     * 设置 Task 回调
     *
     * @param taskBean 线程任务所需参数
     * @throws RuntimeException 线程的异常
     */
    public static void setTaskCallBack(TaskBean<?> taskBean) {
        Task<?> task = taskBean.getWorkingTask();
        task.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            EventHandler<WorkerStateEvent> handler = taskBean.getOnSucceeded();
            try {
                if (handler != null) {
                    handler.handle(event);
                }
            } finally {
                taskBean.clearTask();
            }
        });
        task.setOnFailed(event -> {
            taskNotSuccess(taskBean, text_taskFailed());
            EventHandler<WorkerStateEvent> handler = taskBean.getOnFailed();
            try {
                if (handler != null) {
                    handler.handle(event);
                }
            } finally {
                taskBean.clearTask();
            }
            throw new RuntimeException(event.getSource().getException());
        });
        task.setOnCancelled(event -> {
            taskNotSuccess(taskBean, text_taskCancelled());
            EventHandler<WorkerStateEvent> handler = taskBean.getOnCancelled();
            try {
                if (handler != null) {
                    handler.handle(event);
                }
            } finally {
                taskBean.clearTask();
            }
        });
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
        Label messageLabel = taskBean.getMessageLabel();
        if (messageLabel != null) {
            messageLabel.textProperty().unbind();
        }
        // 隐藏和解绑进度条
        ProgressBar progressBar = taskBean.getProgressBar();
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.progressProperty().unbind();
        }
        Button cancelButton = taskBean.getCancelButton();
        if (cancelButton != null) {
            cancelButton.setVisible(false);
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
        showErrLabelText(taskBean.getMessageLabel(), log);
    }

    /**
     * 页面关闭时清理资源任务
     *
     * @param runnable 清理资源函数
     * @return 清理资源任务
     */
    public static Task<Void> clearResourcesTask(Runnable runnable) {
        return new Task<>() {
            @Override
            protected Void call() {
                if (runnable != null) {
                    runnable.run();
                }
                return null;
            }
        };
    }

    /**
     * 启动清理资源任务
     *
     * @param runnable 清理资源函数
     * @param tabId    功能 id
     */
    public static void startClearResourcesTask(Runnable runnable, String tabId) {
        Task<Void> clearResources = clearResourcesTask(runnable);
        clearResources.setOnSucceeded(_ -> System.gc());
        Thread.ofVirtual()
                .name("clearResourcesTask-vThread" + tabId)
                .start(clearResources);
    }

}
