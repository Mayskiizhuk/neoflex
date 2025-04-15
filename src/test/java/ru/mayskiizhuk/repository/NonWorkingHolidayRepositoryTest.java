package ru.mayskiizhuk.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование NonWorkingHolidayRepository")
class NonWorkingHolidayRepositoryTest {

    // --- Источники данных для параметризованных тестов ---

    // Предоставляет поток дат, которые ДОЛЖНЫ быть праздниками
    static Stream<LocalDate> provideHolidayDates() {
        return Stream.of(
                // Январские каникулы (разные года)
                LocalDate.of(2023, 01, 1),
                LocalDate.of(2024, 01, 5),
                LocalDate.of(2025, 01, 8),
                // 23 Февраля
                LocalDate.of(2024, 02,23),
                // 8 Марта
                LocalDate.of(2023, 03, 8),
                // 1 Мая
                LocalDate.of(2024, 05, 1),
                // 9 Мая
                LocalDate.of(2025, 05, 9),
                // 12 Июня
                LocalDate.of(2023, 06, 12),
                // 4 Ноября
                LocalDate.of(2024, 11, 4)
        );
    }

    // Предоставляет поток дат, которые НЕ должны быть праздниками
    static Stream<LocalDate> provideNonHolidayDates() {
        return Stream.of(
                // Обычные рабочие/выходные дни
                LocalDate.of(2023, 01, 9), // Сразу после каникул
                LocalDate.of(2024, 02, 22),// День перед праздником
                LocalDate.of(2025, 02, 24),// День после праздника
                LocalDate.of(2023, 04, 15),// Середина месяца без праздников
                LocalDate.of(2024, 05, 2), // Между майскими
                LocalDate.of(2025, 05, 10),// После 9 мая
                LocalDate.of(2026, 12, 31) // Канун НГ (часто сокращенный, но не гос. праздник)
        );
    }

    // --- Тесты для метода isNonWorkingHoliday ---
    @ParameterizedTest(name = "Дата {0} должна быть праздником")
    @MethodSource("provideHolidayDates")
    @DisplayName("Проверка известных праздничных дат")
    void isNonWorkingHoliday_whenDateIsHoliday_shouldReturnTrue(LocalDate holidayDate) {
        assertTrue(NonWorkingHolidayRepository.isNonWorkingHoliday(holidayDate),
                "Дата " + holidayDate + " должна определяться как праздник");
    }

    @ParameterizedTest(name = "Дата {0} не должна быть праздником")
    @MethodSource("provideNonHolidayDates")
    @DisplayName("Проверка дат, не являющихся праздниками")
    void isNonWorkingHoliday_whenDateIsNotHoliday_shouldReturnFalse(LocalDate nonHolidayDate) {
        assertFalse(NonWorkingHolidayRepository.isNonWorkingHoliday(nonHolidayDate),
                "Дата " + nonHolidayDate + " не должна определяться как праздник");
    }
}