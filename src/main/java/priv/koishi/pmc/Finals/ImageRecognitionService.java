package priv.koishi.pmc.Finals;

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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * 图像识别工具类
 *
 * @author koishi
 * Date 2022/04/08
 * Time 10:08:04
 */
public class ImageRecognitionService {

    /**
     * 多尺度并行匹配缩放比例
     */
    private static final double[] scales = {0.8, 0.9, 1.0, 1.1, 1.2};

    /**
     * 获取屏幕宽度
     */
    private static int screenWidth;

    /**
     * 获取屏幕高度
     */
    private static int screenHeight;

    /**
     * 获取缩放比例
     */
    private static double dpiScale;

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
        MatchPoint bestLoc = new MatchPoint();
        double matchThreshold = config.getMatchThreshold();
        String templatePath = config.getTemplatePath();
        if (config.isContinuously()) {
            while (true) {
                bestLoc = getPoint(templatePath, matchThreshold);
                if (bestLoc.getPoint() != null) {
                    return bestLoc;
                }
            }
        } else {
            for (int i = config.getMaxRetry() + 1; i > 0; i--) {
                bestLoc = getPoint(templatePath, matchThreshold);
                if (bestLoc.getPoint() != null) {
                    return bestLoc;
                }
                // 匹配失败后等待指定事件再重试
                Thread.sleep(config.getRetryWait());
            }
            return bestLoc;
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
        // 获取屏幕当前画面
        BufferedImage screenImg;
        try {
            screenImg = new Robot().createScreenCapture(
                    new Rectangle((int) (screenWidth * dpiScale), (int) (screenHeight * dpiScale)));
        } catch (AWTException e) {
            throw new Exception("屏幕图像获取失败: " + e.getMessage(), e);
        }
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
                        }
                    });
                }
            }
            // 匹配成功返回匹配坐标和匹配度，否则只返回匹配度
            if (bestVal.get() >= matchThreshold / 100) {
                matchPoint.setPoint(bestLocRef.get())
                        .setMatchThreshold((int) (bestVal.get() * 100));
                return matchPoint;
            }
        }
        matchPoint.setMatchThreshold((int) (bestVal.get() * 100));
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

}
