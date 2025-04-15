package ru.mayskiizhuk.service;

import org.springframework.stereotype.Service;
import ru.mayskiizhuk.repository.NonWorkingHolidayRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class VacationService {
    // Среднее количество календарных дней в месяце для расчета (по ТК РФ)
    private static final double AVERAGE_DAYS_IN_MONTH = 29.3;

    /**
     * Рассчитывает сумму отпускных по количеству дней.
     *
     * @param averageSalaryInKopecks Средняя зарплата в копейках
     * @param numberOfVacationDays   Количество дней отпуска
     * @return Сумма отпускных в копейках, округленная вверх
     */
    public long calculatePay(long averageSalaryInKopecks, int numberOfVacationDays) {
        // Формула: СреднедневнойЗаработок * КоличествоДней
        // СреднедневнойЗаработок = averageSalaryInKopecks / AVERAGE_DAYS_IN_MONTH
        double averageDailySalary = (double) averageSalaryInKopecks / AVERAGE_DAYS_IN_MONTH;
        double result = averageDailySalary * numberOfVacationDays;
        return (long) Math.ceil(result);
    }

    /**
     * Рассчитывает сумму отпускных по датам начала и конца.
     *
     * @param averageSalaryInKopecks Средняя зарплата в копейках
     * @param startDate              Дата начала отпуска (включительно)
     * @param endDate                Дата окончания отпуска (включительно)
     * @return Сумма отпускных в копейках, округленная вверх
     */
    public long calculatePaySpecificDate(long averageSalaryInKopecks, LocalDate startDate, LocalDate endDate) {
        int numberOfNonWorkingHolidays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (NonWorkingHolidayRepository.isNonWorkingHoliday(date)) {
                numberOfNonWorkingHolidays++;
            }
        }

        // Общее количество календарных дней в периоде
        long totalDaysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Количество оплачиваемых дней (календарные минус праздники)
        long paidDays = totalDaysInPeriod - numberOfNonWorkingHolidays;

        // Если количество оплачиваемых дней равно нулю,
        if (paidDays == 0) {
            return 0L; // Нечего оплачивать
        }

        // Формула: СреднедневнойЗаработок * КоличествоОплачиваемыхДней
        double averageDailySalary = (double) averageSalaryInKopecks / AVERAGE_DAYS_IN_MONTH;
        double result = averageDailySalary * paidDays;

        return (long) Math.ceil(result);
    }
}