package ru.mayskiizhuk.validate;

import org.springframework.stereotype.Component;
import ru.mayskiizhuk.config.SalaryConstants;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class InputValidator {

    /**
     * Проверяет, находится ли зарплата в допустимом диапазоне.
     * @param averageSalaryInKopecks Зарплата в копейках
     * @return true, если зарплата валидна, иначе false
     */
    public boolean isValidatedSalary(long averageSalaryInKopecks) {
        return averageSalaryInKopecks >= SalaryConstants.MINIMUM_SALARY && averageSalaryInKopecks <= SalaryConstants.MAXIMUM_SALARY;
    }

    /**
     * Проверяет, находится ли количество дней в допустимом диапазоне.
     * @param numberOfVacationDays Количество дней
     * @return true, если количество дней валидно, иначе false
     */
    public boolean isValidatedDays(int numberOfVacationDays) {
        return numberOfVacationDays >= SalaryConstants.MINIMUM_DAYS && numberOfVacationDays <= SalaryConstants.MAXIMUM_DAYS;
    }

    /**
     * Проверяет, является ли период дат корректным (окончание не раньше начала)
     * и находится ли его продолжительность в допустимом диапазоне.
     * @param startDate Дата начала (включительно)
     * @param endDate Дата окончания (включительно)
     * @return true, если период валиден, иначе false
     */
    public boolean isValidatedDaysStartAndEnd(LocalDate startDate, LocalDate endDate) {
        // Проверяем порядок дат
        if (startDate.isAfter(endDate)) {
            return false;
        }
        // Вычисляем корректную продолжительность периода (включая обе даты)
        int daysInPeriod = (int)ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Проверяем корректность продолжительности периода
        return isValidatedDays(daysInPeriod);
    }
}