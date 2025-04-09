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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.MouseButton.NONE;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Service.ImageRecognitionService.findPosition;
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
     * 执行自动流程前点击第一个起始坐标标识
     */
    private static final AtomicBoolean firstClick = new AtomicBoolean();

    /**
     * 浮窗信息栏
     */
    private static Label floatingLabel;

    /**
     * 程序界面信息栏
     */
    private static Label massageLabel;

    /**
     * 自动点击任务线程
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
                int loopTime = taskBean.getLoopTime();
                if (loopTime == 0) {
                    int i = 0;
                    while (!isCancelled()) {
                        i++;
                        String loopTimeText = text_cancelTask + text_execution + i + " / ∞" + text_executionTime;
                        if (isCancelled()) {
                            break;
                        }
                        // 执行操作流程
                        clicks(tableViewItems, loopTimeText);
                    }
                } else {
                    for (int i = 0; i < loopTime && !isCancelled(); i++) {
                        String loopTimeText = text_cancelTask + text_execution + (i + 1) + " / " + loopTime + text_executionTime;
                        if (isCancelled()) {
                            break;
                        }
                        // 执行操作流程
                        clicks(tableViewItems, loopTimeText);
                    }
                }
                clearReferences();
                return null;
            }

            // 执行操作流程
            private void clicks(List<ClickPositionVO> tableViewItems, String loopTimeText) throws Exception {
                int dataSize = tableViewItems.size();
                floatingLabel = taskBean.getFloatingLabel();
                massageLabel = taskBean.getMassageLabel();
                firstClick.set(taskBean.isFirstClick());
                updateProgress(0, dataSize);
                int currentStep = 0;
                while (currentStep < dataSize) {
                    int progress = currentStep + 1;
                    updateProgress(progress, dataSize);
                    ClickPositionVO clickPositionVO = tableViewItems.get(currentStep);
                    int startX = Integer.parseInt((clickPositionVO.getStartX()));
                    int startY = Integer.parseInt((clickPositionVO.getStartY()));
                    int endX = Integer.parseInt((clickPositionVO.getEndX()));
                    int endY = Integer.parseInt((clickPositionVO.getEndY()));
                    String waitTime = clickPositionVO.getWaitTime();
                    String clickTime = clickPositionVO.getClickTime();
                    String name = clickPositionVO.getName();
                    String clickType = clickPositionVO.getClickType();
                    String interval = clickPositionVO.getClickInterval();
                    String clickImgPath = clickPositionVO.getClickImgPath();
                    int clickNum = Integer.parseInt(clickPositionVO.getClickNum()) - 1;
                    Platform.runLater(() -> {
                        String text = loopTimeText +
                                "\n本轮进度：" + progress + "/" + dataSize +
                                "\n将在 " + waitTime + " 毫秒后将执行: " + name +
                                "\n操作内容：" + clickType + " X：" + startX + " Y：" + startY +
                                "\n在 " + clickTime + " 毫秒内移动到 X：" + endX + " Y：" + endY +
                                "\n重复 " + clickNum + " 次，每次操作间隔：" + interval + " 毫秒";
                        if (StringUtils.isNotBlank(clickImgPath)) {
                            try {
                                text = loopTimeText +
                                        "\n本轮进度：" + progress + "/" + dataSize +
                                        "\n将在 " + waitTime + " 毫秒后将执行: " + name +
                                        "\n操作内容：" + clickType + " 要识别的图片：" +
                                        "\n" + getExistsFileName(new File(clickImgPath)) +
                                        "\n单次点击" + clickTime + " 毫秒" +
                                        "\n重复 " + clickNum + " 次，每次操作间隔：" + interval + " 毫秒";
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        massageLabel.setText(text);
                        floatingLabel.setText(text);
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
                    int stepIndex = click(clickPositionVO, robot, loopTimeText);
                    // 跳转到指定步骤
                    if (stepIndex > 0) {
                        currentStep = stepIndex - 1;
                        continue;
                    }
                    currentStep++;
                }
            }
        };
    }

    /**
     * 按照操作设置执行操作
     *
     * @param clickPositionVO 操作设置
     * @param robot           Robot实例
     * @return 跳转的步骤索引，0为不跳转
     */
    private static int click(ClickPositionVO clickPositionVO, Robot robot, String loopTimeText) throws Exception {
        int gotoStep = 0;
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
                AtomicReference<String> fileName = new AtomicReference<>();
                Platform.runLater(() -> {
                    try {
                        fileName.set(getExistsFileName(new File(stopPath)));
                        String text = loopTimeText + "\n正在识别终止操作图像：\n" + fileName.get();
                        floatingLabel.setText(text);
                        massageLabel.setText(text);
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
                MatchPoint matchPoint;
                try {
                    matchPoint = findPosition(findPositionConfig);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try (Point position = matchPoint.getPoint()) {
                    if (matchPoint.getMatchThreshold() >= findPositionConfig.getMatchThreshold()) {
                        throw new Exception("执行到序号为：" + clickPositionVO.getIndex() + " 的步骤时终止操作" +
                                "\n匹配到终止操作图像：" + fileName.get() +
                                "\n匹配度为：" + matchPoint.getMatchThreshold() + " %" +
                                "\n坐标 X：" + position.x() + " Y：" + position.y());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // 匹配要点击的图像
        String clickPath = clickPositionVO.getClickImgPath();
        AtomicReference<String> fileName = new AtomicReference<>();
        if (StringUtils.isNotBlank(clickPath)) {
            Platform.runLater(() -> {
                try {
                    fileName.set(getExistsFileName(new File(clickPath)));
                    String text = loopTimeText + "\n正在识别目标图像：\n" + fileName.get();
                    floatingLabel.setText(text);
                    massageLabel.setText(text);
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
                if (matchPoint.getMatchThreshold() >= findPositionConfig.getMatchThreshold()) {
                    String matchedType = clickPositionVO.getMatchedType();
                    // 匹配成功后跳过操作
                    if (clickMatched_break.equals(matchedType)) {
                        return gotoStep;
                        // 匹配成功后执行指定步骤
                    } else if (clickMatched_Step.equals(matchedType)) {
                        return Integer.parseInt(clickPositionVO.getMatchedStep());
                        // 匹配成功后点击匹配图像并执行指定步骤
                    } else if (clickMatched_ClickStep.equals(matchedType)) {
                        gotoStep = Integer.parseInt(clickPositionVO.getMatchedStep());
                    }
                    startX = position.x();
                    startY = position.y();
                    endX = position.x();
                    endY = position.y();
                    // 匹配失败后终止操作
                } else if (retryType_stop.equals(retryType)) {
                    try {
                        throw new Exception("执行到序号为：" + clickPositionVO.getIndex() + " 的步骤时发生异常" +
                                "\n已重试最大重试次数：" + clickPositionVO.getClickRetryTimes() + " 次" +
                                "\n未找到匹配图像：" + fileName.get() +
                                "\n最接近的图像匹配度为：" + matchPoint.getMatchThreshold() + " %" +
                                "\n坐标 X：" + position.x() + " Y：" + position.y());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    // 匹配失败后跳过本次操作
                } else if (retryType_break.equals(retryType)) {
                    return gotoStep;
                    // 匹配失败后执行指定步骤
                } else if (retryType_Step.equals(retryType)) {
                    return Integer.parseInt(clickPositionVO.getRetryStep());
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
            MouseButton mouseButton = runClickTypeMap.get(clickPositionVO.getClickType());
            double finalStartX = startX;
            double finalStartY = startY;
            Platform.runLater(() -> {
                robot.mouseMove(finalStartX, finalStartY);
                // 执行自动流程前点击第一个起始坐标
                if (firstClick.compareAndSet(true, false)) {
                    robot.mousePress(mouseButton);
                    robot.mouseRelease(mouseButton);
                }
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
        return gotoStep;
    }

    /**
     * 解除组件引用
     */
    public static void clearReferences() {
        floatingLabel = null;
        massageLabel = null;
    }

}
