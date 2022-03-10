package entities;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Item {

    private BigDecimal cost;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;

    private Product product;
}
