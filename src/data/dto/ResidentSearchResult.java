package data.dto;

public class ResidentSearchResult {

    private final int residentId;
    private final String lastName;
    private final String firstName;
    private final String buildingAddress;
    private final int apartmentNumber;

    public ResidentSearchResult(int residentId, String lastName, String firstName,
                                String buildingAddress, int apartmentNumber) {
        this.residentId = residentId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.buildingAddress = buildingAddress;
        this.apartmentNumber = apartmentNumber;
    }

    public int getResidentId() {
        return residentId;
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

    public String getFullAddress() {
        return buildingAddress + ", кв. " + apartmentNumber;
    }
}