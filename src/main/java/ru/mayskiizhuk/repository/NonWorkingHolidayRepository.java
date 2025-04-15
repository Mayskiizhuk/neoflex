package ru.mayskiizhuk.repository;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

public class NonWorkingHolidayRepository {
    private static final Set<MonthDay> NON_WORKING_HOLIDAYS = Set.of(MonthDay.of(1, 1),
            MonthDay.of(1, 2), MonthDay.of(1, 3), MonthDay.of(1, 4),
            MonthDay.of(1, 5), MonthDay.of(1, 6), MonthDay.of(1, 7),
            MonthDay.of(1, 8), MonthDay.of(2, 23), MonthDay.of(3, 8),
            MonthDay.of(5, 1), MonthDay.of(5, 9), MonthDay.of(6, 12),
            MonthDay.of(11, 4));

    private NonWorkingHolidayRepository() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isNonWorkingHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        MonthDay md = MonthDay.from(date);
        return NON_WORKING_HOLIDAYS.contains(md);
    }
}