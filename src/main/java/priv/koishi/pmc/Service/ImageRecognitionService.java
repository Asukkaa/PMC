package priv.koishi.pmc.Service;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.tesseract.ResultIterator;
import org.bytedeco.tesseract.TessBaseAPI;
import priv.koishi.pmc.Bean.ClickLogBean;
import priv.koishi.pmc.Bean.Config.FindPositionConfig;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.MatchPointBean;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.Finals.Enum.RecognitionTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.Queue.DynamicQueue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.tesseract.global.tesseract.PSM_AUTO;
import static org.bytedeco.tesseract.global.tesseract.RIL_WORD;
import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
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

    // macOS 加载内置的 libavif，防止操作系统没有安装相关环境
    static {
        if (!isRunningFromIDEA && isMac) {
            System.load(macAppDirectory + "/libavif.16.3.0.dylib");
        }
    }

    /**
     * 多尺度并行匹配缩放比例
     */
    private static final double[] scales = {0.8, 0.9, 1.0, 1.1, 1.2};

    /**
     * 屏幕宽度
     */
    public static int screenWidth;

    /**
     * 屏幕高度
     */
    public static int screenHeight;

    /**
     * 缩放比例
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
        String templatePath = config.getTemplate();
        int overTime = config.getOverTime();
        String name = config.getName();
        int recognitionType = config.getRecognitionType();
        String fileName = templatePath;
        if (recognitionType == RecognitionTypeEnum.IMAGE.ordinal()) {
            fileName = getFileName(templatePath);
            if (StringUtils.isBlank(templatePath)) {
                throw new RuntimeException(text_image() + fileName + text_nullPath());
            }
            File file = new File(templatePath);
            if (!file.exists()) {
                throw new RuntimeException(text_image() + fileName + text_noExists());
            }
        } else if (recognitionType == RecognitionTypeEnum.COLOR.ordinal()) {
            if (StringUtils.isBlank(templatePath)) {
                throw new RuntimeException(text_noColor());
            }
        } else if (recognitionType == RecognitionTypeEnum.TEXT.ordinal()) {
            if (StringUtils.isBlank(templatePath)) {
                throw new RuntimeException(text_noText());
            }
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
                    String timeOutErr = text_imgTarget(recognitionType) + fileName + text_timeOut();
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
            int infoX = windowInfo.getX();
            int infoY = windowInfo.getY();
            int infoW = windowInfo.getWidth();
            int infoH = windowInfo.getHeight();
            x = (int) (infoX + infoW * windowInfo.getRelativeX());
            y = (int) (infoY + infoH * windowInfo.getRelativeY());
            w = (int) (infoW * windowInfo.getRelativeWidth());
            h = (int) (infoH * windowInfo.getRelativeHeight());
        } else {
            // 识别次数大于 1 且开启全屏重试
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
        int recognitionType = findPositionConfig.getRecognitionType();
        // 图像识别
        if (recognitionType == RecognitionTypeEnum.IMAGE.ordinal()) {
            System.out.println("图像识别:" + findPositionConfig.getTemplate());
            matchPointBean = getImgMatchPointBean(findPositionConfig, screenImg, x, y);
            // 颜色识别
        } else if (recognitionType == RecognitionTypeEnum.COLOR.ordinal()) {
            System.out.println("颜色识别:" + findPositionConfig.getTemplate());
            matchPointBean = colorRecognition(screenImg, findPositionConfig, x, y);
            // 文字识别
        } else if (recognitionType == RecognitionTypeEnum.TEXT.ordinal()) {
            System.out.println("文字识别:" + findPositionConfig.getTemplate());
            matchPointBean = textRecognition(screenImg, findPositionConfig, x, y);
        }
        return matchPointBean;
    }

    /**
     * 图像识别
     *
     * @param findPositionConfig 配置信息
     * @param screenImg          截图图像
     * @param offsetX            截图在屏幕上的 X 偏移
     * @param offsetY            截图在屏幕上的 Y 偏移
     * @return 匹配点及相似度
     * @throws IOException 读取图片失败时抛出异常
     */
    private static MatchPointBean getImgMatchPointBean(FindPositionConfig findPositionConfig, BufferedImage screenImg,
                                                       int offsetX, int offsetY) throws IOException {
        AtomicReference<Double> bestVal = new AtomicReference<>(0.0);
        AtomicReference<Point> bestLocRef = new AtomicReference<>(new Point(0, 0));
        // 读取图片为 byte 数组，防止中文路径乱码
        byte[] bytes = Files.readAllBytes(new File(findPositionConfig.getTemplate()).toPath());
        try (Mat screenMat = bufferedImageToMat(screenImg);
             Mat templateMat = opencv_imgcodecs.imdecode(new Mat(bytes), opencv_imgcodecs.IMREAD_UNCHANGED)) {
            // 转换为灰度图提升处理效率
            try (Mat screenGray = new Mat(); Mat templateGray = new Mat()) {
                cvtColor(screenMat, screenGray, COLOR_BGR2GRAY);
                cvtColor(templateMat, templateGray, COLOR_BGR2GRAY);
                // DPI 缩放补偿：根据系统缩放反向调整模板尺寸
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
                                        int absX = (int) Math.round(relX) + offsetX;
                                        int absY = (int) Math.round(relY) + offsetY;
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
            MatchPointBean matchPointBean = new MatchPointBean();
            matchPointBean.setPoint(bestLocRef.get())
                    .setMatchThreshold((int) (bestVal.get() * 100));
            // 匹配成功返回匹配坐标和匹配度，否则只返回匹配度
            if (bestVal.get() >= findPositionConfig.getMatchThreshold() / 100) {
                return matchPointBean;
            }
        }
        return null;
    }

    /**
     * 颜色识别
     *
     * @param screenImg 截图图像
     * @param config    配置信息
     * @param offsetX   截图在屏幕上的 X 偏移
     * @param offsetY   截图在屏幕上的 Y 偏移
     * @return 匹配点及相似度
     */
    private static MatchPointBean colorRecognition(BufferedImage screenImg, FindPositionConfig config,
                                                   int offsetX, int offsetY) {
        // 解析目标颜色
        Color targetColor = fxColorToAwt(javafx.scene.paint.Color.valueOf(config.getTemplate()));
        int tolerance = config.getColorTolerance();
        double matchThreshold = config.getMatchThreshold() / 100.0;
        int width = screenImg.getWidth();
        int height = screenImg.getHeight();
        // 记录最大相似度及其坐标
        double maxSimilarity = 0.0;
        int maxX = 0, maxY = 0;
        // 构建掩码数据，同时记录最大相似度
        byte[] maskData = new byte[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = screenImg.getRGB(x, y);
                Color pixelColor = new Color(rgb);
                // 计算两个颜色的相似度
                double similarity = colorSimilarity(pixelColor, targetColor, tolerance);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    maxX = x;
                    maxY = y;
                }
                if (similarity >= matchThreshold) {
                    // 白色（前景）
                    maskData[y * width + x] = (byte) 255;
                } else {
                    // 黑色（背景）
                    maskData[y * width + x] = 0;
                }
            }
        }
        // 如果没有达到阈值的像素，直接返回最高相似度点
        if (maxSimilarity < matchThreshold) {
            return new MatchPointBean()
                    .setPoint(new Point(maxX + offsetX, maxY + offsetY))
                    .setMatchThreshold((int) (maxSimilarity * 100));
        }
        // 将 byte[] 转换为 BytePointer 并创建掩码 Mat（CV_8UC1）
        BytePointer maskPtr = new BytePointer(maskData);
        Mat mask = new Mat(height, width, CV_8UC1, maskPtr);
        // 连通组件分析
        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        int numLabels = connectedComponentsWithStats(mask, labels, stats, centroids, 8, CV_32S);
        MatchPointBean result = new MatchPointBean();
        if (numLabels <= 1) {
            // 没有找到匹配区域（只有背景），返回最高相似度点
            result.setPoint(new Point(maxX + offsetX, maxY + offsetY))
                    .setMatchThreshold((int) (maxSimilarity * 100));
        } else {
            // 找出面积最大的连通区域（排除背景 label=0）
            int maxArea = 0;
            int maxLabel = 1;
            for (int i = 1; i < numLabels; i++) {
                int area = new IntPointer(stats.ptr(i)).get(CC_STAT_AREA);
                if (area > maxArea) {
                    maxArea = area;
                    maxLabel = i;
                }
            }
            // 获取该区域的质心坐标
            DoublePointer centroidPtr = new DoublePointer(centroids.ptr(maxLabel));
            double centerX = centroidPtr.get(0);
            double centerY = centroidPtr.get(1);
            int finalX = (int) Math.round(centerX) + offsetX;
            int finalY = (int) Math.round(centerY) + offsetY;
            result.setPoint(new Point(finalX, finalY));
            // 置信度：设为最大相似度
            result.setMatchThreshold((int) (maxSimilarity * 100));
        }
        // 释放资源
        mask.close();
        labels.close();
        stats.close();
        centroids.close();
        return result;
    }

    /**
     * 将 JavaFX 颜色转换为 AWT 颜色
     *
     * @param fxColor JavaFX 颜色
     * @return AWT 颜色
     */
    public static Color fxColorToAwt(javafx.scene.paint.Color fxColor) {
        return new Color((int) Math.round(fxColor.getRed() * 255),
                (int) Math.round(fxColor.getGreen() * 255),
                (int) Math.round(fxColor.getBlue() * 255),
                (int) Math.round(fxColor.getOpacity() * 255));
    }

    /**
     * 计算两个颜色的相似度（基于 RGB 欧氏距离，带容差）
     *
     * @param source    源像素颜色
     * @param target    目标颜色
     * @param tolerance 容差
     * @return 0~1 之间的相似度，1 表示完全相同（或在容差范围内）
     */
    private static double colorSimilarity(Color source, Color target, int tolerance) {
        double dr = source.getRed() - target.getRed();
        double dg = source.getGreen() - target.getGreen();
        double db = source.getBlue() - target.getBlue();
        double distance = Math.sqrt(dr * dr + dg * dg + db * db);
        // 最大可能距离 sqrt(3 * 255^2) ≈ 441.67
        if (distance <= tolerance) {
            return 1.0;
        }
        return 1.0 - Math.min(1.0, distance / 441.67);
    }

    /**
     * 文字识别
     *
     * @param screenImg 截图图像
     * @param config    配置信息
     * @param offsetX   截图偏移
     * @param offsetY   截图偏移
     * @return 匹配点及置信度
     */
    private static MatchPointBean textRecognition(BufferedImage screenImg, FindPositionConfig config,
                                                  int offsetX, int offsetY) {
        String targetText = config.getTemplate();
        if (StringUtils.isEmpty(targetText)) {
            throw new IllegalArgumentException("待识别的文字不能为空");
        }
        // 1. 转换为 Mat 并灰度化
        Mat imageMat = bufferedImageToMat(screenImg);
        Mat grayMat = new Mat();
        cvtColor(imageMat, grayMat, COLOR_BGR2GRAY);
        // 2. 初始化 Tesseract
        try (TessBaseAPI api = new TessBaseAPI()) {
            String dataPath = "tessdata/简体中文.traineddata";
            String language = config.getOcrLanguage();
            if (api.Init(dataPath, language) != 0) {
                throw new RuntimeException("Tesseract 初始化失败，请检查 tessdata 路径: " + dataPath);
            }
            // 设置页面分割模式为自动
            api.SetPageSegMode(PSM_AUTO);
            // 将灰度图数据传递给 Tesseract
            api.SetImage(grayMat.data(), grayMat.cols(), grayMat.rows(),
                    (int) grayMat.elemSize(), (int) grayMat.step1());
            // 3. 获取结果迭代器
            ResultIterator ri = api.GetIterator();
            if (ri == null) {
                return new MatchPointBean().setPoint(new Point(0, 0)).setMatchThreshold(0);
            }
            float bestConfidence = 0.0f;
            Point bestCenter = null;
            // 4. 遍历所有单词
            do {
                // 获取单词文本
                BytePointer wordTextPtr = ri.GetUTF8Text(RIL_WORD);
                if (wordTextPtr == null) continue;
                String word = wordTextPtr.getString().trim();
                wordTextPtr.deallocate();
                if (word.equalsIgnoreCase(targetText)) {
                    // 获取单词边界框
                    IntPointer x1 = new IntPointer(1);
                    IntPointer y1 = new IntPointer(1);
                    IntPointer x2 = new IntPointer(1);
                    IntPointer y2 = new IntPointer(1);
                    if (ri.BoundingBox(RIL_WORD, x1, y1, x2, y2)) {
                        float confidence = ri.Confidence(RIL_WORD);
                        if (confidence > bestConfidence) {
                            bestConfidence = confidence;
                            int centerX = (x1.get() + x2.get()) / 2 + offsetX;
                            int centerY = (y1.get() + y2.get()) / 2 + offsetY;
                            bestCenter = new Point(centerX, centerY);
                        }
                    }
                }
            } while (ri.Next(RIL_WORD));
            if (bestCenter != null) {
                // 将浮点置信度转为整数（四舍五入）
                int matchThreshold = Math.round(bestConfidence);
                return new MatchPointBean()
                        .setPoint(bestCenter)
                        .setMatchThreshold(matchThreshold);
            }
        } catch (Exception e) {
            throw new RuntimeException("文字识别失败: " + e.getMessage(), e);
        }
        return new MatchPointBean().setPoint(new Point(0, 0)).setMatchThreshold(0);
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
        // 转换时数据类型为 CV_8U
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
