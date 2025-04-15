package ru.mayskiizhuk.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование VacationService")
class VacationServiceTest {

    // Константа для удобства, соответствует значению в VacationService
    private static final double AVERAGE_DAYS_IN_MONTH = 29.3;

    private VacationService vacationService;

    @BeforeEach
    void setUp() {
        vacationService = new VacationService();
    }

    // --- Тесты для метода calculatePay(long averageSalaryInKopecks, int numberOfVacationDays) ---
    @Nested
    @DisplayName("Метод calculatePay (по количеству дней)")
    class CalculatePayTests {

        @ParameterizedTest(name = "Зарплата {0} коп., Дней {1} -> Ожидаемый результат {2} коп.")
        @CsvSource({
                // Простые случаи
                "100000, 14, 47782",   // 1000 руб/мес, 14 дней -> ceil((100000 / 29.3) * 14) = ceil(47781.57) = 47782
                "293000, 10, 100000",  // 2930 руб/мес, 10 дней -> ceil((293000 / 29.3) * 10) = ceil(100000) = 100000
                "58600,  5,  10000",   // 586 руб/мес, 5 дней -> ceil((58600 / 29.3) * 5) = ceil(10000) = 10000
                // Случай с округлением вверх
                "150000, 7, 35837"     // 1500 руб/мес, 7 дней -> ceil((150000 / 29.3) * 7) = ceil(35836.17) = 35837
        })
        @DisplayName("Расчет для различных зарплат и дней")
        void calculatePay_shouldCalculateCorrectly(long salary, int days, long expectedPay) {
            long actualPay = vacationService.calculatePay(salary, days);
            assertEquals(expectedPay, actualPay, "Расчет для зарплаты " + salary + " и " + days + " дней неверен");
        }
    }

    // --- Тесты для метода calculatePaySpecificDate(long averageSalaryInKopecks, LocalDate startDate, LocalDate endDate) ---
    @Nested
    @DisplayName("Метод calculatePaySpecificDate (по датам)")
    class CalculatePaySpecificDateTests {

        // Примерная зарплата для тестов
        private final long SALARY = 293000; // 10000 коп. в день (примерно)

        @Test
        @DisplayName("Период без праздников")
        void calculatePaySpecificDate_whenNoHolidays_shouldCalculateCorrectly() {
            LocalDate startDate = LocalDate.of(2025, 04, 7);
            LocalDate endDate = LocalDate.of(2025, 04, 20);
            // В апреле нет официальных праздников
            int totalDays = 14;
            int holidays = 0;
            int paidDays = totalDays - holidays; // 14
            long expectedPay = (long) Math.ceil(((double) SALARY / AVERAGE_DAYS_IN_MONTH) * paidDays); // ceil(10000 * 14) = 140000

            long actualPay = vacationService.calculatePaySpecificDate(SALARY, startDate, endDate);
            assertEquals(expectedPay, actualPay);
        }

        @Test
        @DisplayName("Период, включающий 1 мая")
        void calculatePaySpecificDate_whenContainsMay1st_shouldExcludeHoliday() {
            LocalDate startDate = LocalDate.of(2025, 04, 28);
            LocalDate endDate = LocalDate.of(2025, 05, 4);
            // Период: 28.04, 29.04, 30.04, 01.05 (Праздник), 02.05, 03.05, 04.05
            int totalDays = 7;
            int holidays = 1; // 1 мая
            int paidDays = totalDays - holidays; // 6
            long expectedPay = (long) Math.ceil(((double) SALARY / AVERAGE_DAYS_IN_MONTH) * paidDays); // ceil(10000 * 6) = 60000

            long actualPay = vacationService.calculatePaySpecificDate(SALARY, startDate, endDate);
            assertEquals(expectedPay, actualPay);
        }

        @Test
        @DisplayName("Период, включающий 1 и 9 мая")
        void calculatePaySpecificDate_whenContainsMay1stAnd9th_shouldExcludeHolidays() {
            LocalDate startDate = LocalDate.of(2025, 04, 28);
            LocalDate endDate = LocalDate.of(2025, 05, 11);
            // Период: Период: 28.04, 29.04, 30.04, 01.05 (Праздник), 02.05, 03.05, 04.05, 05.05, 06.05, 07.05, 08.05, 09.05 (Праздник), 10.05, 11.05
            int totalDays = 14;
            int holidays = 2; // 1 и 9 мая
            int paidDays = totalDays - holidays; // 12
            long expectedPay = (long) Math.ceil(((double) SALARY / AVERAGE_DAYS_IN_MONTH) * paidDays); // ceil(10000 * 12) = 120000

            long actualPay = vacationService.calculatePaySpecificDate(SALARY, startDate, endDate);
            assertEquals(expectedPay, actualPay);
        }

        @Test
        @DisplayName("Период, состоящий только из праздников")
        void calculatePaySpecificDate_whenOnlyHolidays_shouldReturnZero() {
            LocalDate startDate = LocalDate.of(2025, 01, 1);
            LocalDate endDate = LocalDate.of(2025, 01, 8);
            // Все дни с 1 по 8 января являются праздниками по NonWorkingHolidayRepository

            long actualPay = vacationService.calculatePaySpecificDate(SALARY, startDate, endDate);
            assertEquals(0L, actualPay, "Если все дни в периоде - праздники, результат должен быть 0");
        }

        @Test
        @DisplayName("Период в один день (не праздник)")
        void calculatePaySpecificDate_whenSingleNonHolidayDay_shouldCalculateForOneDay() {
            LocalDate date = LocalDate.of(2025, 04, 1);
            int totalDays = 1;
            int holidays = 0;
            int paidDays = totalDays - holidays;
            long expectedPay = (long) Math.ceil(((double) SALARY / AVERAGE_DAYS_IN_MONTH) * paidDays); // ceil(10000 * 1) = 10000

            long actualPay = vacationService.calculatePaySpecificDate(SALARY, date, date);
            assertEquals(expectedPay, actualPay);
        }

        @Test
        @DisplayName("Период в один день (праздник)")
        void calculatePaySpecificDate_whenSingleHolidayDay_shouldReturnZero() {
            LocalDate date = LocalDate.of(2025, 05, 1); // Праздник

            long actualPay = vacationService.calculatePaySpecificDate(SALARY, date, date);
            assertEquals(0L, actualPay, "Если единственный день - праздник, результат должен быть 0");
        }
    }
}