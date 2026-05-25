package data.dto;

import java.math.BigDecimal;

public class ResidentProfile {

    private final int residentId;
    private final int apartmentId;
    private final String lastName;
    private final String firstName;
    private final String buildingAddress;
    private final int apartmentNumber;
    private final BigDecimal apartmentArea;
    private final String benefitCategory;
    private final BigDecimal discountPercent;
    private final BigDecimal currentDebt;

    public ResidentProfile(int residentId,
                           int apartmentId,
                           String lastName,
                           String firstName,
                           String buildingAddress,
                           int apartmentNumber,
                           BigDecimal apartmentArea,
                           String benefitCategory,
                           BigDecimal discountPercent,
                           BigDecimal currentDebt) {
        this.residentId = residentId;
        this.apartmentId = apartmentId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.buildingAddress = buildingAddress;
        this.apartmentNumber = apartmentNumber;
        this.apartmentArea = apartmentArea;
        this.benefitCategory = benefitCategory;
        this.discountPercent = discountPercent;
        this.currentDebt = currentDebt;
    }

    public int getResidentId() {
        return residentId;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getBuildingAddress() {
        return buildingAddress;
    }

    public int getApartmentNumber() {
        return apartmentNumber;
    }

    public BigDecimal getApartmentArea() {
        return apartmentArea;
    }

    public String getBenefitCategory() {
        return benefitCategory;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getCurrentDebt() {
        return currentDebt;
    }

    public String getFullAddress() {
        return buildingAddress + ", кв. " + apartmentNumber;
    }

    public String getBenefitLabel() {
        if (benefitCategory == null || benefitCategory.trim().length() == 0) {
            return "Ћьгота: отсутствует";
        }
        return "Ћьгота: " + benefitCategory + " (" + discountPercent + "%)";
    }
}