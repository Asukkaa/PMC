package priv.koishi.pmc.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

/**
 * 赞赏页面控制器
 *
 * @author KOISHI
 * Date:2025-11-10
 * Time:21:17
 */
public class AppreciateController extends RootController {

    @FXML
    public ScrollPane scrollPane_Ap;

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) scrollPane_Ap.getScene().getWindow();
            stage.setOnCloseRequest(_ -> {
                removeController();
                AutoClickController.isSonOpening = false;
            });
        });
    }

}
