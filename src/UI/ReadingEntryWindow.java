package UI;

import data.OperatorRepository;
import data.dto.ResidentSearchResult;
import data.dto.ServiceItem;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class ReadingEntryWindow extends Stage {

    private final OperatorRepository repository = new OperatorRepository();
    private final TableView<ResidentSearchResult> residentTable = new TableView<ResidentSearchResult>();

    private final Label selectedResidentLabel = new Label("Жилец не выбран");
    private final ComboBox<ServiceItem> serviceBox = new ComboBox<ServiceItem>();
    private final TextField monthField = new TextField();
    private final TextField yearField = new TextField();
    private final TextField valueField = new TextField();

    private Integer selectedResidentId = null;

    public ReadingEntryWindow() {
        setTitle("Ввод показаний");

        configureResidentTable();
        refreshResidents();
        refreshServices();

        Label title = new Label("Ввод показаний оператором");

        Button searchButton = new Button("Найти");
        Button addButton = new Button("Добавить показание");

        TextField lastNameField = new TextField();
        TextField firstNameField = new TextField();

        monthField.setPromptText("");
        yearField.setPromptText("");
        valueField.setPromptText("");

        residentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedResidentId = Integer.valueOf(newVal.getResidentId());
                selectedResidentLabel.setText(
                        "Выбран: " + newVal.getLastName() + " " + newVal.getFirstName()
                                + " | ID " + newVal.getResidentId()
                                + " | " + newVal.getFullAddress()
                );
            }
        });

        searchButton.setOnAction(e -> {
            try {
                List<ResidentSearchResult> results = repository.searchResidents(
                        lastNameField.getText(),
                        firstNameField.getText()
                );
                residentTable.setItems(FXCollections.observableArrayList(results));
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        addButton.setOnAction(e -> {
            try {
                if (selectedResidentId == null) {
                    UiAlerts.showWarning("Ошибка ввода", "Сначала выберите жильца");
                    return;
                }

                ServiceItem service = serviceBox.getValue();
                if (service == null) {
                    UiAlerts.showWarning("Ошибка ввода", "Выберите услугу");
                    return;
                }

                int month = Integer.parseInt(monthField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());
                BigDecimal value = new BigDecimal(valueField.getText().trim());

                if (month < 1 || month > 12) {
                    UiAlerts.showWarning("Ошибка ввода", "Номер месяца должен быть от 1 до 12");
                    return;
                }

                if (year < 2000) {
                    UiAlerts.showWarning("Ошибка ввода", "Год должен быть не меньше 2000");
                    return;
                }

                if (value.compareTo(BigDecimal.ZERO) < 0) {
                    UiAlerts.showWarning("Ошибка ввода", "Показание не может быть отрицательным");
                    return;
                }

                repository.addReading(
                        selectedResidentId.intValue(),
                        service.getName(),
                        month,
                        year,
                        value
                );

                UiAlerts.showWarning("Успех", "Показание успешно добавлено");
                valueField.clear();

            } catch (NumberFormatException ex) {
                UiAlerts.showWarning("Ошибка ввода", "Месяц, год и показание должны быть числами");
            } catch (Exception ex) {
                UiAlerts.showError("Ошибка", ex.getMessage());
            }
        });

        GridPane searchForm = new GridPane();
        searchForm.setHgap(10);
        searchForm.setVgap(8);
        searchForm.add(new Label("Фамилия"), 0, 0);
        searchForm.add(lastNameField, 1, 0);
        searchForm.add(new Label("Имя"), 0, 1);
        searchForm.add(firstNameField, 1, 1);
        searchForm.add(searchButton, 1, 2);

        GridPane readingForm = new GridPane();
        readingForm.setHgap(10);
        readingForm.setVgap(8);
        readingForm.add(new Label("Услуга"), 0, 0);
        readingForm.add(serviceBox, 1, 0);
        readingForm.add(new Label("Месяц"), 0, 1);
        readingForm.add(monthField, 1, 1);
        readingForm.add(new Label("Год"), 0, 2);
        readingForm.add(yearField, 1, 2);
        readingForm.add(new Label("Показание"), 0, 3);
        readingForm.add(valueField, 1, 3);
        readingForm.add(addButton, 1, 4);

        VBox root = new VBox(12,
                title,
                searchForm,
                new Label("Результаты поиска"),
                residentTable,
                selectedResidentLabel,
                readingForm
        );
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 980, 620));
    }

    private void configureResidentTable() {
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
        addressCol.setPrefWidth(460);

        residentTable.getColumns().addAll(idCol, lastNameCol, firstNameCol, addressCol);
        residentTable.setPlaceholder(new Label("Ничего не найдено"));
    }

    private void refreshResidents() {
        try {
            residentTable.setItems(FXCollections.observableArrayList(repository.searchResidents("", "")));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshServices() {
        try {
            List<ServiceItem> services = repository.loadServices();
            serviceBox.setItems(FXCollections.observableArrayList(services));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}