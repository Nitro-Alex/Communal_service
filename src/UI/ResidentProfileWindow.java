package UI;

import data.ResidentRepository;
import data.dto.ResidentProfile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class ResidentProfileWindow extends Stage {

    private final ResidentRepository repository = new ResidentRepository();
    private final int residentId;

    public ResidentProfileWindow(int residentId, String titleText) {
        this.residentId = residentId;
        setTitle(titleText);

        ResidentProfile profile = loadProfile();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        addRow(grid, 0, "Фамилия:", profile.getLastName());
        addRow(grid, 1, "Имя:", profile.getFirstName());
        addRow(grid, 2, "ID:", String.valueOf(profile.getResidentId()));
        addRow(grid, 3, "Адрес:", profile.getFullAddress());
        addRow(grid, 4, "Площадь:", profile.getApartmentArea() + " м?");
        addRow(grid, 5, "Льгота:", profile.getBenefitLabel());
        addRow(grid, 6, "Задолженность:", format(profile.getCurrentDebt()) + " руб.");

        VBox root = new VBox(12, grid);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        setScene(new Scene(root, 420, 280));
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

    private void addRow(GridPane grid, int row, String label, String value) {
        grid.add(new Label(label), 0, row);
        grid.add(new Label(value), 1, row);
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%.2f", value.doubleValue());
    }
}