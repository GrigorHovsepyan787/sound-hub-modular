package com.example.util;

import java.time.LocalDateTime;

public record DateRange(LocalDateTime currentStart,
                        LocalDateTime currentEnd,
                        LocalDateTime previousStart,
                        LocalDateTime previousEnd) {
}
