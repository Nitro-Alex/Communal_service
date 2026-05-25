package UI;

import data.OperatorRepository;
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

import java.util.List;

public class BuildingManagementWindow extends Stage {

    private final OperatorRepository repository = new OperatorRepository();
    private final TableView<BuildingItem> table = new TableView<BuildingItem>();

    private final TextField idField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField apartmentsCountField = new TextField();

    public BuildingManagementWindow() {
        setTitle("Управление домами");

        configureTable();
        refreshTable();

        Label title = new Label("Дома");

        idField.setPromptText("ID");
        idField.setEditable(false);

        Label addressLabel = new Label("Адрес");
        Label countLabel = new Label("Количество квартир");

        Button addButton = new Button("Добавить");
        Button updateButton = new Button("Изменить");
        Button deleteButton = new Button("Удалить");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                idField.setText(String.valueOf(newVal.getBuildingId()));
                addressField.setText(newVal.getAddress());
                apartmentsCountField.setText(String.valueOf(newVal.getApartmentsCount()));
            }
        });

        addButton.setOnAction(e -> {
            try {
                String address = addressField.getText().trim();
                int count = Integer.parseInt(apartmentsCountField.getText().trim());

                if (address.isEmpty()) {
                    UiAlerts.showWarning("Ошибка ввода", "Адрес не может быть пустым");
                    return;
                }

                if (count <= 0) {
                    UiAlerts.showWarning("Ошибка ввода", "Количество квартир должно быть больше нуля");
                    return;
                }

                repository.addBuilding(address, count);
                refreshTable();
                clearForm();

            } catch (NumberFormatException ex) {
                UiAlerts.showWarning("Ошибка ввода", "Количество квартир должно быть числом");
            } catch (Exception ex) {
                UiAlerts.showError("Ошибка", ex.getMessage());
            }
        });

        updateButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                if (!confirmDanger("Изменение дома может затронуть связанные квартиры, жильцов, показания и оплаты.")) {
                    return;
                }
                repository.updateBuilding(id,
                        addressField.getText().trim(),
                        Integer.parseInt(apartmentsCountField.getText().trim()));
                refreshTable();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        deleteButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                if (!confirmDanger("Удаление дома удалит связанные квартиры, жильцов, показания и оплаты.")) {
                    return;
                }
                repository.deleteBuilding(id);
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
        form.add(addressLabel, 0, 1);
        form.add(addressField, 1, 1);
        form.add(countLabel, 0, 2);
        form.add(apartmentsCountField, 1, 2);

        HBox buttons = new HBox(10, addButton, updateButton, deleteButton);

        VBox root = new VBox(12, title, form, buttons, table);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 760, 480));
    }

    private void configureTable() {
        TableColumn<BuildingItem, Integer> idCol = new TableColumn<BuildingItem, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<BuildingItem, Integer>("buildingId"));
        idCol.setPrefWidth(80);

        TableColumn<BuildingItem, String> addressCol = new TableColumn<BuildingItem, String>("Адрес");
        addressCol.setCellValueFactory(new PropertyValueFactory<BuildingItem, String>("address"));
        addressCol.setPrefWidth(420);

        TableColumn<BuildingItem, Integer> countCol = new TableColumn<BuildingItem, Integer>("Квартир");
        countCol.setCellValueFactory(new PropertyValueFactory<BuildingItem, Integer>("apartmentsCount"));
        countCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, addressCol, countCol);
        table.setPlaceholder(new Label("Нет домов"));
    }

    private void refreshTable() {
        try {
            List<BuildingItem> items = repository.loadBuildings();
            table.setItems(FXCollections.observableArrayList(items));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void clearForm() {
        idField.clear();
        addressField.clear();
        apartmentsCountField.clear();
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