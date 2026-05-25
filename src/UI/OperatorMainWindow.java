package UI;

import data.OperatorRepository;
import data.dto.ResidentSearchResult;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.List;

public class OperatorMainWindow extends Stage {

    private final OperatorRepository repository = new OperatorRepository();
    private final TableView<ResidentSearchResult> table = new TableView<ResidentSearchResult>();

    public OperatorMainWindow() {
        setTitle("Учётная запись оператора");

        Label title = new Label("Оператор");
        title.setStyle("-fx-font-size: 18px;");

        Label lastNameLabel = new Label("Фамилия");
        TextField lastNameField = new TextField();

        Label firstNameLabel = new Label("Имя");
        TextField firstNameField = new TextField();

        Button searchButton = new Button("Найти");
        Button openButton = new Button("Открыть карточку жильца");
        Button readingHistoryButton = new Button("История показаний");
        Button paymentHistoryButton = new Button("История оплат");

        Button buildingsButton = new Button("Дома");
        Button apartmentsButton = new Button("Квартиры");
        Button residentsButton = new Button("Жильцы");
        Button servicesButton = new Button("Тарифы");
        Button benefitsButton = new Button("Льготы");
        Button readingsButton = new Button("Ввод показаний");

        configureTable();

        searchButton.setOnAction(e -> doSearch(lastNameField.getText(), firstNameField.getText()));
        openButton.setOnAction(e -> openSelectedResident());
        
        readingHistoryButton.setOnAction(e -> {
            ResidentSearchResult selected = table.getSelectionModel().getSelectedItem();
            if(selected == null){
                showAlert("Ошибка", "Выберите жильца");
                return;
            }
            new ReadingHistoryWindow(selected.getResidentId()).show();
        });
        
        paymentHistoryButton.setOnAction(e -> {
            ResidentSearchResult selected = table.getSelectionModel().getSelectedItem();
            if(selected == null){
                showAlert("Ошибка", "Выберите жильца");
                return;
            }
            new PaymentHistoryWindow(selected.getResidentId()).show();
        });

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

        buildingsButton.setOnAction(e -> new BuildingManagementWindow().show());
        apartmentsButton.setOnAction(e -> new ApartmentManagementWindow().show());
        residentsButton.setOnAction(e -> new ResidentManagementWindow().show());
        servicesButton.setOnAction(e -> new ServiceManagementWindow().show());
        benefitsButton.setOnAction(e -> new BenefitTypeManagementWindow().show());
        readingsButton.setOnAction(e -> new ReadingEntryWindow().show());

        GridPane filters = new GridPane();
        filters.setHgap(10);
        filters.setVgap(8);
        filters.add(lastNameLabel, 0, 0);
        filters.add(lastNameField, 1, 0);
        filters.add(firstNameLabel, 0, 1);
        filters.add(firstNameField, 1, 1);
        filters.add(searchButton, 1, 2);

        HBox adminButtons = new HBox(10, buildingsButton, apartmentsButton, residentsButton,
                servicesButton, benefitsButton, readingsButton);
        adminButtons.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(10, title, filters, adminButtons);
        topBox.setPadding(new Insets(15));

        HBox bottomButtons = new HBox(
            10,
            openButton,
            readingHistoryButton,
            paymentHistoryButton
        );
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setPadding(new Insets(10));
        
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(table);
        root.setBottom(bottomButtons);

        setScene(new Scene(root, 900, 520));

        doSearch("", "");
    }

    private void configureTable() {
        TableColumn<ResidentSearchResult, Integer> idCol =
                new TableColumn<ResidentSearchResult, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, Integer>("residentId"));
        idCol.setPrefWidth(80);

        TableColumn<ResidentSearchResult, String> lastNameCol =
                new TableColumn<ResidentSearchResult, String>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, String>("lastName"));
        lastNameCol.setPrefWidth(160);

        TableColumn<ResidentSearchResult, String> firstNameCol =
                new TableColumn<ResidentSearchResult, String>("Имя");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, String>("firstName"));
        firstNameCol.setPrefWidth(140);

        TableColumn<ResidentSearchResult, String> addressCol =
                new TableColumn<ResidentSearchResult, String>("Адрес");
        addressCol.setCellValueFactory(new PropertyValueFactory<ResidentSearchResult, String>("fullAddress"));
        addressCol.setPrefWidth(420);

        table.getColumns().addAll(idCol, lastNameCol, firstNameCol, addressCol);
        table.setPlaceholder(new Label("Ничего не найдено"));
    }

    private void doSearch(String lastName, String firstName) {
        try {
            List<ResidentSearchResult> results = repository.searchResidents(lastName, firstName);
            table.setItems(FXCollections.observableArrayList(results));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void openSelectedResident() {
        ResidentSearchResult selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Сначала выберите жильца из таблицы");
            return;
        }
        new ResidentProfileWindow(selected.getResidentId(), "Карточка жильца").show();
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String text) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }
}