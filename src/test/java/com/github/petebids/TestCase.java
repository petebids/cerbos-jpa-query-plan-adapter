package com.github.petebids;

import java.util.List;
import java.util.function.Function;

public record TestCase(
        String action, String principal, Integer expectedResultCount, Function<List<Resource>, Boolean> validator
){}
