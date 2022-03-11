import logical.IntervalLogical;
import logical.OrderLogical;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationStarter {

    public static void main(String[] args){
        orchestrate(args);
    }

    /**
     * This methods will validate the input and determine values for all the options (it will use default values in case they were not provided by user)
     *
     * @param args
     */
    private static void orchestrate(String[] args){
        try {
            if(args.length >= 2){

                var useNow = Boolean.FALSE;
                var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                var dateTime1 = LocalDateTime.parse(args[0], formatter);
                var dateTime2 = LocalDateTime.parse(args[1], formatter);

                if(args.length > 2) {
                    List<String> givenArguments = Arrays.stream(args)
                            .skip(2)
                            .collect(Collectors.toList());

                    Optional<Integer> timeoutLimit = Optional.empty();
                    List<String> givenIntervals = new ArrayList<String>();


                    for(String argument : givenArguments){
                        if(argument.contains("to")){
                            try {
                                timeoutLimit = Optional.of(Integer.parseInt(argument.substring(2)));
                            } catch (Exception e){
                                System.out.println("Given Time out is invalid");
                            }
                        } else if (argument.equals("n")){
                            useNow = Boolean.TRUE;
                        }
                        else {
                            givenIntervals.add(argument);
                        }
                    }

                    if(givenIntervals.size() > 0){
                        if(IntervalLogical.validateIntervals(givenIntervals)){
                            if(timeoutLimit.isPresent()) {
                                System.out.println(OrderLogical.getOrdersInsidePeriod(dateTime1, dateTime2, givenIntervals, timeoutLimit.get(), useNow));
                            } else {
                                System.out.println(OrderLogical.getOrdersInsidePeriod(dateTime1, dateTime2, givenIntervals, useNow));
                            }
                        } else {
                            System.out.println("The Given Intervals are invalid, please check the documentation and try again");
                        }
                    } else {
                        if(timeoutLimit.isPresent()) {
                            System.out.println(OrderLogical.getOrdersInsidePeriod(dateTime1, dateTime2, timeoutLimit.get(), useNow));
                        } else {
                            System.out.println(OrderLogical.getOrdersInsidePeriod(dateTime1, dateTime2, useNow));
                        }
                    }
                } else {
                    System.out.println(OrderLogical.getOrdersInsidePeriod(dateTime1, dateTime2, Boolean.FALSE));
                }
            } else {
                System.out.println("Wrong number of arguments, please check documentation and try again");
            }
        } catch(Exception e){
            System.out.println("Unespected Error: " + e.getMessage());
        }
    }
}
