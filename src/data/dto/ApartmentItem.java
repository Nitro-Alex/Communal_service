package data.dto;

import java.math.BigDecimal;

public class ApartmentItem {

    private final int apartmentId;
    private final int buildingId;
    private final String buildingAddress;
    private final int apartmentNumber;
    private final BigDecimal area;

    public ApartmentItem(int apartmentId, int buildingId, String buildingAddress,
                         int apartmentNumber, BigDecimal area) {
        this.apartmentId = apartmentId;
        this.buildingId = buildingId;
        this.buildingAddress = buildingAddress;
        this.apartmentNumber = apartmentNumber;
        this.area = area;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public String getBuildingAddress() {
        return buildingAddress;
    }

    public int getApartmentNumber() {
        return apartmentNumber;
    }

    public BigDecimal getArea() {
        return area;
    }

    public String toString() {
        return apartmentId + " | " + buildingAddress + ", кв. " + apartmentNumber;
    }
}