package ru.edu.pr05;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BusinessTripService {
    private final Map<String, BusinessTrip> trips = new LinkedHashMap<>();
    private final Clock clock;

    public BusinessTripService() {
        this(Clock.system(ZoneId.of("Europe/Moscow")));
    }

    public BusinessTripService(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("требуется источник текущей даты");
        }
        this.clock = clock;
    }

    public BusinessTrip create(String id, LocalDate start, LocalDate end) {
        BusinessTrip trip = new BusinessTrip(id, start, end);
        if (trips.containsKey(trip.getId())) {
            throw new IllegalArgumentException("командировка с id " + trip.getId() + " уже существует");
        }
        trips.put(trip.getId(), trip);
        return trip;
    }

    public BusinessTrip createFromText(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("нужны id;start;end");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 3) {
            throw new IllegalArgumentException("нужны ровно 3 поля id;start;end");
        }
        return create(fields[0], parseDate(fields[1], "start (позиция 2)"),
                parseDate(fields[2], "end (позиция 3)"));
    }

    public BusinessTrip getById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id командировки обязателен");
        }
        BusinessTrip trip = trips.get(id.trim());
        if (trip == null) {
            throw new IllegalArgumentException("командировка не найдена: " + id.trim());
        }
        return trip;
    }

    public List<BusinessTrip> getTrips() {
        return List.copyOf(trips.values());
    }

    public List<BusinessTrip> findByStatus(TripStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("статус для поиска обязателен");
        }
        List<BusinessTrip> result = new ArrayList<>();
        for (BusinessTrip trip : trips.values()) {
            if (trip.getStatus() == status) {
                result.add(trip);
            }
        }
        return List.copyOf(result);
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public BusinessTrip execute(String id, String action, String argument) {
        BusinessTrip trip = getById(id);
        applyAction(trip, action, argument);
        return trip;
    }

    public BusinessTrip processCsvLine(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("пустая строка командировки");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 4 && fields.length != 5) {
            throw new IllegalArgumentException("нужны 4 или 5 полей id;start;end;action[;reason]");
        }
        BusinessTrip candidate = new BusinessTrip(fields[0],
                parseDate(fields[1], "start (позиция 2)"),
                parseDate(fields[2], "end (позиция 3)"));
        BusinessTrip existing = trips.get(candidate.getId());
        if (existing != null && (!existing.getStart().equals(candidate.getStart())
                || !existing.getEnd().equals(candidate.getEnd()))) {
            throw new IllegalArgumentException("конфликт дат командировки " + candidate.getId());
        }
        BusinessTrip trip = existing == null ? candidate : existing;
        applyAction(trip, fields[3], fields.length == 5 ? fields[4] : "");
        // Новая заявка появляется в хранилище только после успешной операции.
        if (existing == null) {
            trips.put(trip.getId(), trip);
        }
        return trip;
    }

    private void applyAction(BusinessTrip trip, String action, String argument) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action (позиция 4): операция обязательна");
        }
        String operation = action.trim().toUpperCase(Locale.ROOT);
        if (!"REJECT".equals(operation) && argument != null && !argument.isBlank()) {
            throw new IllegalArgumentException("дополнительный аргумент допускается только для REJECT");
        }
        switch (operation) {
            case "SUBMIT":
                trip.submit();
                break;
            case "APPROVE":
                trip.approve();
                break;
            case "REJECT":
                trip.reject(argument);
                break;
            case "CANCEL":
                trip.cancel();
                break;
            case "COMPLETE":
                trip.complete(today());
                break;
            default:
                throw new IllegalArgumentException("неизвестная операция: " + action);
        }
    }

    private LocalDate parseDate(String raw, String field) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(field + ": ожидается существующая дата YYYY-MM-DD");
        }
    }
}
