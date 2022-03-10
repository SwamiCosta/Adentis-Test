package logical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import entities.Order;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class OrderFactory {

    private static OrderFactory instance;

    private List<Order> orders;

    private OrderFactory(){

        try {

            URL url = Resources.getResource("PresetedOrders");
            String ordersJson = Resources.toString(url, StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            List<Order> orderList = Arrays.asList(mapper.readValue(ordersJson, Order[].class));

            this.orders = orderList;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static OrderFactory getInstance(){
        if(instance == null){
            instance = new OrderFactory();
        }
        return instance;
    }

    public List<Order> getOrders(){
        return orders;
    }
}
