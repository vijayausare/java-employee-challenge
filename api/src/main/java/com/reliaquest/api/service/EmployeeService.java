package com.reliaquest.api.service;

import static com.reliaquest.api.utils.StringUtils.containsString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.reliaquest.api.controller.request.DeleteEmployeeInput;
import com.reliaquest.api.controller.request.EmployeeCreationInput;
import com.reliaquest.api.exception.APIException;
import com.reliaquest.api.model.Employee;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmployeeService {
    private final EmployeeAPIClient employeeApiClient;
    private static final TypeReference<List<Employee>> employeeListTypeReference = new TypeReference<>() {};
    private static final TypeReference<Employee> employeeTypeReference = new TypeReference<>() {};

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

    public Employee getEmployeeById(String id) {
        log.debug("Getting employee for ID: {}", id);

        return employeeApiClient
                .get("/api/v1/employee/" + id, employeeTypeReference)
                .join();
    }

    public List<String> getTopEmployeesBySalary(Integer limit) {
        List<Employee> allEmployees = getAllEmployees();
        log.debug("Returning top {} earning employees out of {} employees", limit, allEmployees.size());

        return allEmployees.stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(limit)
                .map(Employee::getName)
                .toList();
    }

    public Employee createEmployee(EmployeeCreationInput input) {
        return employeeApiClient
                .post("/api/v1/employee", input, employeeTypeReference)
                .join();
    }

    public String deleteEmployee(String id) {
        log.debug("Deleting employee with ID: {}", id);
        Employee employee = getEmployeeById(id);

        boolean isDeleted = Objects.nonNull(employee)
                ? employeeApiClient
                        .delete("/api/v1/employee", new DeleteEmployeeInput(employee.getName()))
                        .join()
                : false;

        if (isDeleted) {
            log.debug("Employee deleted with ID: {}", employee.getId());
            return employee.getName();
        } else {
            log.error("Delete Employee: Employee not found with ID: {}", id);
            throw new APIException(400, "Employee not found with ID %s".formatted(id));
        }
    }
}
