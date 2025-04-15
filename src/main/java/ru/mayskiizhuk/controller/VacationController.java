package ru.mayskiizhuk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mayskiizhuk.config.SalaryConstants;
import ru.mayskiizhuk.service.VacationService;
import ru.mayskiizhuk.validate.InputValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/calculacte")
public class VacationController {

    private final VacationService vacationService;
    private final InputValidator validator;
    private final DateTimeFormatter dateFormatter;

    public VacationController(VacationService vacationService, InputValidator validator) {
        this.vacationService = vacationService;
        this.validator = validator;
        this.dateFormatter = DateTimeFormatter.ofPattern(SalaryConstants.DATE_FORMAT);
    }

    @GetMapping
    public ResponseEntity<Object> calculateVacationPay(
            @RequestParam(value = "averageSalary", required = false) String averageSalaryStr,
            @RequestParam(value = "numberOfDays", required = false) String numberOfDaysStr,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr) {
        // 1. Валидация и парсинг средней зарплаты (обязательный параметр)
        if (averageSalaryStr == null || averageSalaryStr.isBlank()) {
            return buildBadRequestResponse("Не указан обязательный параметр averageSalary.");
        }
        long averageSalary;
        try {
            averageSalary = Long.parseLong(averageSalaryStr);
            if (!validator.isValidatedSalary(averageSalary)) {
                return buildBadRequestResponse("Средняя зарплата (averageSalary) должна быть в диапазоне от %d до %d копеек." +
                        "Вы ввели значение %d копеек.", SalaryConstants.MINIMUM_SALARY, SalaryConstants.MAXIMUM_SALARY, averageSalary);
            }
        } catch (NumberFormatException e) {
            return buildBadRequestResponse("Параметр averageSalary должен быть целым числом (количество копеек).");
        }

        // 2. Определяем режим работы: по количеству дней или по датам
        boolean useDays = numberOfDaysStr != null && !numberOfDaysStr.isBlank();
        boolean useTwoDates = (startDateStr != null && !startDateStr.isBlank()) && (endDateStr != null && !endDateStr.isBlank());

        // Проверяем конфликтующие или недостающие параметры
        if (useDays && useTwoDates) {
            return buildBadRequestResponse("Укажите ЛИБО numberOfDays, ЛИБО оба параметра startDate и endDate, но не все вместе.");
        }
        if (!useDays && !useTwoDates) {
            return buildBadRequestResponse("Необходимо указать ЛИБО numberOfDays, ЛИБО оба параметра startDate и endDate.");
        }

        // 3. Обработка и валидация в зависимости от режима
        if (useDays) {
            // Режим: количество дней
            int days;
            try {
                days = Integer.parseInt(numberOfDaysStr);
                if (!validator.isValidatedDays(days)) {
                    return buildBadRequestResponse("Количество дней отпуска (numberOfDays) должно быть в диапазоне от %d до %d." +
                                    "Вы ввели значение %d дней.",
                            SalaryConstants.MINIMUM_DAYS, SalaryConstants.MAXIMUM_DAYS, days);
                }
            } catch (NumberFormatException e) {
                return buildBadRequestResponse("Параметр numberOfDays должен быть целым числом.");
            }
            // Выполняем расчет
            long result = vacationService.calculatePay(averageSalary, days);
            return ResponseEntity.ok(result);

        } else {
            // Режим: даты начала и конца отпуска
            LocalDate startLocalDate;
            LocalDate endLocalDate;
            try {
                startLocalDate = LocalDate.parse(startDateStr, dateFormatter);
            } catch (DateTimeParseException e) {
                return buildBadRequestResponse("Неверный формат даты начала отпуска (startDate). Ожидается формат %s.",
                        SalaryConstants.DATE_FORMAT.toLowerCase());
            }
            try {
                endLocalDate = LocalDate.parse(endDateStr, dateFormatter);
            } catch (DateTimeParseException e) {
                return buildBadRequestResponse("Неверный формат даты окончания отпуска (endDate). Ожидается формат %s.",
                        SalaryConstants.DATE_FORMAT.toLowerCase());
            }

            // Валидируем период дат
            if (!validator.isValidatedDaysStartAndEnd(startLocalDate, endLocalDate)) {
                return buildBadRequestResponse("Некорректный период отпуска: дата окончания должна быть не раньше даты начала, " +
                                "а продолжительность должна быть в диапазоне от %d до %d дней.",
                        SalaryConstants.MINIMUM_DAYS, SalaryConstants.MAXIMUM_DAYS);
            }

            // Выполняем расчет
            long result = vacationService.calculatePaySpecificDate(averageSalary, startLocalDate, endLocalDate);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Вспомогательный метод для создания единообразных BadRequest ответов.
     *
     * @param format Строка формата сообщения
     * @param args   Аргументы для строки формата
     * @return ResponseEntity с кодом 400 и телом сообщения
     */
    private ResponseEntity<Object> buildBadRequestResponse(String format, Object... args) {
        String message = String.format(format, args);
        return ResponseEntity.badRequest().body(message);
    }
}
//http://localhost:8080/calculacte?averageSalary=100&numberOfDays=14
