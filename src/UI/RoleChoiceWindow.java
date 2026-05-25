package UI;

import data.UserRole;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RoleChoiceWindow extends Stage {

    public RoleChoiceWindow() {
        setTitle("Выбор учётной записи");

        Label title = new Label("Вход в систему");
        Button operatorButton = new Button("Оператор");
        Button residentButton = new Button("Обычный пользователь");

        operatorButton.setOnAction(e -> {
            new DatabaseChoiceWindow(UserRole.OPERATOR).show();
            close();
        });

        residentButton.setOnAction(e -> {
            new DatabaseChoiceWindow(UserRole.RESIDENT).show();
            close();
        });

        VBox root = new VBox(12, title, operatorButton, residentButton);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        setScene(new Scene(root, 500, 300));
    }
}