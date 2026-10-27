package ru.edu.pr05;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class SelfCheck {
    private static final LocalDate START = LocalDate.of(2026, 10, 10);
    private static final LocalDate END = LocalDate.of(2026, 10, 12);
    private static final String REASON = "1234567890123456789012345678901234567890";
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        // Таблица ожидаемых переходов: submit, approve, reject, cancel, complete.
        boolean[][] allowed = {
            {true, false, false, true, false},
            {false, true, true, true, false},
            {false, false, false, true, true},
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, false, false, false}
        };
        String[] actions = {"submit", "approve", "reject", "cancel", "complete"};
        TripStatus[] targets = {TripStatus.SUBMITTED, TripStatus.APPROVED, TripStatus.REJECTED,
                TripStatus.CANCELLED, TripStatus.COMPLETED};
        for (TripStatus state : TripStatus.values()) {
            for (int i = 0; i < actions.length; i++) {
                int actionIndex = i;
                check(state + " + " + actions[i], () -> {
                    BusinessTrip trip = inStatus(state);
                    String previousReason = trip.getRejectionReason();
                    if (allowed[state.ordinal()][actionIndex]) {
                        apply(trip, actionIndex);
                        require(trip.getStatus() == targets[actionIndex]);
                        if (actionIndex == 2) {
                            require(REASON.equals(trip.getRejectionReason()));
                        }
                    } else {
                        expect(IllegalStateException.class, () -> apply(trip, actionIndex));
                        require(trip.getStatus() == state);
                        require(Objects.equals(previousReason, trip.getRejectionReason()));
                    }
                });
            }
        }

        check("константа варианта 7 равна 40", () -> require(BusinessTrip.MIN_REJECTION_REASON_LENGTH == 40));
        check("обязательные данные сохраняются", () -> {
            BusinessTrip trip = new BusinessTrip(" T1 ", START, END);
            require(trip.getId().equals("T1") && trip.getStart().equals(START) && trip.getEnd().equals(END));
            require(trip.getStatus() == TripStatus.DRAFT && trip.getRejectionReason() == null);
        });
        check("null вместо id", () -> expect(IllegalArgumentException.class,
                () -> new BusinessTrip(null, START, END)));
        check("пустой id", () -> expect(IllegalArgumentException.class,
                () -> new BusinessTrip("   ", START, END)));
        check("нет даты начала", () -> expect(IllegalArgumentException.class,
                () -> new BusinessTrip("T1", null, END)));
        check("нет даты окончания", () -> expect(IllegalArgumentException.class,
                () -> new BusinessTrip("T1", START, null)));
        check("окончание раньше начала", () -> expect(IllegalArgumentException.class,
                () -> new BusinessTrip("T1", END, START)));
        check("однодневная командировка допустима", () -> {
            BusinessTrip trip = new BusinessTrip("T1", START, START);
            trip.submit();
            require(trip.getStatus() == TripStatus.SUBMITTED);
        });

        check("причина длиной 39 отклоняется без изменения", () -> {
            BusinessTrip trip = inStatus(TripStatus.SUBMITTED);
            expect(IllegalArgumentException.class, () -> trip.reject("x".repeat(39)));
            require(trip.getStatus() == TripStatus.SUBMITTED && trip.getRejectionReason() == null);
        });
        check("причина ровно 40 символов включена", () -> {
            BusinessTrip trip = inStatus(TripStatus.SUBMITTED);
            trip.reject("x".repeat(40));
            require(trip.getStatus() == TripStatus.REJECTED && trip.getRejectionReason().length() == 40);
        });
        check("причина длиной 41 допустима", () -> {
            BusinessTrip trip = inStatus(TripStatus.SUBMITTED);
            trip.reject("x".repeat(41));
            require(trip.getRejectionReason().length() == 41);
        });
        check("null вместо причины", () -> expect(IllegalArgumentException.class,
                () -> inStatus(TripStatus.SUBMITTED).reject(null)));
        check("причина только из пробелов", () -> expect(IllegalArgumentException.class,
                () -> inStatus(TripStatus.SUBMITTED).reject(" ".repeat(50))));
        check("крайние пробелы не дополняют длину 39", () -> expect(IllegalArgumentException.class,
                () -> inStatus(TripStatus.SUBMITTED).reject("  " + "x".repeat(39) + "  ")));
        check("причина сохраняется после trim", () -> {
            BusinessTrip trip = inStatus(TripStatus.SUBMITTED);
            trip.reject("  " + REASON + "  ");
            require(REASON.equals(trip.getRejectionReason()));
        });
        check("причина 10 символов больше не достаточна", () -> expect(IllegalArgumentException.class,
                () -> inStatus(TripStatus.SUBMITTED).reject("x".repeat(10))));
        check("ошибка статуса проверяется раньше причины", () -> expect(IllegalStateException.class,
                () -> inStatus(TripStatus.DRAFT).reject(null)));

        check("завершение за день до окончания запрещено", () -> {
            BusinessTrip trip = inStatus(TripStatus.APPROVED);
            expect(IllegalStateException.class, () -> trip.complete(END.minusDays(1)));
            require(trip.getStatus() == TripStatus.APPROVED);
        });
        check("завершение в день окончания запрещено", () -> {
            BusinessTrip trip = inStatus(TripStatus.APPROVED);
            expect(IllegalStateException.class, () -> trip.complete(END));
            require(trip.getStatus() == TripStatus.APPROVED);
        });
        check("завершение на следующий день разрешено", () -> {
            BusinessTrip trip = inStatus(TripStatus.APPROVED);
            trip.complete(END.plusDays(1));
            require(trip.getStatus() == TripStatus.COMPLETED);
        });
        check("завершение без текущей даты", () -> {
            BusinessTrip trip = inStatus(TripStatus.APPROVED);
            expect(IllegalArgumentException.class, () -> trip.complete(null));
            require(trip.getStatus() == TripStatus.APPROVED);
        });

        check("сервис создаёт и находит DRAFT", () -> {
            BusinessTripService service = service();
            BusinessTrip trip = service.create(" T1 ", START, END);
            require(service.getById(" T1 ") == trip && service.getTrips().size() == 1);
        });
        check("повторный id не перезаписывает заявку", () -> {
            BusinessTripService service = service();
            BusinessTrip original = service.create("T1", START, END);
            expect(IllegalArgumentException.class, () -> service.create(" T1 ", START, END));
            require(service.getById("T1") == original && service.getTrips().size() == 1);
        });
        check("поиск отсутствующего и пустого id", () -> {
            expect(IllegalArgumentException.class, () -> service().getById("UNKNOWN"));
            expect(IllegalArgumentException.class, () -> service().getById(null));
        });
        check("сервис без Clock", () -> expect(IllegalArgumentException.class,
                () -> new BusinessTripService(null)));
        check("поиск по статусу и защита списка", () -> {
            BusinessTripService service = service();
            service.create("T1", START, END);
            service.create("T2", START, END).submit();
            require(service.findByStatus(TripStatus.DRAFT).size() == 1);
            require(service.findByStatus(TripStatus.SUBMITTED).get(0).getId().equals("T2"));
            expect(UnsupportedOperationException.class, () -> service.getTrips().clear());
            expect(UnsupportedOperationException.class, () -> service.findByStatus(TripStatus.DRAFT).clear());
            expect(IllegalArgumentException.class, () -> service.findByStatus(null));
        });
        check("сервис использует московскую дату после полуночи", () -> {
            BusinessTripService service = new BusinessTripService(Clock.fixed(
                    Instant.parse("2026-10-12T21:30:00Z"), ZoneId.of("Europe/Moscow")));
            require(service.today().equals(END.plusDays(1)));
            service.create("T1", START, END);
            service.execute("T1", "submit", "");
            service.execute("T1", "approve", "");
            require(service.execute("T1", "complete", "").getStatus() == TripStatus.COMPLETED);
        });
        check("сервис запрещает complete в день окончания", () -> {
            BusinessTripService service = new BusinessTripService(Clock.fixed(
                    Instant.parse("2026-10-12T20:59:00Z"), ZoneId.of("Europe/Moscow")));
            service.create("T1", START, END).submit();
            service.execute("T1", "approve", "");
            expect(IllegalStateException.class, () -> service.execute("T1", "complete", ""));
            require(service.getById("T1").getStatus() == TripStatus.APPROVED);
        });
        check("CSV продолжает состояние существующей заявки", () -> {
            BusinessTripService service = service();
            service.processCsvLine(" T1 ; 2026-10-10 ; 2026-10-12 ; submit ");
            service.processCsvLine("T1;2026-10-10;2026-10-12;APPROVE");
            service.processCsvLine("T1;2026-10-10;2026-10-12;COMPLETE");
            require(service.getTrips().size() == 1 && service.getById("T1").getStatus() == TripStatus.COMPLETED);
        });
        check("CSV: отклонение с причиной", () -> {
            BusinessTripService service = service();
            service.processCsvLine("T1;2026-10-10;2026-10-12;SUBMIT");
            service.processCsvLine("T1;2026-10-10;2026-10-12;REJECT;" + REASON);
            require(service.getById("T1").getStatus() == TripStatus.REJECTED);
            require(REASON.equals(service.getById("T1").getRejectionReason()));
        });
        check("CSV: ошибка причины не меняет заявку", () -> {
            BusinessTripService service = service();
            service.processCsvLine("T1;2026-10-10;2026-10-12;SUBMIT");
            expect(IllegalArgumentException.class,
                    () -> service.processCsvLine("T1;2026-10-10;2026-10-12;REJECT;short"));
            require(service.getById("T1").getStatus() == TripStatus.SUBMITTED);
            require(service.getById("T1").getRejectionReason() == null);
        });
        check("CSV: конфликт дат не меняет заявку", () -> {
            BusinessTripService service = service();
            service.processCsvLine("T1;2026-10-10;2026-10-12;SUBMIT");
            expect(IllegalArgumentException.class,
                    () -> service.processCsvLine("T1;2026-10-10;2026-10-13;APPROVE"));
            require(service.getById("T1").getStatus() == TripStatus.SUBMITTED);
            require(service.getById("T1").getEnd().equals(END));
        });
        check("CSV: повторный SUBMIT — ошибка перехода", () -> {
            BusinessTripService service = service();
            String line = "T1;2026-10-10;2026-10-12;SUBMIT";
            service.processCsvLine(line);
            expect(IllegalStateException.class, () -> service.processCsvLine(line));
            require(service.getTrips().size() == 1 && service.getById("T1").getStatus() == TripStatus.SUBMITTED);
        });
        check("CSV: неуспешная первая операция не создаёт заявку", () -> {
            BusinessTripService service = service();
            expect(IllegalStateException.class,
                    () -> service.processCsvLine("T1;2026-10-10;2026-10-12;APPROVE"));
            expect(IllegalArgumentException.class,
                    () -> service.processCsvLine("T1;2026-10-10;2026-10-12;UNKNOWN"));
            require(service.getTrips().isEmpty());
        });
        check("CSV: null, пустая строка и неверное число полей", () -> {
            expect(IllegalArgumentException.class, () -> service().processCsvLine(null));
            expect(IllegalArgumentException.class, () -> service().processCsvLine("   "));
            expect(IllegalArgumentException.class, () -> service().processCsvLine("T1;2026-10-10;2026-10-12"));
            expect(IllegalArgumentException.class, () -> service().processCsvLine("T1;a;b;c;d;e"));
        });
        check("CSV: невозможная дата и неверный формат", () -> {
            expect(IllegalArgumentException.class,
                    () -> service().processCsvLine("T1;2026-02-30;2026-03-01;SUBMIT"));
            expect(IllegalArgumentException.class,
                    () -> service().processCsvLine("T1;2026-02-29;2026-03-01;SUBMIT"));
            expect(IllegalArgumentException.class,
                    () -> service().processCsvLine("T1;10.10.2026;2026-10-12;SUBMIT"));
        });
        check("CSV: пустая дата, пустой id и обратный интервал", () -> {
            expect(IllegalArgumentException.class,
                    () -> service().processCsvLine("T1;;2026-10-12;SUBMIT"));
            expect(IllegalArgumentException.class,
                    () -> service().processCsvLine(";2026-10-10;2026-10-12;SUBMIT"));
            expect(IllegalArgumentException.class,
                    () -> service().processCsvLine("T1;2026-10-12;2026-10-10;SUBMIT"));
        });
        check("CSV: пустая операция", () -> expect(IllegalArgumentException.class,
                () -> service().processCsvLine("T1;2026-10-10;2026-10-12;")));
        check("лишний аргумент обычной операции отклоняется", () -> {
            BusinessTripService service = service();
            service.create("T1", START, END);
            expect(IllegalArgumentException.class, () -> service.execute("T1", "SUBMIT", "лишнее"));
            require(service.getById("T1").getStatus() == TripStatus.DRAFT);
        });
        check("високосная дата допустима", () -> {
            BusinessTrip trip = service().processCsvLine("T1;2024-02-29;2024-02-29;SUBMIT");
            require(trip.getStart().equals(LocalDate.of(2024, 2, 29)));
        });
        check("создание из трёх текстовых полей", () -> {
            BusinessTrip trip = service().createFromText("T1;2026-10-10;2026-10-12");
            require(trip.getStatus() == TripStatus.DRAFT);
        });
        check("ошибочная строка создания", () -> {
            expect(IllegalArgumentException.class, () -> service().createFromText(null));
            expect(IllegalArgumentException.class, () -> service().createFromText("T1;2026-10-10"));
        });

        System.out.printf("Результат: PASS — %d, FAIL — %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static BusinessTripService service() {
        return new BusinessTripService(Clock.fixed(Instant.parse("2026-10-27T09:00:00Z"),
                ZoneId.of("Europe/Moscow")));
    }

    private static BusinessTrip inStatus(TripStatus status) {
        BusinessTrip trip = new BusinessTrip("T1", START, END);
        switch (status) {
            case DRAFT:
                break;
            case SUBMITTED:
                trip.submit();
                break;
            case APPROVED:
                trip.submit();
                trip.approve();
                break;
            case REJECTED:
                trip.submit();
                trip.reject(REASON);
                break;
            case CANCELLED:
                trip.cancel();
                break;
            case COMPLETED:
                trip.submit();
                trip.approve();
                trip.complete(END.plusDays(1));
                break;
            default:
                throw new AssertionError(status);
        }
        require(trip.getStatus() == status);
        return trip;
    }

    private static void apply(BusinessTrip trip, int action) {
        switch (action) {
            case 0:
                trip.submit();
                break;
            case 1:
                trip.approve();
                break;
            case 2:
                trip.reject(REASON);
                break;
            case 3:
                trip.cancel();
                break;
            case 4:
                trip.complete(END.plusDays(1));
                break;
            default:
                throw new AssertionError(action);
        }
    }

    private static void check(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (RuntimeException | AssertionError e) {
            failed++;
            System.out.println("FAIL: " + name + " — " + e.getMessage());
        }
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            if (type.isInstance(e)) {
                return;
            }
            throw new AssertionError("ожидалось " + type.getSimpleName() + ", получено " + e);
        }
        throw new AssertionError("ожидалось " + type.getSimpleName());
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new AssertionError("неверное состояние командировки");
        }
    }
}
