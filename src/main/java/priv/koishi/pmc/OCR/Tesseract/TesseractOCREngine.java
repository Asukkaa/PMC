package priv.koishi.pmc.OCR.Tesseract;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.tesseract.TessBaseAPI;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.bytedeco.tesseract.global.tesseract.PSM_AUTO;
import static priv.koishi.pmc.Finals.CommonFinals.tessdataDirectory;
import static priv.koishi.pmc.Finals.i18nFinal.text_tesseractInitErr;

/**
 * TesseractOCR 引擎配置类
 *
 * @author Koishi
 * Date:2026-08-21
 * Time:12:12
 */
public class TesseractOCREngine {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(TesseractOCREngine.class);

    /**
     * 引擎池：Key = 语言字符串，Value = 对应的 TessBaseAPI 实例
     */
    private static final Map<String, TessBaseAPI> enginePool = new ConcurrentHashMap<>();

    /**
     * 引擎是否已成功初始化（true 表示池中至少有一个引擎，默认 false）
     */
    private static boolean initialized;

    /**
     * 引擎关闭状态（true 空闲后将会关闭引擎，默认 false）
     */
    public static boolean isReleasing;

    /**
     * 批量初始化引擎池（预加载指定语言）
     * <p>
     * 初始化所有在 {@code languages} 集合中的语言，并将它们加入池中。
     * 同时销毁池中但不在新集合中的旧引擎
     *
     * @param languages 需要预加载的语言字符串集合（如 {"eng", "chi_sim"}）
     * @throws RuntimeException 如果任一语言初始化失败
     */
    public static synchronized void initEnginePool(Set<String> languages) {
        if (CollectionUtils.isNotEmpty(languages)) {
            // 拼接语言组合 key
            String languageKey = String.join("+", languages);
            // 如果已存在，直接返回
            if (enginePool.containsKey(languageKey)) {
                logger.info("语言组合 {} 已存在于池中，跳过加载", languageKey);
                return;
            }
            // 创建并放入池中
            TessBaseAPI api = createEngine(languageKey);
            enginePool.put(languageKey, api);
            initialized = true;
            logger.info("预加载引擎：{}", languageKey);
        }
    }

    /**
     * 初始化 OCR 引擎（懒加载）
     * <p>
     * 如果池中已存在该语言则直接返回，否则初始化并放入池中
     *
     * @param language 语言字符串，如 "eng"、"chi_sim+eng"
     * @throws RuntimeException 如果初始化失败
     */
    private static synchronized void initEngine(String language) {
        if (enginePool.containsKey(language)) {
            initialized = true;
            return;
        }
        TessBaseAPI api = createEngine(language);
        enginePool.put(language, api);
        initialized = true;
        logger.info("懒加载引擎：{}", language);
    }

    /**
     * 释放所有引擎资源
     */
    public static synchronized void releaseEngine() {
        if (initialized) {
            for (Map.Entry<String, TessBaseAPI> entry : enginePool.entrySet()) {
                try {
                    entry.getValue().End();
                    logger.info("释放引擎：{}", entry.getKey());
                } catch (Exception e) {
                    logger.warn("释放引擎 {} 时发生异常", entry.getKey(), e);
                }
            }
            enginePool.clear();
            initialized = false;
            logger.info("所有引擎已释放");
        }
        isReleasing = false;
    }

    /**
     * 获取指定语言的引擎实例
     *
     * @param language 语言字符串
     * @return TessBaseAPI 实例
     */
    public static TessBaseAPI getEngine(String language) {
        TessBaseAPI api = enginePool.get(language);
        if (api == null) {
            initEngine(language);
            api = enginePool.get(language);
        }
        return api;
    }

    /**
     * 创建单个引擎实例
     *
     * @param language 语言字符串
     * @return 已初始化的 TessBaseAPI 实例
     */
    private static TessBaseAPI createEngine(String language) {
        logger.info("加载语言模型: {}, 路径: {}", language, tessdataDirectory);
        long startTime = System.currentTimeMillis();
        TessBaseAPI api = new TessBaseAPI();
        int initCode = api.Init(tessdataDirectory, language);
        if (initCode != 0) {
            throw new RuntimeException(text_tesseractInitErr() + tessdataDirectory +
                    "\ninitCode: " + initCode + ", language: " + language);
        }
        api.SetPageSegMode(PSM_AUTO);
        logger.info("语言 {} 加载完成，耗时: {} ms", language, System.currentTimeMillis() - startTime);
        return api;
    }

}
