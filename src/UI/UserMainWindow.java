package UI;

import data.AppSession;
import data.ResidentRepository;
import data.dto.ResidentProfile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class UserMainWindow extends Stage {

    private final ResidentRepository repository = new ResidentRepository();
    private final int residentId;

    public UserMainWindow(int residentId) {
        this.residentId = residentId;
        setTitle("Личный кабинет пользователя");

        ResidentProfile profile = loadProfile();

        Label title = new Label("Личный кабинет");
        title.setStyle("-fx-font-size: 18px;");

        Label nameLabel = new Label(profile.getLastName() + " " + profile.getFirstName());
        Label addressLabel = new Label("Адрес: " + profile.getFullAddress());
        Label apartmentLabel = new Label("Квартира: " + profile.getApartmentNumber());
        Label debtLabel = new Label("Текущая задолженность: " + format(profile.getCurrentDebt()) + " руб.");
        Label benefitLabel = new Label(profile.getBenefitLabel());

        Button profileButton = new Button("Полные данные");
        Button readingButton = new Button("Потребление");
        Button paymentHistoryButton = new Button("История оплат");
        Button payButton = new Button("Оплатить");

        profileButton.setOnAction(e -> new ResidentProfileWindow(residentId, "Полные данные жильца").show());
        readingButton.setOnAction(e -> new ReadingHistoryWindow(residentId).show());
        paymentHistoryButton.setOnAction(e -> new PaymentHistoryWindow(residentId).show());
        payButton.setOnAction(e -> new PaymentWindow(residentId).show());

        VBox root = new VBox(10,
                title,
                nameLabel,
                addressLabel,
                apartmentLabel,
                benefitLabel,
                debtLabel,
                profileButton,
                readingButton,
                paymentHistoryButton,
                payButton
        );
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));

        setScene(new Scene(root, 420, 420));
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
}