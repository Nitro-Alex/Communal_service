package UI;

import data.ResidentRepository;
import data.dto.ReadingRecord;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.YearMonth;
import java.util.List;

public class ReadingHistoryWindow extends Stage {

    private final ResidentRepository repository = new ResidentRepository();
    private final int residentId;
    private final TableView<ReadingRecord> table = new TableView<ReadingRecord>();

    public ReadingHistoryWindow(int residentId) {
        this.residentId = residentId;
        setTitle("Потребление");

        configureTable();

        Label title = new Label("История потребления");

        Label fromLabel = new Label("Период с");
        TextField fromMonth = new TextField();
        fromMonth.setPromptText("Месяц");
        TextField fromYear = new TextField();
        fromYear.setPromptText("Год");

        Label toLabel = new Label("Период по");
        TextField toMonth = new TextField();
        toMonth.setPromptText("Месяц");
        TextField toYear = new TextField();
        toYear.setPromptText("Год");

        Label serviceLabel = new Label("Услуга");
        ComboBox<String> serviceBox = new ComboBox<String>();
        serviceBox.getItems().addAll("Все", "Холодная вода", "Горячая вода", "Электричество", "Отопление");
        serviceBox.getSelectionModel().selectFirst();

        Button applyButton = new Button("Показать");

        applyButton.setOnAction(e -> {
            try {
                YearMonth from = parseYearMonth(fromYear.getText(), fromMonth.getText());
                YearMonth to = parseYearMonth(toYear.getText(), toMonth.getText());
                if(from != null && to != null && from.isAfter(to)){
                    showError("Начальный период не может быть позже конечного");
                    return;
                }

                String service = serviceBox.getValue();

                List<ReadingRecord> list = repository.loadReadings(residentId, from, to, service);
                table.setItems(FXCollections.observableArrayList(list));
            }
            catch(IllegalArgumentException ex){
                showError(ex.getMessage());
            }
            catch(Exception ex){
                showError("Ошибка: " + ex.getMessage()
                );
            }
        });

        GridPane filters = new GridPane();
        filters.setHgap(10);
        filters.setVgap(8);

        filters.add(fromLabel, 0, 0);
        filters.add(fromMonth, 1, 0);
        filters.add(fromYear, 2, 0);
        filters.add(toLabel, 0, 1);
        filters.add(toMonth, 1, 1);
        filters.add(toYear, 2, 1);
        filters.add(serviceLabel, 0, 2);
        filters.add(serviceBox, 1, 2);
        filters.add(applyButton, 1, 3);

        VBox root = new VBox(12, title, filters, table);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 720, 420));
    }

    private void configureTable() {
        TableColumn<ReadingRecord, String> periodCol = new TableColumn<ReadingRecord, String>("Период");
        periodCol.setCellValueFactory(new PropertyValueFactory<ReadingRecord, String>("period"));
        periodCol.setPrefWidth(110);

        TableColumn<ReadingRecord, String> serviceCol = new TableColumn<ReadingRecord, String>("Услуга");
        serviceCol.setCellValueFactory(new PropertyValueFactory<ReadingRecord, String>("serviceName"));
        serviceCol.setPrefWidth(180);

        TableColumn<ReadingRecord, java.math.BigDecimal> valueCol = new TableColumn<ReadingRecord, java.math.BigDecimal>("Показание");
        valueCol.setCellValueFactory(new PropertyValueFactory<ReadingRecord, java.math.BigDecimal>("value"));
        valueCol.setPrefWidth(120);

        TableColumn<ReadingRecord, java.math.BigDecimal> chargeCol = new TableColumn<ReadingRecord, java.math.BigDecimal>("Начислено");
        chargeCol.setCellValueFactory(new PropertyValueFactory<ReadingRecord, java.math.BigDecimal>("charge"));
        chargeCol.setPrefWidth(120);

        table.getColumns().addAll(periodCol, serviceCol, valueCol, chargeCol);
        table.setPlaceholder(new Label("Нет данных"));
    }

    private YearMonth parseYearMonth(String yearText, String monthText) {
        String y = yearText == null ? "" : yearText.trim();
        String m = monthText == null ? "" : monthText.trim();
        // оба поля пустые - фильтр не используется
        if(y.isEmpty() && m.isEmpty()){
            return null;
        }
        // заполнено только одно поле
        if(y.isEmpty() || m.isEmpty()){
            throw new IllegalArgumentException("Для периода необходимо заполнить и месяц, и год");
        }

        int year;
        int month;
        try{
            year = Integer.parseInt(y);
            month = Integer.parseInt(m);
        }
        catch(NumberFormatException ex){
            throw new IllegalArgumentException("Месяц и год должны быть числами");
        }
        if(month < 1 || month > 12){
            throw new IllegalArgumentException("Месяц должен быть в диапазоне 1–12");
        }

        if(year < 2000){
            throw new IllegalArgumentException("Год не может быть меньше 2000");
        }

        return YearMonth.of(year, month);
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}