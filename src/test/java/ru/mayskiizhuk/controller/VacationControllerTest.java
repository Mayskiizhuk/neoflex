package ru.mayskiizhuk.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mayskiizhuk.config.SalaryConstants;
import ru.mayskiizhuk.service.VacationService;
import ru.mayskiizhuk.validate.InputValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc // Автоматически настраивает MockMvc
@DisplayName("Интеграционное тестирование VacationController")
class VacationControllerIntegrationTest { // Изменили имя класса для ясности

    @Autowired
    private MockMvc mockMvc; // MockMvc все еще используется для отправки запросов

    // Константы для тестов
    private final String BASE_URL = "/calculacte";
    private final String VALID_SALARY_STR = "293000"; // 2930 руб = 100 руб/день по формуле
    private final long VALID_SALARY_LONG = 293000L;
    private final String VALID_DAYS_STR = "7";
    private final int VALID_DAYS_INT = 7;

    // Даты для теста с датами (период без праздников)
    private final String NO_HOLIDAY_START_STR = "07-04-25"; // Апрель 2025
    private final String NO_HOLIDAY_END_STR = "13-04-25";   // 7 дней
    // Даты для теста с датами (период с праздниками 1 и 9 мая)
    private final String MAY_HOLIDAY_START_STR = "28-04-25";
    private final String MAY_HOLIDAY_END_STR = "11-05-25"; // 14 дней всего, 2 праздника -> 12 оплачиваемых

    // Ожидаемые результаты (рассчитанные по реальной логике)
    private final long EXPECTED_PAY_FOR_7_DAYS = (long) Math.ceil(((double) VALID_SALARY_LONG / 29.3) * 7);
    private final long EXPECTED_PAY_FOR_14_NO_HOLIDAYS = (long) Math.ceil(((double) VALID_SALARY_LONG / 29.3) * 14);
    private final long EXPECTED_PAY_FOR_14_MAY_HOLIDAYS = (long) Math.ceil(((double) VALID_SALARY_LONG / 29.3) * 12);

    // --- Успешные сценарии ---

    @Test
    @DisplayName("Успешный расчет по количеству дней")
    void calculateVacationPay_whenValidSalaryAndDays_shouldReturnOkWithRealCalculation() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("numberOfDays", VALID_DAYS_STR))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(EXPECTED_PAY_FOR_7_DAYS))); // Проверяем реальный результат
    }

    @Test
    @DisplayName("Успешный расчет по датам без праздников")
    void calculateVacationPay_whenValidSalaryAndDatesNoHolidays_shouldReturnOkWithRealCalculation() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("startDate", NO_HOLIDAY_START_STR)
                        .param("endDate", NO_HOLIDAY_END_STR))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(EXPECTED_PAY_FOR_7_DAYS))); // Проверяем реальный результат
    }

    @Test
    @DisplayName("Успешный расчет по датам с праздниками")
    void calculateVacationPay_whenValidSalaryAndDatesWithHolidays_shouldReturnOkWithRealCalculation() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("startDate", MAY_HOLIDAY_START_STR)
                        .param("endDate", MAY_HOLIDAY_END_STR))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(EXPECTED_PAY_FOR_14_MAY_HOLIDAYS))); // Проверяем реальный результат
    }

    // --- Сценарии с ошибками (Bad Request - 400) ---

    @Test
    @DisplayName("Ошибка: Не указана средняя зарплата")
    void calculateVacationPay_whenAverageSalaryMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("numberOfDays", VALID_DAYS_STR))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Не указан обязательный параметр averageSalary")));
    }

    @Test
    @DisplayName("Ошибка: Средняя зарплата - не число")
    void calculateVacationPay_whenAverageSalaryIsNotNumber_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", "не_число")
                        .param("numberOfDays", VALID_DAYS_STR))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Параметр averageSalary должен быть целым числом")));
    }

    @Test
    @DisplayName("Ошибка: Средняя зарплата вне диапазона")
    void calculateVacationPay_whenAverageSalaryOutOfRange_shouldReturnBadRequest() throws Exception {
        long invalidSalary = SalaryConstants.MAXIMUM_SALARY + 1;

        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", String.valueOf(invalidSalary))
                        .param("numberOfDays", VALID_DAYS_STR))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Средняя зарплата (averageSalary) должна быть в диапазоне")));
    }

    @Test
    @DisplayName("Ошибка: Не указаны ни дни, ни даты")
    void calculateVacationPay_whenDaysAndDatesMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Необходимо указать ЛИБО numberOfDays, ЛИБО оба параметра startDate и endDate")));
    }

    @Test
    @DisplayName("Ошибка: Указана только одна дата")
    void calculateVacationPay_whenOneDatesMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("startDate", NO_HOLIDAY_START_STR))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Необходимо указать ЛИБО numberOfDays, ЛИБО оба параметра startDate и endDate")));
    }

    @Test
    @DisplayName("Ошибка: Указаны и дни, и даты одновременно")
    void calculateVacationPay_whenBothDaysAndDatesProvided_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("numberOfDays", VALID_DAYS_STR)
                        .param("startDate", NO_HOLIDAY_START_STR)
                        .param("endDate", NO_HOLIDAY_END_STR))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Укажите ЛИБО numberOfDays, ЛИБО оба параметра startDate и endDate, но не все вместе")));
    }

    @Test
    @DisplayName("Ошибка: Количество дней - не число")
    void calculateVacationPay_whenNumberOfDaysIsNotNumber_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("numberOfDays", "десять"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Параметр numberOfDays должен быть целым числом")));
    }

    @Test
    @DisplayName("Ошибка: Количество дней вне диапазона")
    void calculateVacationPay_whenNumberOfDaysOutOfRange_shouldReturnBadRequest() throws Exception {
        int invalidDays = SalaryConstants.MAXIMUM_DAYS + 1;

        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("numberOfDays", String.valueOf(invalidDays)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Количество дней отпуска (numberOfDays) должно быть в диапазоне")));
    }

    @Test
    @DisplayName("Ошибка: Неверный формат startDate")
    void calculateVacationPay_whenInvalidStartDateFotmat_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("startDate", "01.04.2024") // Неверный формат
                        .param("endDate", NO_HOLIDAY_END_STR))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Неверный формат даты начала отпуска (startDate)")));
    }

    @Test
    @DisplayName("Ошибка: Неверный формат endDate")
    void calculateVacationPay_whenInvalidEndDateFotmat_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("startDate", NO_HOLIDAY_START_STR)
                        .param("endDate", "2024/04/14")) // Неверный формат
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Неверный формат даты окончания отпуска (endDate)")));
    }

    @Test
    @DisplayName("Ошибка: Некорректный период дат")
    void calculateVacationPay_whenDatePeriodIsInvalid_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("averageSalary", VALID_SALARY_STR)
                        .param("startDate", NO_HOLIDAY_END_STR) // end дата в качестве start
                        .param("endDate", NO_HOLIDAY_START_STR)) // start дата в качестве end
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Некорректный период отпуска")));
    }
}