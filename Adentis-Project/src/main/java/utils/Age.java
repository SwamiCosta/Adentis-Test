package utils;

public enum Age {

    TWELVE_PLUS(">12"),
    SEVEN_TWELVE("7-12"),
    FOUR_SIX("4-6"),
    ONE_THREE("1-3");

    private String text;

    Age(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
