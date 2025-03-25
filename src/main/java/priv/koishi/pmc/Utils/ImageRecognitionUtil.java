package priv.koishi.pmc.Utils;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Size;

import java.awt.*;
import java.awt.image.BufferedImage;

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
public class ImageRecognitionUtil {

    /**
     * 根据本地图片寻找屏幕坐标
     *
     * @param templatePath   要识别的图片路径
     * @param maxRetry       最大重试次数
     * @param matchThreshold 匹配阈值
     * @param continuously   是否持续匹配
     * @return Javacv Point对象
     * @throws Exception 匹配失败时抛出异常
     */
    public static Point findPosition(String templatePath, int maxRetry, double matchThreshold, boolean continuously) throws Exception {
        if (continuously) {
            while (true) {
                Point bestLoc = getPoint(templatePath, matchThreshold);
                if (bestLoc != null) {
                    return bestLoc;
                }
            }
        } else {
            for (int i = maxRetry + 1; i > 0; i--) {
                Point bestLoc = getPoint(templatePath, matchThreshold);
                if (bestLoc != null) {
                    return bestLoc;
                }
            }
        }
        return null;
    }

    /**
     * 根据本地图片寻找屏幕坐标
     *
     * @param templatePath   要识别的图片路径
     * @param matchThreshold 匹配阈值
     * @return Javacv Point对象
     * @throws Exception 匹配失败时抛出异常
     */
    private static Point getPoint(String templatePath, double matchThreshold) throws Exception {
        // 获取屏幕参数
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        // 获取DPI缩放比例
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        double dpiScale = env.getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();
        // 截取屏幕时物理像素尺寸（考虑DPI缩放）
        BufferedImage screenImg = new Robot().createScreenCapture(
                new Rectangle((int) (screenWidth * dpiScale), (int) (screenHeight * dpiScale)));
        try (Mat screenMat = bufferedImageToMat(screenImg);
             Mat templateMat = imread(templatePath, IMREAD_COLOR)) {
            // 灰度预处理
            Mat screenGray = new Mat(), templateGray = new Mat();
            cvtColor(screenMat, screenGray, COLOR_BGR2GRAY);
            cvtColor(templateMat, templateGray, COLOR_BGR2GRAY);
            // 步骤1：DPI缩放补偿（仅执行一次）
            Mat dpiAdjustedTemplate = new Mat();
            resize(templateGray, dpiAdjustedTemplate,
                    new Size((int) (templateGray.cols() / dpiScale),
                            (int) (templateGray.rows() / dpiScale)));
            // 步骤2：多尺度匹配（基于DPI调整后的模板）
            double bestVal = -1;
            Point bestLoc = new Point(0, 0);
            double[] scales = {0.8, 0.9, 1.0, 1.1, 1.2};
            for (double scale : scales) {
                Mat resizedTemplate = new Mat();
                resize(dpiAdjustedTemplate, resizedTemplate,
                        new Size((int) (dpiAdjustedTemplate.cols() * scale),
                                (int) (dpiAdjustedTemplate.rows() * scale)));
                Mat result = new Mat();
                matchTemplate(screenGray, resizedTemplate, result, TM_CCOEFF_NORMED);
                try (Point maxLoc = new Point()) {
                    DoublePointer maxVal = new DoublePointer(1);
                    minMaxLoc(result, null, maxVal, null, maxLoc, null);
                    if (maxVal.get() > bestVal) {
                        bestVal = maxVal.get();
                        // 换算公式：物理坐标 = (匹配坐标 + 模板宽度/2) / scale
                        int x = (int) ((maxLoc.x() + (double) resizedTemplate.cols() / 2) / scale);
                        int y = (int) ((maxLoc.y() + (double) resizedTemplate.rows() / 2) / scale);
                        // 坐标边界检查
                        x = Math.min(Math.max(x, 0), screenWidth);
                        y = Math.min(Math.max(y, 0), screenHeight);
                        bestLoc.x(x);
                        bestLoc.y(y);
                    }
                }
            }
            if (bestVal >= matchThreshold / 100) {
                return bestLoc;
            }
            // 匹配度不足时等待1秒
            Thread.sleep(1000);
        }
        return null;
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
        g.drawImage(image, 0, 0, null);
        g.dispose();
        // 转换时数据类型为CV_8U
        try (Java2DFrameConverter frameConverter = new Java2DFrameConverter();
             OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat()) {
            Frame frame = frameConverter.convert(converted);
            return matConverter.convertToMat(frame).clone();
        }
    }

}
