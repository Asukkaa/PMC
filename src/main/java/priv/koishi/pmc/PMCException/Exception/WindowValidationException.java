package priv.koishi.pmc.PMCException.Exception;

/**
 * 窗口信息校验异常类
 *
 * @author Koishi
 * Date:2026-08-21
 * Time:17:10
 */
public class WindowValidationException extends RuntimeException {

    /**
     * 窗口信息校验异常
     *
     * @param message 异常信息
     */
    public WindowValidationException(String message) {
        super(message);
    }

}
