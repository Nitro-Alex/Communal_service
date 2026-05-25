package data.dto;

import java.math.BigDecimal;

public class BenefitTypeItem {

    private final String category;
    private final BigDecimal discountPercent;

    public BenefitTypeItem(String category, BigDecimal discountPercent) {
        this.category = category;
        this.discountPercent = discountPercent;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public String toString() {
        return category;
    }
}