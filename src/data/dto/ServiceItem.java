package data.dto;

import java.math.BigDecimal;

public class ServiceItem {

    private final String name;
    private final String unit;
    private final BigDecimal price;

    public ServiceItem(String name, String unit, BigDecimal price) {
        this.name = name;
        this.unit = unit;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String toString() {
        return name;
    }
}