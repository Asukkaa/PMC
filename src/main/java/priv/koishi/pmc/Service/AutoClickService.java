package priv.koishi.pmc.Service;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import priv.koishi.pmc.Bean.AutoClickTaskBean;
import priv.koishi.pmc.Bean.ClickPositionBean;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static javafx.scene.input.MouseButton.NONE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.UiUtils.changeDisableControls;

/**
 * 自动点击线程任务类
 *
 * @author KOISHI
 * Date:2025-02-18
 * Time:14:42
 */
public class AutoClickService {

    /**
     * 自动点击
     *
     * @param taskBean 线程任务参数
     */
    public static Task<Void> autoClick(AutoClickTaskBean taskBean, Robot robot) {
        return new Task<>() {
            @Override
            protected Void call() {
                // 改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                Timeline timeline = taskBean.getRunTimeline();
                if (timeline != null) {
                    timeline.stop();
                }
                List<ClickPositionBean> tableViewItems = taskBean.getBeanList();
                Label floatingLabel = taskBean.getFloatingLabel();
                // 执行自动流程前点击第一个起始坐标
                if (taskBean.isFirstClick()) {
                    ClickPositionBean clickPositionBean = tableViewItems.getFirst();
                    double x = Double.parseDouble(clickPositionBean.getStartX());
                    double y = Double.parseDouble(clickPositionBean.getStartY());
                    Platform.runLater(() -> {
                        robot.mouseMove(x, y);
                        robot.mousePress(PRIMARY);
                        robot.mouseRelease(PRIMARY);
                        updateMessage(text_changeWindow);
                        floatingLabel.setText(text_cancelTask + text_changeWindow);
                    });
                }
                int loopTime = taskBean.getLoopTime();
                if (loopTime == 0) {
                    int i = 0;
                    while (!isCancelled()) {
                        i++;
                        String loopTimeText = text_execution + i + " / ∞" + text_executionTime;
                        if (isCancelled()) {
                            break;
                        }
                        // 执行点击任务
                        clicks(tableViewItems, loopTimeText);
                    }
                } else {
                    for (int i = 0; i < loopTime && !isCancelled(); i++) {
                        String loopTimeText = text_execution + (i + 1) + " / " + loopTime + text_executionTime;
                        if (isCancelled()) {
                            break;
                        }
                        // 执行点击任务
                        clicks(tableViewItems, loopTimeText);
                    }
                }
                return null;
            }

            // 执行点击任务
            private void clicks(List<ClickPositionBean> tableViewItems, String loopTimeText) {
                int dataSize = tableViewItems.size();
                Label floatingLabel = taskBean.getFloatingLabel();
                updateProgress(0, dataSize);
                for (int j = 0; j < dataSize; j++) {
                    updateProgress(j + 1, dataSize);
                    ClickPositionBean clickPositionBean = tableViewItems.get(j);
                    double startX = Double.parseDouble(clickPositionBean.getStartX());
                    double startY = Double.parseDouble(clickPositionBean.getStartY());
                    double endX = Double.parseDouble(clickPositionBean.getEndX());
                    double endY = Double.parseDouble(clickPositionBean.getEndY());
                    String waitTime = clickPositionBean.getWaitTime();
                    String clickTime = clickPositionBean.getClickTime();
                    String name = clickPositionBean.getName();
                    String clickNum = clickPositionBean.getClickNum();
                    Platform.runLater(() -> {
                        String text = loopTimeText + waitTime + " 毫秒后将执行: " + name +
                                "\n操作内容：" + clickPositionBean.getType() + " X：" + startX + " Y：" + startY + " 在 " +
                                clickTime + " 毫秒内移动到 X：" + endX + " Y：" + endY +
                                "\n每次操作间隔：" + clickPositionBean.getClickInterval() + " 毫秒，共 " + clickNum + " 次";
                        updateMessage(text);
                        floatingLabel.setText(text_cancelTask + text);
                    });
                    // 执行前等待时间
                    try {
                        Thread.sleep(Long.parseLong(waitTime));
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            break;
                        }
                    }
                    // 执行自动流程
                    click(clickPositionBean, robot);
                }
            }
        };
    }

    /**
     * 按照操作设置执行操作
     *
     * @param clickPositionBean 操作设置
     * @param robot             Robot实例
     */
    private static void click(ClickPositionBean clickPositionBean, Robot robot) {
        // 操作次数
        int clickNum = Integer.parseInt(clickPositionBean.getClickNum());
        double startX = Double.parseDouble(clickPositionBean.getStartX());
        double startY = Double.parseDouble(clickPositionBean.getStartY());
        double endX = Double.parseDouble(clickPositionBean.getEndX());
        double endY = Double.parseDouble(clickPositionBean.getEndY());
        long clickTime = Long.parseLong(clickPositionBean.getClickTime());
        long clickInterval = Long.parseLong(clickPositionBean.getClickInterval());
        // 按照操作次数执行
        for (int i = 0; i < clickNum; i++) {
            // 每次操作的间隔时间
            if (i > 0) {
                try {
                    Thread.sleep(clickInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            MouseButton mouseButton = clickTypeMap.get(clickPositionBean.getType());
            Platform.runLater(() -> {
                robot.mouseMove(startX, startY);
                if (mouseButton != NONE) {
                    robot.mousePress(mouseButton);
                }
            });
            // 计算鼠标移动的轨迹
            double deltaX = endX - startX;
            double deltaY = endY - startY;
            int steps = 10;
            long stepDuration = clickTime / steps;
            for (int j = 1; j <= steps; j++) {
                double x = startX + deltaX * j / steps;
                double y = startY + deltaY * j / steps;
                CompletableFuture<Void> moveFuture = new CompletableFuture<>();
                Platform.runLater(() -> {
                    robot.mouseMove(x, y);
                    moveFuture.complete(null);
                });
                // 等待任务完成
                try {
                    moveFuture.get();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                // 单次操作时间
                try {
                    Thread.sleep(stepDuration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            CompletableFuture<Void> releaseFuture = new CompletableFuture<>();
            Platform.runLater(() -> {
                if (mouseButton != NONE) {
                    robot.mouseRelease(mouseButton);
                }
                releaseFuture.complete(null);
            });
            // 等待任务完成
            try {
                releaseFuture.get();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
