package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.reliaquest.api.controller.request.EmployeeCreationInput;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Employee {
    @JsonAlias("id")
    private UUID id;

    @JsonAlias("employee_name")
    private String name;

    @JsonAlias("employee_salary")
    private Integer salary;

    @JsonAlias("employee_age")
    private Integer age;

    @JsonAlias("employee_title")
    private String title;

    @JsonAlias("employee_email")
    private String email;

    public static Employee newEmployee(EmployeeCreationInput request) {
        return new Employee(
                UUID.randomUUID(), request.name(), request.salary(), request.age(), request.title(), request.email());
    }
}
