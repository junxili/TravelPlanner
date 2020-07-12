package com.ll.travelplanner.utils;

import lombok.NonNull;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class CommonUtils {
    private CommonUtils() {
    }

    public static <S, T> T getExtrema(@NonNull final List<S> list,
                                      @NonNull final T defaultValue,
                                      @NonNull final Function<S, T> extractionFunction,
                                      @NonNull final BinaryOperator<T> combiner) {
        return list.stream()
                .reduce(defaultValue,
                        (aggregator, element) -> combiner.apply(aggregator, extractionFunction.apply(element)),
                        combiner);
    }
}
