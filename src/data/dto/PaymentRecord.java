package data.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentRecord {

    private final int paymentId;
    private final LocalDate paymentDate;
    private final BigDecimal amount;

    public PaymentRecord(int paymentId, LocalDate paymentDate, BigDecimal amount) {
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
        this.amount = amount;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}