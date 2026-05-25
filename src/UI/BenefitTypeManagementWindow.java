package UI;

import data.OperatorRepository;
import data.dto.BenefitTypeItem;
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

public class BenefitTypeManagementWindow extends Stage {

    private final OperatorRepository repository = new OperatorRepository();
    private final TableView<BenefitTypeItem> table = new TableView<BenefitTypeItem>();

    private final TextField categoryField = new TextField();
    private final TextField percentField = new TextField();

    public BenefitTypeManagementWindow() {
        setTitle("Льготы");

        configureTable();
        refreshTable();

        categoryField.setEditable(false);

        Button updateButton = new Button("Обновить процент");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                categoryField.setText(newVal.getCategory());
                percentField.setText(String.valueOf(newVal.getDiscountPercent()));
            }
        });

        updateButton.setOnAction(e -> {
            try {
                if (categoryField.getText().trim().isEmpty()) {
                    UiAlerts.showWarning("Ошибка ввода", "Выберите категорию льготы");
                    return;
                }

                BigDecimal percent = new BigDecimal(percentField.getText().trim());
                if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal("100")) > 0) {
                    UiAlerts.showWarning("Ошибка ввода", "Процент льготы должен быть от 0 до 100");
                    return;
                }

                if (!confirmDanger("Изменение процента льготы повлияет на будущие начисления для жильцов этой категории.")) {
                    return;
                }

                repository.updateBenefitPercent(categoryField.getText().trim(), percent);
                refreshTable();

            } catch (NumberFormatException ex) {
                UiAlerts.showWarning("Ошибка ввода", "Процент льготы должен быть числом");
            } catch (Exception ex) {
                UiAlerts.showError("Ошибка", ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.add(new Label("Категория"), 0, 0);
        form.add(categoryField, 1, 0);
        form.add(new Label("Процент"), 0, 1);
        form.add(percentField, 1, 1);

        VBox root = new VBox(12, new Label("Категории льгот"), form, updateButton, table);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 520, 420));
    }

    private void configureTable() {
        TableColumn<BenefitTypeItem, String> categoryCol = new TableColumn<BenefitTypeItem, String>("Категория");
        categoryCol.setCellValueFactory(new PropertyValueFactory<BenefitTypeItem, String>("category"));
        categoryCol.setPrefWidth(260);

        TableColumn<BenefitTypeItem, BigDecimal> percentCol = new TableColumn<BenefitTypeItem, BigDecimal>("Процент");
        percentCol.setCellValueFactory(new PropertyValueFactory<BenefitTypeItem, BigDecimal>("discountPercent"));
        percentCol.setPrefWidth(100);

        table.getColumns().addAll(categoryCol, percentCol);
        table.setPlaceholder(new Label("Нет льгот"));
    }

    private void refreshTable() {
        try {
            List<BenefitTypeItem> items = repository.loadBenefitTypes();
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