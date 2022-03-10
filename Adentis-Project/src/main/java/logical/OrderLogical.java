package logical;

import entities.Item;
import entities.Order;
import entities.Product;
import utils.Age;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderLogical {

    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, List<String> intervals, Boolean useNow) throws IOException, InterruptedException {
        return getOrdersInsidePeriod(date1, date2, intervals,1, useNow);
    }

    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, Boolean useNow) throws IOException, InterruptedException {
        return getOrdersInsidePeriod(date1, date2, 1, useNow);
    }

    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, Integer timeoutLimit, Boolean useNow) throws IOException, InterruptedException {
        return getOrdersInsidePeriod(date1, date2, Arrays.asList(Age.ONE_THREE.getText(),
                Age.FOUR_SIX.getText(), Age.SEVEN_TWELVE.getText(), Age.TWELVE_PLUS.getText()
        ), timeoutLimit, useNow);
    }

    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, List<String> intervals, Integer timeoutLimit, Boolean useNow) throws IOException, InterruptedException {
        List<Order> ordersInRange;

        if(date1.isBefore(date2)){
            ordersInRange = filterOrdersByRange(date1, date2);
        } else {
            ordersInRange = filterOrdersByRange(date2, date1);
        }

        List<Product> productList = new ArrayList<Product>();

        for (Order order: ordersInRange) {
            for (Item item: order.getItemList()) {
                productList.add(item.getProduct());
            }
        }

        LocalDateTime dateToConsider = useNow ? LocalDateTime.now() : date1;

        return calculateAgeAndQuantity(productList, intervals, timeoutLimit, dateToConsider);
    }

    /**
     * Receives two dates and finds all Orders that were processed in a period between those dates
     *
     * The orders are retrieved from a pre-determined set, built by a Factory class
     *
     * @param initialDate the initial date to be checked
     * @param endDate the end date to be checked
     * @return a list of all Orders that happened in the given period
     */
    private static List<Order> filterOrdersByRange(LocalDateTime initialDate, LocalDateTime endDate){

        var orderFactory = OrderFactory.getInstance();

        return orderFactory.getOrders().stream()
                .filter(e -> e.getOrderDate().isAfter(initialDate))
                .filter(e -> e.getOrderDate().isBefore(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Receives a list of Products and calculates their age based on predefined Sets of Months
     *
     * @param productList the Product List to be evaluated
     * @return A list of Data binding and age to quantity of Orders made
     */
    private static String calculateAgeAndQuantity(List<Product> productList, List<String> intervals, Integer timeoutLimit, LocalDateTime dateToConsider) throws IOException, InterruptedException {

        new FileWriter("OutputData", false).close();

        for (String interval: intervals){
            Thread thread = new Thread(() -> findProductsInInterval(productList, interval, dateToConsider));
            thread.start();
        }

        Thread.sleep(timeoutLimit*1000);

        FileReader fileReader = new FileReader("OutputData");

        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder finalContent = new StringBuilder();
        String sCurrentLine;
        while ((sCurrentLine = bufferedReader.readLine()) != null)
        {
            finalContent.append(sCurrentLine).append("\n");
        }

        return finalContent.toString();
    }

    private static void findProductsInInterval(List<Product> productList, String interval, LocalDateTime dateToConsider)  {

        Integer quantity = 0;

        Character firstCharacter = interval.charAt(0);

        if(firstCharacter == '>'){
            Integer firstNumber = Integer.parseInt(interval.substring(1));
            LocalDateTime monthsBefore = dateToConsider.minusMonths(firstNumber);

            for (Product product: productList) {
                if(product.getCreationDate().isBefore(monthsBefore)){
                    quantity++;
                }
            }
        } else if(firstCharacter == '<') {
            Integer firstNumber = Integer.parseInt(interval.substring(1));
            LocalDateTime monthsAfter = dateToConsider.minusMonths(firstNumber);

            for (Product product: productList) {
                if(product.getCreationDate().isAfter(monthsAfter)){
                    quantity++;
                }
            }
        } else {
            Integer firstNumber = Integer.parseInt(interval.substring(0, interval.indexOf('-')));
            Integer secondNumber = Integer.parseInt(interval.substring(interval.indexOf('-') + 1));
            LocalDateTime monthsBefore = dateToConsider.minusMonths(firstNumber);
            LocalDateTime monthsAfter = dateToConsider.minusMonths(secondNumber);

            for (Product product : productList) {
                if (product.getCreationDate().isBefore(monthsBefore) && product.getCreationDate().isAfter(monthsAfter)) {
                    quantity++;
                }
            }
        }

        String contents = interval + " months: " + quantity.toString() +" orders\n";

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("OutputData", true);
            fileWriter.append(contents);
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Unespected Error: " + ex.getMessage());
        }
    }
}
