package UI;

import data.OperatorRepository;
import data.dto.ApartmentItem;
import data.dto.BuildingItem;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class ApartmentManagementWindow extends Stage {

    private final OperatorRepository repository = new OperatorRepository();
    private final TableView<ApartmentItem> table = new TableView<ApartmentItem>();

    private final TextField idField = new TextField();
    private final ComboBox<BuildingItem> buildingBox = new ComboBox<BuildingItem>();
    private final TextField apartmentNumberField = new TextField();
    private final TextField areaField = new TextField();

    public ApartmentManagementWindow() {
        setTitle("Управление квартирами");

        configureTable();
        refreshBuildings();
        refreshTable();

        idField.setEditable(false);

        Label title = new Label("Квартиры");

        Button addButton = new Button("Добавить");
        Button updateButton = new Button("Изменить");
        Button deleteButton = new Button("Удалить");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                idField.setText(String.valueOf(newVal.getApartmentId()));
                apartmentNumberField.setText(String.valueOf(newVal.getApartmentNumber()));
                areaField.setText(String.valueOf(newVal.getArea()));
                selectBuilding(newVal.getBuildingId());
            }
        });

        addButton.setOnAction(e -> {
            try {
                BuildingItem building = buildingBox.getValue();
                if (building == null) {
                    UiAlerts.showWarning("Ошибка ввода", "Выберите дом");
                    return;
                }

                int number = Integer.parseInt(apartmentNumberField.getText().trim());
                BigDecimal area = new BigDecimal(areaField.getText().trim());

                if (number <= 0) {
                    UiAlerts.showWarning("Ошибка ввода", "Номер квартиры должен быть больше нуля");
                    return;
                }

                if (area.compareTo(BigDecimal.ZERO) <= 0) {
                    UiAlerts.showWarning("Ошибка ввода", "Площадь должна быть больше нуля");
                    return;
                }

                repository.addApartment(building.getBuildingId(), number, area);
                refreshTable();
                clearForm();

            } catch (NumberFormatException ex) {
                UiAlerts.showWarning("Ошибка ввода", "Номер квартиры и площадь должны быть числами");
            } catch (Exception ex) {
                UiAlerts.showError("Ошибка", ex.getMessage());
            }
        });

        updateButton.setOnAction(e -> {
            try {
                if (!confirmDanger("Изменение квартиры может затронуть связанные данные жильца, показаний и оплат.")) {
                    return;
                }
                int apartmentId = Integer.parseInt(idField.getText().trim());
                BuildingItem building = buildingBox.getValue();
                if (building == null) {
                    throw new IllegalArgumentException("Выберите дом");
                }

                repository.updateApartment(
                        apartmentId,
                        building.getBuildingId(),
                        Integer.parseInt(apartmentNumberField.getText().trim()),
                        new BigDecimal(areaField.getText().trim())
                );
                refreshTable();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        deleteButton.setOnAction(e -> {
            try {
                int apartmentId = Integer.parseInt(idField.getText().trim());
                if (!confirmDanger("Удаление квартиры удалит привязанного жильца, его показания и оплаты.")) {
                    return;
                }
                repository.deleteApartment(apartmentId);
                refreshTable();
                clearForm();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.add(new Label("ID"), 0, 0);
        form.add(idField, 1, 0);
        form.add(new Label("Дом"), 0, 1);
        form.add(buildingBox, 1, 1);
        form.add(new Label("Номер квартиры"), 0, 2);
        form.add(apartmentNumberField, 1, 2);
        form.add(new Label("Площадь"), 0, 3);
        form.add(areaField, 1, 3);

        HBox buttons = new HBox(10, addButton, updateButton, deleteButton);

        VBox root = new VBox(12, title, form, buttons, table);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 900, 520));
    }

    private void configureTable() {
        TableColumn<ApartmentItem, Integer> idCol = new TableColumn<ApartmentItem, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<ApartmentItem, Integer>("apartmentId"));
        idCol.setPrefWidth(80);

        TableColumn<ApartmentItem, String> buildingCol = new TableColumn<ApartmentItem, String>("Дом");
        buildingCol.setCellValueFactory(new PropertyValueFactory<ApartmentItem, String>("buildingAddress"));
        buildingCol.setPrefWidth(380);

        TableColumn<ApartmentItem, Integer> numberCol = new TableColumn<ApartmentItem, Integer>("Квартира");
        numberCol.setCellValueFactory(new PropertyValueFactory<ApartmentItem, Integer>("apartmentNumber"));
        numberCol.setPrefWidth(100);

        TableColumn<ApartmentItem, BigDecimal> areaCol = new TableColumn<ApartmentItem, BigDecimal>("Площадь");
        areaCol.setCellValueFactory(new PropertyValueFactory<ApartmentItem, BigDecimal>("area"));
        areaCol.setPrefWidth(120);

        table.getColumns().addAll(idCol, buildingCol, numberCol, areaCol);
        table.setPlaceholder(new Label("Нет квартир"));
    }

    private void refreshBuildings() {
        try {
            List<BuildingItem> buildings = repository.loadBuildings();
            buildingBox.setItems(FXCollections.observableArrayList(buildings));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<ApartmentItem> items = repository.loadApartments();
            table.setItems(FXCollections.observableArrayList(items));
            refreshBuildings();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void selectBuilding(int buildingId) {
        for (BuildingItem item : buildingBox.getItems()) {
            if (item.getBuildingId() == buildingId) {
                buildingBox.getSelectionModel().select(item);
                return;
            }
        }
    }

    private void clearForm() {
        idField.clear();
        apartmentNumberField.clear();
        areaField.clear();
        buildingBox.getSelectionModel().clearSelection();
    }

    private boolean confirmDanger(String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Внимание");
        alert.setContentText(text);
        return alert.showAndWait().isPresent();
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}