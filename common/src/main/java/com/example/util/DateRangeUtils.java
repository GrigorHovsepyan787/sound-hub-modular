package com.example.util;

import java.time.LocalDateTime;

public final class DateRangeUtils {
    private DateRangeUtils() {
    }

    public static DateRange last30Days() {

        LocalDateTime end = LocalDateTime.now();

        LocalDateTime start =
                end.minusDays(30);

        return new DateRange(start, end);
    }
}
