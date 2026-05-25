package data.dto;

import java.math.BigDecimal;

public class ReadingRecord {

    private final int readingId;
    private final String serviceName;
    private final int month;
    private final int year;
    private final BigDecimal value;
    private final BigDecimal charge;

    public ReadingRecord(int readingId, String serviceName, int month, int year,
                         BigDecimal value, BigDecimal charge) {
        this.readingId = readingId;
        this.serviceName = serviceName;
        this.month = month;
        this.year = year;
        this.value = value;
        this.charge = charge;
    }

    public int getReadingId() {
        return readingId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getValue() {
        return value;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public String getPeriod() {
        return String.format("%02d.%04d", month, year);
    }
}