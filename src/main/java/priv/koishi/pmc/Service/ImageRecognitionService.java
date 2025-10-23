package priv.koishi.pmc.Service;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Size;
import priv.koishi.pmc.Bean.ClickLogBean;
import priv.koishi.pmc.Bean.Config.FindPositionConfig;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.MatchPointBean;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.Queue.DynamicQueue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Finals.CommonFinals.activation;
import static priv.koishi.pmc.Finals.CommonFinals.percentage;
import static priv.koishi.pmc.Finals.i18nFinal.*;
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
     * @return Javacv Point 对象
     * @throws Exception 匹配失败时抛出异常
     */
    public static MatchPointBean findPosition(FindPositionConfig config, DynamicQueue<? super ClickLogBean> dynamicQueue) throws Exception {
        double matchThreshold = config.getMatchThreshold();
        String templatePath = config.getTemplatePath();
        int overTime = config.getOverTime();
        String name = config.getName();
        File file = new File(templatePath);
        String fileName = getFileName(templatePath);
        if (StringUtils.isBlank(templatePath)) {
            throw new RuntimeException(text_image() + fileName + text_nullPath());
        }
        if (!file.exists()) {
            throw new RuntimeException(text_image() + fileName + text_noExists());
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
                    String timeOutErr = text_image() + fileName + text_timeOut();
                    if (config.isContinuously()) {
                        int findTime = 0;
                        // 重试直到匹配
                        while (true) {
                            long start = System.currentTimeMillis();
                            findTime++;
                            config.setFindTime(findTime);
                            Future<MatchPointBean> future = executor.submit(() ->
                                    getPoint(config));
                            try {
                                MatchPointBean result = (overTime > 0) ?
                                        future.get(overTime, TimeUnit.SECONDS) :
                                        future.get();
                                // 记录识别结果
                                addLog(result, findTime, start, name, dynamicQueue);
                                if (result.getMatchThreshold() >= matchThreshold) {
                                    return result;
                                }
                            } catch (TimeoutException e) {
                                // 中断当前识别
                                future.cancel(true);
                                throw new RuntimeException(timeOutErr);
                            }
                            // 等待重试间隔（不计算在超时时间内）
                            scheduler.schedule(() -> {
                            }, config.getRetryWait(), TimeUnit.SECONDS).get();
                        }
                    } else {
                        MatchPointBean result = new MatchPointBean();
                        // 按规定次数重试
                        for (int i = 0; i <= config.getMaxRetry(); i++) {
                            long start = System.currentTimeMillis();
                            int findTime = i + 1;
                            config.setFindTime(findTime);
                            Future<MatchPointBean> future = executor.submit(() ->
                                    getPoint(config));
                            try {
                                result = (overTime > 0) ?
                                        future.get(overTime, TimeUnit.SECONDS) :
                                        future.get();
                                // 记录识别结果
                                addLog(result, findTime, start, name, dynamicQueue);
                                if (result.getMatchThreshold() >= matchThreshold) {
                                    return result;
                                }
                            } catch (TimeoutException e) {
                                future.cancel(true);
                                throw new RuntimeException(timeOutErr);
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
     * 记录识别结果
     *
     * @param result       识别结果
     * @param retry        重试次数
     * @param start        识别开始时间
     * @param name         操作名称
     * @param dynamicQueue 日志队列
     */
    private static void addLog(MatchPointBean result, int retry, long start, String name, DynamicQueue<? super ClickLogBean> dynamicQueue) {
        if (settingController.imgLog_Set.isSelected() && dynamicQueue != null) {
            long end = System.currentTimeMillis();
            ClickLogBean clickLogBean = new ClickLogBean();
            clickLogBean.setResult(result.getMatchThreshold() + percentage)
                    .setType(log_findImage() + " - " + retry)
                    .setX(String.valueOf(result.getPoint().x()))
                    .setY(String.valueOf(result.getPoint().y()))
                    .setClickTime(String.valueOf(end - start))
                    .setName(name);
            dynamicQueue.add(clickLogBean);
        }
    }

    /**
     * 根据本地图片寻找屏幕坐标
     *
     * @param findPositionConfig 匹配配置
     * @return Javacv Point 对象
     * @throws Exception 匹配失败时抛出异常
     */
    private static MatchPointBean getPoint(FindPositionConfig findPositionConfig) throws Exception {
        checkInterruption();
        // 获取屏幕当前画面
        BufferedImage screenImg;
        FloatingWindowConfig config = findPositionConfig.getFloatingWindowConfig();
        int x;
        int y;
        int w;
        int h;
        if (FindImgTypeEnum.ALL.ordinal() == config.getFindImgTypeEnum()) {
            x = 0;
            y = 0;
            w = screenWidth;
            h = screenHeight;
        } else if (FindImgTypeEnum.WINDOW.ordinal() == config.getFindImgTypeEnum()) {
            WindowInfo windowInfo = config.getWindowInfo();
            if (windowInfo == null) {
                throw new RuntimeException(findImgSet_noWindow());
            }
            x = windowInfo.getX();
            y = windowInfo.getY();
            w = windowInfo.getWidth();
            h = windowInfo.getHeight();
        } else {
            // 识别次数大于1且开启全屏重试
            if (findPositionConfig.getFindTime() > 1 && activation.equals(config.getAllRegion())) {
                x = 0;
                y = 0;
                w = screenWidth;
                h = screenHeight;
            } else {
                x = config.getX();
                y = config.getY();
                h = config.getHeight();
                w = config.getWidth();
            }
        }
        try {
            screenImg = new Robot().createScreenCapture(new Rectangle(
                    Math.max(0, Math.min(x, screenWidth)),
                    Math.max(0, Math.min(y, screenHeight)),
                    Math.max(1, Math.min(w, screenWidth - x)),
                    Math.max(1, Math.min(h, screenHeight - y))));
        } catch (AWTException e) {
            throw new RuntimeException(text_screenErr() + e.getMessage(), e);
        }
        checkInterruption();
        // 初始化匹配结果存储变量
        MatchPointBean matchPointBean = new MatchPointBean();
        AtomicReference<Double> bestVal = new AtomicReference<>(-1.0);
        AtomicReference<Point> bestLocRef = new AtomicReference<>(new Point(0, 0));
        // 读取图片为byte数组，防止中文路径乱码
        byte[] bytes = Files.readAllBytes(new File(findPositionConfig.getTemplatePath()).toPath());
        try (Mat screenMat = bufferedImageToMat(screenImg);
             Mat templateMat = opencv_imgcodecs.imdecode(new Mat(bytes), opencv_imgcodecs.IMREAD_UNCHANGED)) {
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
                            // 检查模板尺寸是否合法
                            if (resizedTemplate.cols() > screenGray.cols() || resizedTemplate.rows() > screenGray.rows()) {
                                return;
                            }
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
                                        double relX = (maxLoc.x() + templateWidth / 2.0) / scale;
                                        double relY = (maxLoc.y() + templateHeight / 2.0) / scale;
                                        int absX = (int) Math.round(relX) + x;
                                        int absY = (int) Math.round(relY) + y;
                                        bestLocRef.set(new Point(absX, absY));
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            matchPointBean.setPoint(bestLocRef.get())
                    .setMatchThreshold((int) (bestVal.get() * 100));
            // 匹配成功返回匹配坐标和匹配度，否则只返回匹配度
            if (bestVal.get() >= findPositionConfig.getMatchThreshold() / 100) {
                return matchPointBean;
            }
        }
        return matchPointBean;
    }

    /**
     * BufferedImage 转 Mat
     *
     * @param image 需要转换的图片
     * @return Mat 对象
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
            throw new InterruptedException(text_cancel());
        }
    }

    /**
     * 检查是否能正常截图
     *
     * @return true-可以截图，false-无法截图
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
