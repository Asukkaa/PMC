package priv.koishi.pmc.ProgressDialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static priv.koishi.pmc.Finals.CommonFinals.logoPath;
import static priv.koishi.pmc.MainApplication.bundle;
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
     * 显示窗口
     *
     * @param message 提示信息
     */
    public void show(String message) {
        Platform.runLater(() -> {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            messageLabel = new Label(message);
            progressBar = new ProgressBar();
            progressBar.setPrefWidth(300);
            VBox vbox = new VBox(10, messageLabel, progressBar);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(20));
            Scene scene = new Scene(vbox);
            dialogStage.setScene(scene);
            setWindowLogo(dialogStage, logoPath);
            dialogStage.show();
        });
    }

    /**
     * 更新进度
     *
     * @param progress 进度
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            if (progress >= 1.0) {
                messageLabel.setText(bundle.getString("update.installing"));
            }
        });
    }

    /**
     * 关闭窗口
     */
    public void close() {
        Platform.runLater(() -> {
            if (dialogStage != null) {
                dialogStage.close();
            }
        });
    }

}
