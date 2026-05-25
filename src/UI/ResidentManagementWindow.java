package UI;

import data.OperatorRepository;
import data.dto.ApartmentItem;
import data.dto.BenefitTypeItem;
import data.dto.ResidentProfile;
import data.dto.ResidentSearchResult;
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

public class ResidentManagementWindow extends Stage {

    private final OperatorRepository repository = new OperatorRepository();
    private final TableView<ResidentSearchResult> table = new TableView<ResidentSearchResult>();

    private final TextField idField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField firstNameField = new TextField();
    private final ComboBox<ApartmentItem> apartmentBox = new ComboBox<ApartmentItem>();
    private final ComboBox<String> benefitBox = new ComboBox<String>();

    public ResidentManagementWindow() {
        setTitle("Управление жильцами");

        configureTable();
        refreshApartments();
        refreshBenefits();
        refreshTable();

        idField.setEditable(false);

        Label title = new Label("Жильцы");

        benefitBox.setItems(FXCollections.observableArrayList("Без льготы"));

        Button addButton = new Button("Добавить");
        Button updateButton = new Button("Изменить");
        Button deleteButton = new Button("Удалить");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal.getResidentId());
            }
        });

        addButton.setOnAction(e -> {
            try {
                ApartmentItem apartment = apartmentBox.getValue();
                if (apartment == null) {
                    throw new IllegalArgumentException("Выберите квартиру");
                }

                repository.addResident(
                        lastNameField.getText().trim(),
                        firstNameField.getText().trim(),
                        apartment.getApartmentId(),
                        selectedBenefit()
                );
                refreshTable();
                clearForm();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        updateButton.setOnAction(e -> {
            try {
                if (!confirmDanger("Изменение жильца может затронуть связанные показания и оплаты.")) {
                    return;
                }

                ApartmentItem apartment = apartmentBox.getValue();
                if (apartment == null) {
                    throw new IllegalArgumentException("Выберите квартиру");
                }

                repository.updateResident(
                        Integer.parseInt(idField.getText().trim()),
                        lastNameField.getText().trim(),
                        firstNameField.getText().trim(),
                        apartment.getApartmentId(),
                        selectedBenefit()
                );
                refreshTable();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        deleteButton.setOnAction(e -> {
            try {
                int residentId = Integer.parseInt(idField.getText().trim());
                if (!confirmDanger("Удаление жильца удалит его показания и оплаты.")) {
                    return;
                }
                repository.deleteResident(residentId);
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
        form.add(new Label("Фамилия"), 0, 1);
        form.add(lastNameField, 1, 1);
        form.add(new Label("Имя"), 0, 2);
        form.add(firstNameField, 1, 2);
        form.add(new Label("Квартира"), 0, 3);
        form.add(apartmentBox, 1, 3);
        form.add(new Label("Льгота"), 0, 4);
        form.add(benefitBox, 1, 4);

        HBox buttons = new HBox(10, addButton, updateButton, deleteButton);

        VBox root = new VBox(12, title, form, buttons, table);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 960, 560));
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
        addressCol.setPrefWidth(460);

        table.getColumns().addAll(idCol, lastNameCol, firstNameCol, addressCol);
        table.setPlaceholder(new Label("Нет жильцов"));
    }

    private void refreshTable() {
        try {
            List<ResidentSearchResult> items = repository.searchResidents("", "");
            table.setItems(FXCollections.observableArrayList(items));
            refreshApartments();
            refreshBenefits();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshApartments() {
        try {
            List<ApartmentItem> apartments = repository.loadApartments();
            apartmentBox.setItems(FXCollections.observableArrayList(apartments));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshBenefits() {
        try {
            List<BenefitTypeItem> benefits = repository.loadBenefitTypes();
            benefitBox.getItems().clear();
            benefitBox.getItems().add("Без льготы");
            for (BenefitTypeItem item : benefits) {
                benefitBox.getItems().add(item.getCategory());
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void fillForm(int residentId) {
        try {
            ResidentProfile profile = repository.loadResidentProfile(residentId);
            if (profile == null) {
                return;
            }

            idField.setText(String.valueOf(profile.getResidentId()));
            lastNameField.setText(profile.getLastName());
            firstNameField.setText(profile.getFirstName());

            for (ApartmentItem item : apartmentBox.getItems()) {
                if (item.getApartmentId() == profile.getApartmentId()) {
                    apartmentBox.getSelectionModel().select(item);
                    break;
                }
            }

            if (profile.getBenefitCategory() == null || profile.getBenefitCategory().trim().length() == 0) {
                benefitBox.getSelectionModel().select("Без льготы");
            } else {
                benefitBox.getSelectionModel().select(profile.getBenefitCategory());
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private String selectedBenefit() {
        String value = benefitBox.getValue();
        if (value == null || "Без льготы".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    private void clearForm() {
        idField.clear();
        lastNameField.clear();
        firstNameField.clear();
        apartmentBox.getSelectionModel().clearSelection();
        benefitBox.getSelectionModel().select("Без льготы");
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