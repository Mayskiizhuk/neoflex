package ru.mayskiizhuk.config;

public class SalaryConstants {
    public static final long MINIMUM_SALARY = 100; // минимальная зарплата в копейках
    public static final long MAXIMUM_SALARY = 100000000000000L; // максимальная зарплата в копейках
    public static final int MINIMUM_DAYS = 1; // минимальное количество дней отпуска
    public static final int MAXIMUM_DAYS = 366; // максимальное количество дней отпуска
    public static final String DATE_FORMAT = "dd-MM-yy"; // формат даты

    private SalaryConstants() {
        throw new IllegalStateException("Utility class");
    }
}