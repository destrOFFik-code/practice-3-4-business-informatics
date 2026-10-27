package ru.edu.pr04;

public record Employee(String id, String fullName) {
    public Employee {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("требуется идентификатор сотрудника");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("требуется ФИО сотрудника");
        }
        id = id.trim();
        fullName = fullName.trim();
    }
}
