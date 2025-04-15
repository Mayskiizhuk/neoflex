package ru.mayskiizhuk.validate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mayskiizhuk.config.SalaryConstants;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    private InputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }

    @Nested
    @DisplayName("Тесты для isValidatedSalary")
    class IsValidatedSalaryTests {

        @Test
        @DisplayName("Зарплата в допустимом диапазоне")
        void isValidatedSalary_whenSalaryIsWithinBounds_shouldReturnTrue() {
            long validSalary = (SalaryConstants.MINIMUM_SALARY + SalaryConstants.MAXIMUM_SALARY) / 2;
            assertTrue(validator.isValidatedSalary(validSalary), "Зарплата в середине диапазона должна быть валидной");
        }

        @Test
        @DisplayName("Зарплата равна минимальной границе")
        void isValidatedSalary_whenSalaryIsMinimum_shouldReturnTrue() {
            assertTrue(validator.isValidatedSalary(SalaryConstants.MINIMUM_SALARY), "Минимальная зарплата должна быть валидной");
        }

        @Test
        @DisplayName("Зарплата равна максимальной границе")
        void isValidatedSalary_whenSalaryIsMaximum_shouldReturnTrue() {
            assertTrue(validator.isValidatedSalary(SalaryConstants.MAXIMUM_SALARY), "Максимальная зарплата должна быть валидной");
        }

        @ParameterizedTest
        @ValueSource(longs = {SalaryConstants.MINIMUM_SALARY - 1, -100}) // Значения меньше минимального
        @DisplayName("Зарплата меньше минимальной")
        void isValidatedSalary_whenSalaryIsBelowMinimum_shouldReturnFalse(long salary) {
            assertFalse(validator.isValidatedSalary(salary), "Зарплата " + salary + " должна быть невалидной (меньше минимума)");
        }

        @Test
        @DisplayName("Зарплата больше максимальной")
        void isValidatedSalary_whenSalaryIsAboveMaximum_shouldReturnFalse() {
            long salary = SalaryConstants.MAXIMUM_SALARY + 1;
            assertFalse(validator.isValidatedSalary(salary), "Зарплата больше максимальной должна быть невалидной");
        }
    }

    @Nested
    @DisplayName("Тесты для isValidatedDays")
    class IsValidatedDaysTests {

        @ParameterizedTest
        @ValueSource(ints = {SalaryConstants.MINIMUM_DAYS, SalaryConstants.MAXIMUM_DAYS, 14, 28}) // Валидные значения
        @DisplayName("Количество дней в допустимом диапазоне")
        void isValidatedDays_whenDaysAreValid_shouldReturnTrue(int days) {
            assertTrue(validator.isValidatedDays(days), "Количество дней " + days + " должно быть валидным");
        }

        @ParameterizedTest
        @ValueSource(ints = {SalaryConstants.MINIMUM_DAYS - 1, 0, SalaryConstants.MAXIMUM_DAYS + 1, 500}) // Невалидные значения
        @DisplayName("Количество дней вне допустимого диапазона")
        void isValidatedDays_whenDaysAreInvalid_shouldReturnFalse(int days) {
            assertFalse(validator.isValidatedDays(days), "Количество дней " + days + " должно быть невалидным");
        }
    }

    @Nested
    @DisplayName("Тесты для isValidatedDaysStartAndEnd")
    class IsValidatedDaysStartAndEndTests {

        private final LocalDate BASE_DATE = LocalDate.of(2025, 7, 1);

        @Test
        @DisplayName("Корректный период в середине диапазона")
        void isValidatedDaysStartAndEnd_whenPeriodIsValid_shouldReturnTrue() {
            LocalDate startDate = BASE_DATE;
            LocalDate endDate = BASE_DATE.plusDays(14); //
            assertTrue(validator.isValidatedDaysStartAndEnd(startDate, endDate), "Дата окончания отпуска позже " +
                    "даты начала отпуска и продолжительность отпуска в допустимых пределах");
        }

        @Test
        @DisplayName("Дата окончания раньше даты начала")
        void isValidatedDaysStartAndEnd_whenEndDateIsBeforeStartDate_shouldReturnFalse() {
            LocalDate startDate = BASE_DATE;
            LocalDate endDate = BASE_DATE.minusDays(1);
            assertFalse(validator.isValidatedDaysStartAndEnd(startDate, endDate), "Дата окончания не может быть раньше даты начала");
        }

        @Test
        @DisplayName("Дата начала равна дате окончания")
        void isValidatedDaysStartAndEnd_whenStartDateEqualsEndDate_shouldReturnTrueIfMinDaysIsOne() {
            assertTrue(validator.isValidatedDaysStartAndEnd(BASE_DATE, BASE_DATE),
                    "Период в один день должен быть валидным, если MINIMUM_DAYS <= 1");
        }
    }
}