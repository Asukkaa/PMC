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
 * @author KOISHI
 * Date:2025-06-23
 * Time:15:49
 */
public class ProgressDialog {

    private Stage dialogStage;

    private ProgressBar progressBar;

    private Label messageLabel;

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

    public void updateProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            if (progress >= 1.0) {
                messageLabel.setText(bundle.getString("update.installing"));
            }
        });
    }

    public void close() {
        Platform.runLater(() -> {
            if (dialogStage != null) {
                dialogStage.close();
            }
        });
    }

}
