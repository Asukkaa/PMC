package priv.koishi.pmc.PMCException.Exception;

/**
 * 跳转辑异常类
 *
 * @author Koishi
 * Date:2026-08-21
 * Time:17:09
 */
public class JumpSettingException extends RuntimeException {

    /**
     * 跳转逻辑异常
     *
     * @param message 异常信息
     */
    public JumpSettingException(String message) {
        super(message);
    }

}
