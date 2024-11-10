package com.github.petebids;

import lombok.Getter;

@Getter
enum SupportedOperators {
    AND("AND"),
    OR("OR"),
    NOT("NOT"),
    EQ("EQ"),
    NE("NE"),
    LE("LE"),
    LT("LT"),
    GT("GT"),
    GTE("GTE")
    ;

    private final String value;
    SupportedOperators(String value) {
        this.value = value;
    }

}
