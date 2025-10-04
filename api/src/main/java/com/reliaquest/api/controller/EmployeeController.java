package com.reliaquest.api.controller;

import com.reliaquest.api.controller.request.EmployeeCreationInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class EmployeeController implements IEmployeeController<Employee, EmployeeCreationInput> {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Fetching all employees from service.");
        List<Employee> employees = employeeService.getAllEmployees();
        log.info("Fetched {} employees.", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees with name containing: '{}'", searchString);
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString);
        log.info("Found {} employees matching '{}'", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Fetching employee with ID: {}", id);
        Employee employee = employeeService.getEmployeeById(id);
        log.info("Employee fetched successfully: ID {}", id);
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Fetching the highest salary among employees.");
        int highestSalary = employeeService.getHighestSalaryOfEmployees();
        log.info("Highest salary calculated: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employee names.");
        List<String> topEmployees = employeeService.getTopEmployeesBySalary(10);
        log.info("Top 10 employees fetched successfully.");
        return ResponseEntity.ok(topEmployees);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(EmployeeCreationInput employeeInput) {
        log.info("Creating new employee with input: {}", employeeInput);
        Employee createdEmployee = employeeService.createEmployee(employeeInput);
        log.info("Employee created successfully with ID: {}", createdEmployee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("Deleting employee with ID: {}", id);
        String response = employeeService.deleteEmployee(id);
        log.info("Employee deleted successfully with ID: {}", id);
        return ResponseEntity.ok(response);
    }
}
