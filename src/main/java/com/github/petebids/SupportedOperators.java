package com.github.petebids;




enum SupportedOperators {
    AND("AND"),
    IN("IN"),
    OR("OR"),
    NOT("NOT"),
    EQ("EQ"),
    NE("NE"),
    LE("LE"),
    LT("LT"),
    GT("GT"),
    GTE("GTE")
    ;

    public String getValue() {
        return value;
    }

    private final String value;
    SupportedOperators(String value) {
        this.value = value;
    }

}
