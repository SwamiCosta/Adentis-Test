package entities;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductDateDTO {

    private Product product;
    private LocalDateTime dateToConsider;

    public ProductDateDTO(Product product, LocalDateTime dateToConsider){
        this.setProduct(product);
        this.setDateToConsider(dateToConsider);
    }
}
