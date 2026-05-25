package data.dto;

public class BuildingItem {

    private final int buildingId;
    private final String address;
    private final int apartmentsCount;

    public BuildingItem(int buildingId, String address, int apartmentsCount) {
        this.buildingId = buildingId;
        this.address = address;
        this.apartmentsCount = apartmentsCount;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public String getAddress() {
        return address;
    }

    public int getApartmentsCount() {
        return apartmentsCount;
    }

    public String toString() {
        return buildingId + " | " + address;
    }
}