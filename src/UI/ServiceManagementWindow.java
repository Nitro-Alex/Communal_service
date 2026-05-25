package UI;

import data.OperatorRepository;
import data.dto.ServiceItem;
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

public class ServiceManagementWindow extends Stage {

    private final OperatorRepository repository = new OperatorRepository();
    private final TableView<ServiceItem> table = new TableView<ServiceItem>();

    private final TextField nameField = new TextField();
    private final TextField unitField = new TextField();
    private final TextField priceField = new TextField();

    public ServiceManagementWindow() {
        setTitle("Тарифы");

        configureTable();
        refreshTable();

        nameField.setEditable(false);
        unitField.setEditable(false);

        Button updateButton = new Button("Обновить цену");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
                unitField.setText(newVal.getUnit());
                priceField.setText(String.valueOf(newVal.getPrice()));
            }
        });

        updateButton.setOnAction(e -> {
            try {
                if (nameField.getText().trim().isEmpty()) {
                    UiAlerts.showWarning("Ошибка ввода", "Выберите услугу");
                    return;
                }

                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    UiAlerts.showWarning("Ошибка ввода", "Цена не может быть отрицательной");
                    return;
                }

                if (!confirmDanger("Изменение цены повлияет на будущие начисления по этой услуге.")) {
                    return;
                }

                repository.updateServicePrice(nameField.getText().trim(), price);
                refreshTable();

            } catch (NumberFormatException ex) {
                UiAlerts.showWarning("Ошибка ввода", "Цена должна быть числом");
            } catch (Exception ex) {
                UiAlerts.showError("Ошибка", ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.add(new Label("Услуга"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Ед."), 0, 1);
        form.add(unitField, 1, 1);
        form.add(new Label("Цена"), 0, 2);
        form.add(priceField, 1, 2);

        VBox root = new VBox(12, new Label("Тарифы"), form, updateButton, table);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 640, 450));
    }

    private void configureTable() {
        TableColumn<ServiceItem, String> nameCol = new TableColumn<ServiceItem, String>("Услуга");
        nameCol.setCellValueFactory(new PropertyValueFactory<ServiceItem, String>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<ServiceItem, String> unitCol = new TableColumn<ServiceItem, String>("Ед.");
        unitCol.setCellValueFactory(new PropertyValueFactory<ServiceItem, String>("unit"));
        unitCol.setPrefWidth(80);

        TableColumn<ServiceItem, BigDecimal> priceCol = new TableColumn<ServiceItem, BigDecimal>("Цена");
        priceCol.setCellValueFactory(new PropertyValueFactory<ServiceItem, BigDecimal>("price"));
        priceCol.setPrefWidth(120);

        table.getColumns().addAll(nameCol, unitCol, priceCol);
        table.setPlaceholder(new Label("Нет услуг"));
    }

    private void refreshTable() {
        try {
            List<ServiceItem> items = repository.loadServices();
            table.setItems(FXCollections.observableArrayList(items));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
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