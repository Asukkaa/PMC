package priv.koishi.pmc.Service;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.Point;
import priv.koishi.pmc.Bean.AutoClickTaskBean;
import priv.koishi.pmc.Bean.FindPositionConfig;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.MatchPoint;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static javafx.scene.input.MouseButton.NONE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.ImageRecognitionService.findPosition;
import static priv.koishi.pmc.Utils.FileUtils.getExistsFileName;
import static priv.koishi.pmc.Utils.UiUtils.setDefaultIntValue;

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
            protected Void call() throws Exception {
                Timeline timeline = taskBean.getRunTimeline();
                if (timeline != null) {
                    timeline.stop();
                }
                List<ClickPositionVO> tableViewItems = taskBean.getBeanList();
                Label floatingLabel = taskBean.getFloatingLabel();
                // 执行自动流程前点击第一个起始坐标
                if (taskBean.isFirstClick()) {
                    ClickPositionVO clickPositionVO = tableViewItems.getFirst();
                    double x = Double.parseDouble(clickPositionVO.getStartX());
                    double y = Double.parseDouble(clickPositionVO.getStartY());
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
            private void clicks(List<ClickPositionVO> tableViewItems, String loopTimeText) throws Exception {
                int dataSize = tableViewItems.size();
                Label floatingLabel = taskBean.getFloatingLabel();
                updateProgress(0, dataSize);
                for (int j = 0; j < dataSize; j++) {
                    updateProgress(j + 1, dataSize);
                    ClickPositionVO clickPositionVO = tableViewItems.get(j);
                    int startX = Integer.parseInt((clickPositionVO.getStartX()));
                    int startY = Integer.parseInt((clickPositionVO.getStartY()));
                    int endX = Integer.parseInt((clickPositionVO.getEndX()));
                    int endY = Integer.parseInt((clickPositionVO.getEndY()));
                    String waitTime = clickPositionVO.getWaitTime();
                    String clickTime = clickPositionVO.getClickTime();
                    String name = clickPositionVO.getName();
                    int clickNum = Integer.parseInt(clickPositionVO.getClickNum()) - 1;
                    Platform.runLater(() -> {
                        String text = loopTimeText + waitTime + " 毫秒后将执行: " + name +
                                "\n操作内容：" + clickPositionVO.getType() + " X：" + startX + " Y：" + startY +
                                "\n在 " + clickTime + " 毫秒内移动到 X：" + endX + " Y：" + endY +
                                "\n重复 " + clickNum + " 次，每次操作间隔：" + clickPositionVO.getClickInterval() + " 毫秒";
                        if (StringUtils.isNotBlank(clickPositionVO.getClickImgPath())) {
                            try {
                                text = loopTimeText + waitTime + " 毫秒后将执行: " + name +
                                        "\n操作内容：" + clickPositionVO.getType() + " 要识别的图片：" +
                                        "\n" + getExistsFileName(new File(clickPositionVO.getClickImgPath())) +
                                        "\n单次点击" + clickTime + " 毫秒" +
                                        "\n重复 " + clickNum + " 次，每次操作间隔：" + clickPositionVO.getClickInterval() + " 毫秒";
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
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
                    click(clickPositionVO, robot, floatingLabel, loopTimeText);
                }
            }
        };
    }

    /**
     * 按照操作设置执行操作
     *
     * @param clickPositionVO 操作设置
     * @param robot             Robot实例
     */
    private static void click(ClickPositionVO clickPositionVO, Robot robot, Label floatingLabel, String loopTimeText) throws Exception {
        // 操作次数
        int clickNum = Integer.parseInt(clickPositionVO.getClickNum());
        double startX = Double.parseDouble(clickPositionVO.getStartX());
        double startY = Double.parseDouble(clickPositionVO.getStartY());
        double endX = Double.parseDouble(clickPositionVO.getEndX());
        double endY = Double.parseDouble(clickPositionVO.getEndY());
        TextField retrySecond = (TextField) clickPositionVO.getTableView().getScene().lookup("#retrySecond_Set");
        int retrySecondValue = setDefaultIntValue(retrySecond, 1, 0, null);
        TextField overTime = (TextField) clickPositionVO.getTableView().getScene().lookup("#overtime_Set");
        int overTimeValue = setDefaultIntValue(overTime, 0, 1, null);
        // 匹配终止操作图像
        List<ImgFileBean> stopImgFileVOS = clickPositionVO.getStopImgFiles();
        if (CollectionUtils.isNotEmpty(stopImgFileVOS)) {
            stopImgFileVOS.stream().parallel().forEach(stopImgFileBean -> {
                String stopPath = stopImgFileBean.getPath();
                Platform.runLater(() -> {
                    try {
                        floatingLabel.setText(text_cancelTask + loopTimeText +
                                "\n正在识别终止操作图像：\n" + getExistsFileName(new File(stopPath)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                FindPositionConfig findPositionConfig = new FindPositionConfig();
                findPositionConfig.setMatchThreshold(Double.parseDouble(clickPositionVO.getStopMatchThreshold()))
                        .setMaxRetry(Integer.parseInt(clickPositionVO.getStopRetryTimes()))
                        .setRetryWait(retrySecondValue)
                        .setOverTime(overTimeValue)
                        .setTemplatePath(stopPath)
                        .setContinuously(false);
                try (Point position = findPosition(findPositionConfig).getPoint()) {
                    if (position != null) {
                        throw new Exception("匹配到终止操作图片，操作已终止");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // 匹配要点击的图像
        String clickPath = clickPositionVO.getClickImgPath();
        if (StringUtils.isNotBlank(clickPath)) {
            Platform.runLater(() -> {
                try {
                    floatingLabel.setText(text_cancelTask + loopTimeText +
                            "\n正在识别要点击的图像：\n" + getExistsFileName(new File(clickPath)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            String retryType = clickPositionVO.getRetryType();
            FindPositionConfig findPositionConfig = new FindPositionConfig();
            findPositionConfig.setMatchThreshold(Double.parseDouble(clickPositionVO.getClickMatchThreshold()))
                    .setMaxRetry(Integer.parseInt(clickPositionVO.getClickRetryTimes()))
                    .setContinuously(retryType_continuously.equals(retryType))
                    .setRetryWait(retrySecondValue)
                    .setOverTime(overTimeValue)
                    .setTemplatePath(clickPath);
            MatchPoint matchPoint = findPosition(findPositionConfig);
            try (Point position = matchPoint.getPoint()) {
                if (position != null) {
                    // 匹配成功后跳过操作
                    if (clickPositionVO.isSkip()) {
                        return;
                    }
                    startX = position.x();
                    startY = position.y();
                    endX = position.x();
                    endY = position.y();
                } else if (retryType_stop.equals(retryType)) {
                    throw new Exception("未找到匹配图像，超过最大重试次数，最后一次匹配度为：" + matchPoint.getMatchThreshold() + " %");
                }
            }
        }
        long clickTime = Long.parseLong(clickPositionVO.getClickTime());
        long clickInterval = Long.parseLong(clickPositionVO.getClickInterval());
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
            MouseButton mouseButton = runClickTypeMap.get(clickPositionVO.getType());
            double finalStartX = startX;
            double finalStartY = startY;
            Platform.runLater(() -> {
                robot.mouseMove(finalStartX, finalStartY);
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
