package UI;

import data.AppSession;
import data.ResidentRepository;
import data.dto.ResidentSearchResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ResidentSearchWindow extends Stage {

    private final ResidentRepository repository = new ResidentRepository();
    private final TableView<ResidentSearchResult> table = new TableView<ResidentSearchResult>();

    public ResidentSearchWindow() {
        setTitle("Поиск пользователя");

        Label title = new Label("Найдите свою запись в базе");
        title.setStyle("-fx-font-size: 16px;");

        Label lastNameLabel = new Label("Фамилия");
        TextField lastNameField = new TextField();

        Label firstNameLabel = new Label("Имя");
        TextField firstNameField = new TextField();

        Button searchButton = new Button("Найти");
        Button selectButton = new Button("Выбрать");

        configureTable();

        searchButton.setOnAction(e -> {
            try {
                List<ResidentSearchResult> results = repository.searchResidents(
                        lastNameField.getText(),
                        firstNameField.getText()
                );
                table.setItems(FXCollections.observableArrayList(results));
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        selectButton.setOnAction(e -> openSelectedResident());

        table.setRowFactory(tv -> {
            TableRow<ResidentSearchResult> row = new TableRow<ResidentSearchResult>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getItem());
                    openSelectedResident();
                }
            });
            return row;
        });

        GridPane filters = new GridPane();
        filters.setHgap(10);
        filters.setVgap(8);
        filters.add(lastNameLabel, 0, 0);
        filters.add(lastNameField, 1, 0);
        filters.add(firstNameLabel, 0, 1);
        filters.add(firstNameField, 1, 1);
        filters.add(searchButton, 1, 2);

        VBox topBox = new VBox(10, title, filters);
        topBox.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(table);
        root.setBottom(selectButton);
        BorderPane.setAlignment(selectButton, Pos.CENTER);
        BorderPane.setMargin(selectButton, new Insets(10));

        setScene(new Scene(root, 760, 420));
    }

    private void configureTable() {
        TableColumn<ResidentSearchResult, Integer> idCol = new TableColumn<ResidentSearchResult, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, Integer>("residentId"));
        idCol.setPrefWidth(80);

        TableColumn<ResidentSearchResult, String> lastNameCol = new TableColumn<ResidentSearchResult, String>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, String>("lastName"));
        lastNameCol.setPrefWidth(160);

        TableColumn<ResidentSearchResult, String> firstNameCol = new TableColumn<ResidentSearchResult, String>("Имя");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, String>("firstName"));
        firstNameCol.setPrefWidth(140);

        TableColumn<ResidentSearchResult, String> addressCol = new TableColumn<ResidentSearchResult, String>("Адрес");
        addressCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, String>("fullAddress"));
        addressCol.setPrefWidth(340);

        table.getColumns().addAll(idCol, lastNameCol, firstNameCol, addressCol);
        table.setPlaceholder(new Label("Ничего не найдено"));
    }

    private void openSelectedResident() {
        ResidentSearchResult selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Сначала выберите жильца из таблицы");
            return;
        }

        AppSession.getInstance().setResidentId(Integer.valueOf(selected.getResidentId()));
        AppSession.getInstance().setResidentSearchResult(selected);

        new UserMainWindow(selected.getResidentId()).show();
        close();
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}