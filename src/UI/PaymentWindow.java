package UI;

import data.ResidentRepository;
import data.dto.ResidentProfile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentWindow extends Stage {

    private final ResidentRepository repository = new ResidentRepository();
    private final int residentId;
    private Label debtValueLabel;
    private Label benefitValueLabel;

    public PaymentWindow(int residentId) {
        this.residentId = residentId;
        setTitle("Оплата услуг");

        ResidentProfile profile = loadProfile();

        Label title = new Label("Оплата услуг");
        title.setStyle("-fx-font-size: 16px;");

        benefitValueLabel = new Label(profile.getBenefitLabel());
        debtValueLabel = new Label("Текущая задолженность: " + format(profile.getCurrentDebt()) + " руб.");

        Label amountLabel = new Label("Сумма оплаты");
        TextField amountField = new TextField();

        Label resultLabel = new Label();
        Button payButton = new Button("Оплатить");

        payButton.setOnAction(e -> {
            try {

                BigDecimal amount =
                        new BigDecimal(
                                amountField.getText().trim()
                        );

                if(amount.compareTo(BigDecimal.ZERO) <= 0){

                    UiAlerts.showWarning(
                            "Ошибка ввода",
                            "Сумма оплаты должна быть больше нуля"
                    );

                    return;
                }

                //ResidentProfile profile =loadProfile();

                BigDecimal currentDebt =
                        profile.getCurrentDebt();

                if(currentDebt == null){
                    currentDebt = BigDecimal.ZERO;
                }

                if(currentDebt.compareTo(BigDecimal.ZERO) <= 0){

                    UiAlerts.showWarning(
                            "Оплата невозможна",
                            "Задолженность отсутствует"
                    );

                    return;
                }

                if(amount.compareTo(currentDebt) > 0){

                    UiAlerts.showWarning(
                            "Ошибка ввода",
                            "Сумма оплаты не может превышать текущую задолженность: "
                            + currentDebt.setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            + " руб."
                    );

                    return;
                }

                repository.registerPayment(
                        residentId,
                        amount
                );

                ResidentProfile updated =
                        loadProfile();

                debtValueLabel.setText(
                        "Новая задолженность: "
                        + format(
                                updated.getCurrentDebt()
                        )
                        + " руб."
                );

                resultLabel.setText(
                        "Оплата произведена успешно"
                );

                amountField.clear();

            }
            catch(NumberFormatException ex){

                UiAlerts.showWarning(
                        "Ошибка ввода",
                        "Введите корректную сумму оплаты"
                );

            }
            catch(Exception ex){

                UiAlerts.showError(
                        "Ошибка",
                        ex.getMessage()
                );
            }

        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        form.add(amountLabel, 0, 0);
        form.add(amountField, 1, 0);
        form.add(payButton, 1, 1);

        VBox root = new VBox(10, title, benefitValueLabel, debtValueLabel, form, resultLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 360, 220));
    }

    private ResidentProfile loadProfile() {
        try {
            ResidentProfile profile = repository.loadResidentProfile(residentId);
            if (profile == null) {
                throw new RuntimeException("Жилец не найден");
            }
            return profile;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%.2f", value.doubleValue());
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}