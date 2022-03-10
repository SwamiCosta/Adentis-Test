package logical;

import java.util.List;

public class IntervalLogical {

    public static Boolean validateIntervals(List<String> intervals){
        for (String interval: intervals) {

            Character firstCharacter = interval.charAt(0);

            String firstNumberStr = "";

            if(firstCharacter == '>' || firstCharacter == '<'){
                firstNumberStr = interval.substring(1);

                try{
                    Integer.valueOf(firstNumberStr);
                } catch (Exception e){
                    return Boolean.FALSE;
                }
            } else {
                if(interval.contains("-")){
                    int hyphenIndex = interval.indexOf('-');

                    firstNumberStr = interval.substring(0,hyphenIndex);
                    String secondNumberStr = interval.substring(hyphenIndex+1);

                    var numericValue1 = 0;
                    var numericValue2 = 0;

                    try{
                        numericValue1 = Integer.parseInt(firstNumberStr);
                        numericValue2 = Integer.parseInt(secondNumberStr);
                    } catch (Exception e){
                        return Boolean.FALSE;
                    }

                    if(numericValue1 > numericValue2){
                        return Boolean.FALSE;
                    }
                } else {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

}
