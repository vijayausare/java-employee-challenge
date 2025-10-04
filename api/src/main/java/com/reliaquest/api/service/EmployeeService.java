package com.reliaquest.api.service;

import static com.reliaquest.api.utils.StringUtils.containsString;

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

    public List<Employee> getEmployeesByNameSearch(String searchString) {
        List<Employee> employees = getAllEmployees();
        log.debug("Searching for input string: {} in {} employees", searchString, employees.size());
        return employees.stream()
                .filter(e -> containsString(e.getName(), searchString))
                .toList();
    }

    public Integer getHighestSalaryOfEmployees() {
        List<Employee> allEmployees = getAllEmployees();
        log.debug("Get highest salary of employee out of {} employees", allEmployees.size());

        return allEmployees.stream().mapToInt(Employee::getSalary).max().orElse(0);
    }
}
