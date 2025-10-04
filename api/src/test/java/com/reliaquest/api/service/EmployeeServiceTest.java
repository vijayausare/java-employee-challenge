package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class EmployeeServiceTest {
    private static final String EMPLOYEE_SERVER_API_PATH = "/api/v1/employee";

    @Mock
    private EmployeeAPIClient employeeApiClient;

    @InjectMocks
    private EmployeeService employeeService;

    private List<Employee> mockEmployeeList;

    private ArgumentCaptor<String> argumentCaptor;

    @BeforeEach
    void setUp() {
        mockEmployeeList = List.of(
                new Employee(UUID.randomUUID(), "Alice Smith", 1200, 30, "QA Engineer", "alice.smith@gmail.com"),
                new Employee(UUID.randomUUID(), "Bob Johnson", 2500, 35, "DevOps Engineer", "bob.johnson@gmail.com"),
                new Employee(
                        UUID.randomUUID(), "Charlie Brown", 1800, 28, "Backend Developer", "charlie.brown@gmail.com"),
                new Employee(
                        UUID.randomUUID(), "Diana Prince", 3000, 32, "Frontend Developer", "diana.prince@gmail.com"),
                new Employee(UUID.randomUUID(), "Ethan Hunt", 2200, 40, "Security Analyst", "ethan.hunt@gmail.com"));

        argumentCaptor = ArgumentCaptor.forClass(String.class);
    }

    @Test
    void shouldGetAllEmployeesFromServer() {
        when(employeeApiClient.get(any(), any())).thenReturn(CompletableFuture.completedFuture(mockEmployeeList));

        List<Employee> receivedEmployees = employeeService.getAllEmployees();

        assertEquals(5, receivedEmployees.size());

        verify(employeeApiClient, times(1)).get(argumentCaptor.capture(), any());
        assertEquals(EMPLOYEE_SERVER_API_PATH, argumentCaptor.getValue());
    }
}
