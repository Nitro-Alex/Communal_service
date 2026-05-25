package UI;

import data.AppSession;
import data.DbConnectionManager;
import data.DbType;
import data.UserRole;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DatabaseChoiceWindow extends Stage {

    private final UserRole role;

    public DatabaseChoiceWindow(UserRole role) {
        this.role = role;
        setTitle("Выбор СУБД");

        Label title = new Label("Выберите используемую СУБД");
        Button pgButton = new Button("PostgreSQL");
        Button sqlButton = new Button("MS SQL Server");

        pgButton.setOnAction(e -> openDatabase(DbType.POSTGRESQL));
        sqlButton.setOnAction(e -> openDatabase(DbType.SQL_SERVER));

        VBox root = new VBox(12, title, pgButton, sqlButton);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        setScene(new Scene(root, 320, 180));
    }

    private void openDatabase(DbType dbType) {
        try {
            AppSession session = AppSession.getInstance();
            session.setRole(role);
            session.setDbType(dbType);
            session.setConnection(DbConnectionManager.openConnection(dbType));

            if (role == UserRole.RESIDENT) {
                new ResidentSearchWindow().show();
            } else {
                new OperatorMainWindow().show();
            }

            close();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Ошибка подключения");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}