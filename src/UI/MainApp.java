package UI;

import data.AppSession;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        RoleChoiceWindow window = new RoleChoiceWindow();
        window.setOnCloseRequest(e -> {
            AppSession.getInstance().closeConnection();
            Platform.exit();
        });
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}