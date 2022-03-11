import entities.OrderQtAgeDTO;
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

        //orchestrate(new String[] {"2021-01-01 00:00:00","2023-01-01 00:00:00"});
        //orchestrate(new String[] {"2021-01-01 00:00:00","2023-01-01 00:00:00", "n"});
        //orchestrate(new String[] {"2021-01-01 00:00:00","2023-01-01 00:00:00", ">11"});
        //orchestrate(new String[] {"2022-01-01 00:00:00","2023-01-01 00:00:00", ">1", "<123", ">123", "5-19", "to10"});
        //orchestrate(new String[] {"2022-01-01 00:00:00","2023-01-01 00:00:00", ">1", "<123", "11-19", "a"});
        //orchestrate(new String[] {"2022-01-01 00:00:00","2023-01-01 00:00:00", "a"});
        //orchestrate(new String[] {"2022-01-01 00:00:00","2023-01-01 00:00:00", "11-1"});
        //orchestrate(new String[] {"2022-01-01 00:00:00","2023-01-01 00:00:00", ">a"});
        //orchestrate(new String[] {"2022-01-01 00:00:00","2023-01-01 00:00:00", "a-21"});
        //orchestrate(new String[] {"2022-01-01 00:00:00","2023-01-01 00:00:00", "21-a"});
        orchestrate(args);
    }

    private static void orchestrate(String[] args){
        try {
            if(args.length >= 2){

                var useNow = Boolean.FALSE;
                var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                var dateTime1 = LocalDateTime.parse(args[0], formatter);
                var dateTime2 = LocalDateTime.parse(args[1], formatter);

                List<OrderQtAgeDTO> qtAgeList = new ArrayList<OrderQtAgeDTO>();

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
