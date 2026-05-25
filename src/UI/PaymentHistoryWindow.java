package UI;

import data.ResidentRepository;
import data.dto.PaymentRecord;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class PaymentHistoryWindow extends Stage {

    private final ResidentRepository repository = new ResidentRepository();
    private final int residentId;
    private final TableView<PaymentRecord> table = new TableView<PaymentRecord>();

    public PaymentHistoryWindow(int residentId) {
        this.residentId = residentId;
        setTitle("История оплат");

        configureTable();

        Label title = new Label("История оплат");

        Label fromLabel = new Label("Дата с");
        DatePicker fromPicker = new DatePicker();

        Label toLabel = new Label("Дата по");
        DatePicker toPicker = new DatePicker();

        Button applyButton = new Button("Показать");

        applyButton.setOnAction(e -> {
            try {
                LocalDate from = fromPicker.getValue();
                LocalDate to = toPicker.getValue();

                if (from != null && to != null && from.isAfter(to)) {
                    UiAlerts.showWarning("Ошибка ввода", "Начальная дата не может быть позже конечной");
                    return;
                }

                List<PaymentRecord> list = repository.loadPayments(residentId, from, to);
                table.setItems(FXCollections.observableArrayList(list));

            } catch (Exception ex) {
                UiAlerts.showError("Ошибка", ex.getMessage());
            }
        });

        GridPane filters = new GridPane();
        filters.setHgap(10);
        filters.setVgap(8);

        filters.add(fromLabel, 0, 0);
        filters.add(fromPicker, 1, 0);
        filters.add(toLabel, 0, 1);
        filters.add(toPicker, 1, 1);
        filters.add(applyButton, 1, 2);

        VBox root = new VBox(12, title, filters, table);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 520, 380));
    }

    private void configureTable() {
        TableColumn<PaymentRecord, java.time.LocalDate> dateCol = new TableColumn<PaymentRecord, java.time.LocalDate>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<PaymentRecord, java.time.LocalDate>("paymentDate"));
        dateCol.setPrefWidth(140);

        TableColumn<PaymentRecord, java.math.BigDecimal> amountCol = new TableColumn<PaymentRecord, java.math.BigDecimal>("Сумма");
        amountCol.setCellValueFactory(new PropertyValueFactory<PaymentRecord, java.math.BigDecimal>("amount"));
        amountCol.setPrefWidth(120);

        table.getColumns().addAll(dateCol, amountCol);
        table.setPlaceholder(new Label("Нет данных"));
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}