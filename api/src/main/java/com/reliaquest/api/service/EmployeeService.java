package com.reliaquest.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.reliaquest.api.model.Employee;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmployeeService {
    private final EmployeeAPIClient employeeApiClient;
    private static final TypeReference<List<Employee>> employeeListTypeReference = new TypeReference<>() {};

    public EmployeeService(EmployeeAPIClient employeeApiClient) {
        this.employeeApiClient = employeeApiClient;
    }

    public List<Employee> getAllEmployees() {
        return employeeApiClient
                .get("/api/v1/employee", employeeListTypeReference)
                .join();
    }
}
