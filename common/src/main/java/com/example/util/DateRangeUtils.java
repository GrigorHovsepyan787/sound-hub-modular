package com.example.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DateRangeUtils {
    private DateRangeUtils() {
    }

    public static DateRange monthlyRange(Clock clock) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime currentStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime currentEnd =
                currentStart.plusMonths(1);
        LocalDateTime previousStart =
                currentStart.minusMonths(1);

        return new DateRange(
                currentStart,
                currentEnd,
                previousStart,
                currentStart
        );
    }

}
