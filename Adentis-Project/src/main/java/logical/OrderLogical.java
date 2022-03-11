package logical;

import entities.Item;
import entities.Order;
import entities.Product;
import entities.ProductDateDTO;
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

    private static final String TIMEOUT_MESSAGE = "Timeout Limit Time expired, check the OutputData file later to see the results";

    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, List<String> intervals, Boolean useNow) throws IOException, InterruptedException {
        return getOrdersInsidePeriod(date1, date2, intervals,5, useNow);
    }

    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, Boolean useNow) throws IOException, InterruptedException {
        return getOrdersInsidePeriod(date1, date2, 5, useNow);
    }

    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, Integer timeoutLimit, Boolean useNow) throws IOException, InterruptedException {
        return getOrdersInsidePeriod(date1, date2, Arrays.asList(Age.ONE_THREE.getText(),
                Age.FOUR_SIX.getText(), Age.SEVEN_TWELVE.getText(), Age.TWELVE_PLUS.getText()
        ), timeoutLimit, useNow);
    }

    /**
     * Gets an output classifying Age (in months) and the amount of orders with products in that age. The orders must be inside a range of dates
     *
     * @param date1 Date 1 to determine the range of orders
     * @param date2 Date 2 to determine the range of order
     * @param intervals Intervals of months (ages) to classify the products
     * @param timeoutLimit A timeout limit in seconds in case the process takes to long
     * @param useNow A Boolean option to consider today's date instead of the order date in the moment of calculating the product age.
     * @return A String having Age (in months) and the amount of orders with products in that age.
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getOrdersInsidePeriod(LocalDateTime date1, LocalDateTime date2, List<String> intervals, Integer timeoutLimit, Boolean useNow) throws IOException, InterruptedException {
        List<Order> ordersInRange;

        if(date1.isBefore(date2)){
            ordersInRange = filterOrdersByRange(date1, date2);
        } else {
            ordersInRange = filterOrdersByRange(date2, date1);
        }

        List<ProductDateDTO> productList = new ArrayList<>();

        for (Order order: ordersInRange) {
            for (Item item: order.getItemList()) {
                productList.add(new ProductDateDTO(item.getProduct(), useNow ? LocalDateTime.now() : order.getOrderDate()));
            }
        }

        LocalDateTime dateToConsider =  date1;

        return calculateAgeAndQuantity(productList, intervals, timeoutLimit);
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
     * Calculate all product ages and them classify them in a set of intervals. The process is divided in one thread for each interval.
     * Each thread writes in the file OutputData, and then this method reads the file after a moment and returns its contents.
     *
     * @param productList List of all products to be classified by age
     * @param intervals Set of intervals (Ages) to classify the products in
     * @param timeoutLimit A timeout limit in case the process takes to much time.
     * @return The complete output of Age and amount of orders made with products in that age
     * @throws IOException
     * @throws InterruptedException
     */
    private static String calculateAgeAndQuantity(List<ProductDateDTO> productList, List<String> intervals, Integer timeoutLimit) throws IOException, InterruptedException {

        new FileWriter("OutputData", false).close();

        for (String interval: intervals){
            Thread thread = new Thread(() -> findProductsInInterval(productList, interval));
            thread.start();
        }

        return recursiveOutputCheck(intervals.size(), timeoutLimit);
    }

    /**
     * This method works as a listener to the OutputData file. As soon as it gets filed with all lines, then its content is returned
     *
     * @param intervalSize The amount of lines to count
     * @param timeoutLimit A limit of seconds to keep trying
     * @return The file content or a message of Timeout
     * @throws IOException
     * @throws InterruptedException
     */
    private static String recursiveOutputCheck(Integer intervalSize, Integer timeoutLimit) throws IOException, InterruptedException {
        FileReader fileReader = new FileReader("OutputData");
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        if(timeoutLimit == 0){
            return TIMEOUT_MESSAGE;
        } else {
            var index = 0;
            StringBuilder finalContent = new StringBuilder();
            String sCurrentLine;
            while ((sCurrentLine = bufferedReader.readLine()) != null){
                finalContent.append(sCurrentLine).append("\n");
                index++;
            }

            if((index == intervalSize)){
                return finalContent.toString();
            } else{
                Thread.sleep(1000);
                return recursiveOutputCheck(intervalSize, timeoutLimit-1);
            }
        }
    }

    /**
     * This method will determine if a product age is a certain interval based on its creation date compared to a determined date (now or the order date,
     *  depending on the user input).
     * The result is written in the file OutputData
     *
     * @param productList The list of All products to be classified
     * @param interval The interval where the product must be classified.
     */
    private static void findProductsInInterval(List<ProductDateDTO> productList, String interval)  {

        Integer quantity = 0;

        Character firstCharacter = interval.charAt(0);

        if(firstCharacter == '>'){
            Integer firstNumber = Integer.parseInt(interval.substring(1));

            for (ProductDateDTO productDTO: productList) {
                LocalDateTime monthsBefore = productDTO.getDateToConsider().minusMonths(firstNumber);

                if(productDTO.getProduct().getCreationDate().isBefore(monthsBefore)){
                    quantity++;
                }
            }
        } else if(firstCharacter == '<') {
            Integer firstNumber = Integer.parseInt(interval.substring(1));

            for (ProductDateDTO productDTO: productList) {
                LocalDateTime monthsAfter = productDTO.getDateToConsider().minusMonths(firstNumber);

                if(productDTO.getProduct().getCreationDate().isAfter(monthsAfter)){
                    quantity++;
                }
            }
        } else {
            Integer firstNumber = Integer.parseInt(interval.substring(0, interval.indexOf('-')));
            Integer secondNumber = Integer.parseInt(interval.substring(interval.indexOf('-') + 1));

            for (ProductDateDTO productDTO : productList) {
                LocalDateTime monthsBefore = productDTO.getDateToConsider().minusMonths(firstNumber);
                LocalDateTime monthsAfter = productDTO.getDateToConsider().minusMonths(secondNumber);

                if (productDTO.getProduct().getCreationDate().isBefore(monthsBefore) && productDTO.getProduct().getCreationDate().isAfter(monthsAfter)) {
                    quantity++;
                }
            }
        }

        String contents = interval + " months: " + quantity.toString() +" orders\n";

//        try {
//            Thread.sleep(15000);
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
