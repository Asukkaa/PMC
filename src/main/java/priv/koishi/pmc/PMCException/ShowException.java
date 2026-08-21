package priv.koishi.pmc.PMCException;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.pmc.PMCException.Exception.JumpSettingException;
import priv.koishi.pmc.PMCException.Exception.WindowValidationException;

import static priv.koishi.pmc.Finals.CommonFinals.logoPath;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Utils.CommonUtils.errToString;
import static priv.koishi.pmc.Utils.UiUtils.setWindowLogo;

/**
 * 错误展示类
 *
 * @author Koishi
 * Date:2026-07-01
 * Time:17:12
 */
public class ShowException {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(ShowException.class);

    /**
     * 处理异常的统一弹窗
     *
     * @param ex 要处理的异常
     */
    public static void showExceptionAlert(Throwable ex) {
        logger.error(ex, ex);
        Alert alert;
        switch (ex) {
            case JumpSettingException _ -> {
                alert = creatErrorAlert(ex.getMessage());
                alert.setHeaderText(text_jumpSettingErr());
            }
            case WindowValidationException _ -> {
                alert = creatErrorAlert(ex.getMessage());
                alert.setHeaderText(text_windowInfoErr());
            }
            default -> {
                alert = creatErrorAlert(errToString(ex));
                Throwable cause = ex.getCause();
                String message;
                if (cause instanceof RuntimeException) {
                    message = cause.getMessage();
                } else {
                    if (cause != null) {
                        cause = cause.getCause();
                    }
                    if (cause != null) {
                        if (cause instanceof Exception) {
                            message = cause.getMessage();
                        } else {
                            message = ex.getMessage();
                        }
                    } else {
                        message = ex.getMessage();
                    }
                }
                if (StringUtils.isBlank(message)) {
                    message = text_error();
                } else if (message.length() > 200 && !message.contains("\n")) {
                    message = message.substring(0, 200) + " ...";
                }
                alert.setHeaderText(message);
            }
        }
        // 展示弹窗
        Platform.runLater(alert::show);
    }

    /**
     * 创建一个错误弹窗
     *
     * @param errString 要展示的异常信息
     * @return Alert 弹窗对象
     */
    public static Alert creatErrorAlert(String errString) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(text_abnormal());
        DialogPane dialogPane = alert.getDialogPane();
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        setWindowLogo(stage, logoPath);
        // 创建展示异常信息的 TextArea
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(errString);
        // 创建 VBox 并添加 TextArea
        VBox details = new VBox();
        VBox.setVgrow(textArea, Priority.ALWAYS);
        textArea.setMaxHeight(Double.MAX_VALUE);
        details.getChildren().add(textArea);
        dialogPane.setExpandableContent(details);
        alert.setOnShown(_ -> {
            Button okBtnNode = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okBtnNode != null) {
                okBtnNode.setCursor(Cursor.HAND);
            }
        });
        return alert;
    }

}
