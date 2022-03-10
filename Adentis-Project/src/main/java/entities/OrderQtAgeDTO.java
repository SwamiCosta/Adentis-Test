package entities;

import lombok.Data;
import utils.Age;

@Data
public class OrderQtAgeDTO {

    private Integer quantity;
    private String age;

    public OrderQtAgeDTO (Integer quantity, String age){
        this.setQuantity(quantity);
        this.setAge(age);
    }
}

