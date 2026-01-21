package priv.koishi.pmc.UI.CustomProgressDialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static priv.koishi.pmc.Controller.MainController.aboutController;
import static priv.koishi.pmc.Finals.CommonFinals.logoPath;
import static priv.koishi.pmc.Utils.UiUtils.setWindowLogo;

/**
 * 进度条对话框
 *
 * @author KOISHI
 * Date:2025-06-23
 * Time:15:49
 */
public class ProgressDialog {

    /**
     * 窗口场景
     */
    private Stage dialogStage;

    /**
     * 进度条
     */
    private ProgressBar progressBar;

    /**
     * 提示信息
     */
    private Label messageLabel;

    /**
     * 功能按钮
     */
    private Button button;

    /**
     * 显示窗口
     *
     * @param message       提示信息
     * @param title         窗口标题
     * @param buttonText    功能按钮文本
     * @param onClickAction 功能按钮点击事件
     */
    public void show(String message, String title, String buttonText, Runnable onClickAction) {
        Platform.runLater(() -> {
            dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setTitle(title);
            messageLabel = new Label(message);
            progressBar = new ProgressBar();
            progressBar.setPrefWidth(300);
            button = new Button();
            button.setCursor(Cursor.HAND);
            updateButton(buttonText, onClickAction);
            VBox vbox = new VBox(10, messageLabel, progressBar, button);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(20));
            Scene scene = new Scene(vbox);
            dialogStage.setScene(scene);
            setWindowLogo(dialogStage, logoPath);
            // 禁用窗口关闭按钮
            dialogStage.setOnCloseRequest(event -> {
                // 阻止窗口关闭
                event.consume();
                // 执行取消操作
                aboutController.cancelUpdate();
                close();
            });
            dialogStage.show();
        });
    }

    /**
     * 更新功能按钮
     *
     * @param buttonText    按钮文本
     * @param onClickAction 按钮点击事件
     */
    public void updateButton(String buttonText, Runnable onClickAction) {
        Platform.runLater(() -> {
            button.setText(buttonText);
            button.setOnAction(_ -> {
                if (onClickAction != null) {
                    onClickAction.run();
                }
            });
        });
    }

    /**
     * 更新进度
     *
     * @param progress 进度
     * @param message  提示信息
     */
    public void updateProgress(double progress, String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            updateMessage(message);
        });
    }

    /**
     * 更新提示信息
     *
     * @param message 提示信息
     */
    public void updateMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * 关闭窗口
     */
    public void close() {
        Platform.runLater(() -> {
            if (dialogStage != null) {
                dialogStage.close();
                dialogStage = null;
            }
        });
    }

}
