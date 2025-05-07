package priv.koishi.pmc.Service;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Size;
import priv.koishi.pmc.Bean.FindPositionConfig;
import priv.koishi.pmc.Bean.MatchPoint;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static priv.koishi.pmc.Utils.FileUtils.getFileName;

/**
 * 图像识别工具类
 *
 * @author koishi
 * Date 2025/04/08
 * Time 10:08
 */
public class ImageRecognitionService {

    /**
     * 多尺度并行匹配缩放比例
     */
    private static final double[] scales = {0.8, 0.9, 1.0, 1.1, 1.2};

    /**
     * 获取屏幕宽度
     */
    public static int screenWidth;

    /**
     * 获取屏幕高度
     */
    public static int screenHeight;

    /**
     * 获取缩放比例
     */
    public static double dpiScale;

    /**
     * 屏幕参数刷新方法
     */
    public static void refreshScreenParameters() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;
        dpiScale = env.getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();
    }

    /**
     * 根据本地图片寻找屏幕坐标
     *
     * @param config 匹配配置
     * @return Javacv Point对象
     * @throws Exception 匹配失败时抛出异常
     */
    public static MatchPoint findPosition(FindPositionConfig config) throws Exception {
        double matchThreshold = config.getMatchThreshold();
        String templatePath = config.getTemplatePath();
        int overTime = config.getOverTime();
        File file = new File(templatePath);
        String fileName = getFileName(templatePath);
        if (StringUtils.isBlank(templatePath)) {
            throw new Exception("图片 " + fileName + " 路径为空");
        }
        if (!file.exists()) {
            throw new Exception("图片 " + fileName + " 不存在");
        }
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(config.getRetryWait())) {
            // 创建带名称的线程池（便于问题排查）
            try (ExecutorService executor = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "ImageRecognitionWorker");
                // 设置为守护线程
                t.setDaemon(true);
                return t;
            })) {
                try {
                    String timeOutErr = "图片 " + fileName + " 匹配超时";
                    if (config.isContinuously()) {
                        while (true) {
                            Future<MatchPoint> future = executor.submit(() ->
                                    getPoint(templatePath, matchThreshold));
                            try {
                                MatchPoint result;
                                if (overTime > 0) {
                                    result = future.get(overTime, TimeUnit.SECONDS);
                                } else {
                                    result = future.get();
                                }
                                if (result.getMatchThreshold() >= matchThreshold) {
                                    return result;
                                }
                            } catch (TimeoutException e) {
                                // 中断当前识别
                                future.cancel(true);
                                throw new Exception(timeOutErr);
                            }
                            // 等待重试间隔（不计算在超时时间内）
                            scheduler.schedule(() -> {
                            }, config.getRetryWait(), TimeUnit.SECONDS).get();
                        }
                    } else {
                        MatchPoint result = new MatchPoint();
                        for (int i = 0; i <= config.getMaxRetry(); i++) {
                            Future<MatchPoint> future = executor.submit(() ->
                                    getPoint(templatePath, matchThreshold));
                            try {
                                if (overTime > 0) {
                                    result = future.get(overTime, TimeUnit.SECONDS);
                                } else {
                                    result = future.get();
                                }
                                if (result.getMatchThreshold() >= matchThreshold) {
                                    return result;
                                }
                            } catch (TimeoutException e) {
                                future.cancel(true);
                                throw new Exception(timeOutErr);
                            }
                            if (i < config.getMaxRetry()) {
                                // 等待重试间隔（不计算在超时时间内）
                                scheduler.schedule(() -> {
                                }, config.getRetryWait(), TimeUnit.SECONDS).get();
                            }
                        }
                        return result;
                    }
                } finally {
                    executor.shutdownNow();
                }
            } finally {
                scheduler.shutdownNow();
            }
        }
    }

    /**
     * 根据本地图片寻找屏幕坐标
     *
     * @param templatePath   要识别的图片路径
     * @param matchThreshold 匹配阈值
     * @return Javacv Point对象
     * @throws Exception 匹配失败时抛出异常
     */
    private static MatchPoint getPoint(String templatePath, double matchThreshold) throws Exception {
        checkInterruption();
        // 获取屏幕当前画面
        BufferedImage screenImg;
        try {
            screenImg = new Robot().createScreenCapture(new Rectangle((int) (screenWidth * dpiScale), (int) (screenHeight * dpiScale)));
        } catch (AWTException e) {
            throw new Exception("屏幕图像获取失败: " + e.getMessage(), e);
        }
        checkInterruption();
        // 初始化匹配结果存储变量
        MatchPoint matchPoint = new MatchPoint();
        AtomicReference<Double> bestVal = new AtomicReference<>(-1.0);
        AtomicReference<Point> bestLocRef = new AtomicReference<>(new Point(0, 0));
        try (Mat screenMat = bufferedImageToMat(screenImg);
             Mat templateMat = imread(templatePath, IMREAD_COLOR)) {
            // 转换为灰度图提升处理效率
            try (Mat screenGray = new Mat(); Mat templateGray = new Mat()) {
                cvtColor(screenMat, screenGray, COLOR_BGR2GRAY);
                cvtColor(templateMat, templateGray, COLOR_BGR2GRAY);
                // DPI缩放补偿：根据系统缩放反向调整模板尺寸
                try (Mat dpiAdjustedTemplate = new Mat()) {
                    resize(templateGray, dpiAdjustedTemplate,
                            new Size((int) (templateGray.cols() / dpiScale),
                                    (int) (templateGray.rows() / dpiScale)));
                    // 多尺度并行匹配：在不同缩放比例下寻找最佳匹配
                    Arrays.stream(scales).parallel().forEach(scale -> {
                        try (Mat resizedTemplate = new Mat(); Mat result = new Mat()) {
                            checkInterruption();
                            resize(dpiAdjustedTemplate, resizedTemplate,
                                    new Size((int) (dpiAdjustedTemplate.cols() * scale),
                                            (int) (dpiAdjustedTemplate.rows() * scale)));
                            // 执行模板匹配并获取最大匹配值位置
                            matchTemplate(screenGray, resizedTemplate, result, TM_CCOEFF_NORMED);
                            try (Point maxLoc = new Point()) {
                                DoublePointer maxVal = new DoublePointer(1);
                                minMaxLoc(result, null, maxVal, null, maxLoc, null);
                                // 线程安全地更新最佳匹配结果
                                synchronized (bestLocRef) {
                                    if (maxVal.get() > bestVal.get()) {
                                        bestVal.set(maxVal.get());
                                        double templateWidth = resizedTemplate.cols();
                                        double templateHeight = resizedTemplate.rows();
                                        int x = (int) Math.round((maxLoc.x() + templateWidth / 2.0) / scale);
                                        int y = (int) Math.round((maxLoc.y() + templateHeight / 2.0) / scale);
                                        x = Math.min(Math.max(x, 0), screenWidth);
                                        y = Math.min(Math.max(y, 0), screenHeight);
                                        bestLocRef.set(new Point(x, y));
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            matchPoint.setPoint(bestLocRef.get())
                    .setMatchThreshold((int) (bestVal.get() * 100));
            // 匹配成功返回匹配坐标和匹配度，否则只返回匹配度
            if (bestVal.get() >= matchThreshold / 100) {
                return matchPoint;
            }
        }
        return matchPoint;
    }

    /**
     * BufferedImage转Mat
     *
     * @param image 需要转换的图片
     * @return Mat对象
     */
    private static Mat bufferedImageToMat(BufferedImage image) {
        // 转换为标准的3通道BGR格式
        BufferedImage converted = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR
        );
        Graphics2D g = converted.createGraphics();
        try {
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        // 转换时数据类型为CV_8U
        try (Java2DFrameConverter frameConverter = new Java2DFrameConverter();
             OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat()) {
            Frame frame = frameConverter.convert(converted);
            return matConverter.convertToMat(frame).clone();
        }
    }

    /**
     * 中断检查
     *
     * @throws InterruptedException 操作被用户取消
     */
    private static void checkInterruption() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("操作被用户取消");
        }
    }

    /**
     * 检查是否能正常截图
     *
     * @return true: 可以截图，false: 无法截图
     */
    public static boolean checkScreenCapturePermission() {
        refreshScreenParameters();
        try {
            new Robot().createScreenCapture(new Rectangle((int) (screenWidth * dpiScale), (int) (screenHeight * dpiScale)));
            return true;
        } catch (SecurityException | AWTException e) {
            return false;
        }
    }

}
