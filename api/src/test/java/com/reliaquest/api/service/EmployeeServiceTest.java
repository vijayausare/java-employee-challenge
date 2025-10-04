package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.APIException;
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

    @Test
    void shouldReturnEmployeesWhenNameContainsSearchString() {
        String searchString = "Al";
        when(employeeApiClient.get(any(), any())).thenReturn(CompletableFuture.completedFuture(mockEmployeeList));

        List<Employee> receivedEmployees = employeeService.getEmployeesByNameSearch(searchString);

        assertEquals(2, receivedEmployees.size());
        verify(employeeApiClient, times(1)).get(argumentCaptor.capture(), any());
        assertEquals(EMPLOYEE_SERVER_API_PATH, argumentCaptor.getValue());
    }

    @Test
    void itShouldReturnHighestSalaryOfEmployee() {
        when(employeeApiClient.get(any(), any())).thenReturn(CompletableFuture.completedFuture(mockEmployeeList));

        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

        assertEquals(3000, highestSalary);
        verify(employeeApiClient, times(1)).get(argumentCaptor.capture(), any());
        assertEquals(EMPLOYEE_SERVER_API_PATH, argumentCaptor.getValue());
    }

    @Test
    void itShouldReturnEmployeeByGivenId() {
        String id = "123e4567-e89b-12d3-a456-426614174000";
        Employee mockEmployee = new Employee(
                UUID.randomUUID(), "Alice Johnson", 1800, 28, "Backend Developer", "alice.johnson@example.com");

        when(employeeApiClient.get(any(), any())).thenReturn(CompletableFuture.completedFuture(mockEmployee));

        Employee receivedEmployee = employeeService.getEmployeeById(id);

        assertEquals(mockEmployee, receivedEmployee);
        verify(employeeApiClient, times(1)).get(argumentCaptor.capture(), any());
        assertEquals(EMPLOYEE_SERVER_API_PATH + "/" + id, argumentCaptor.getValue());
    }

    @Test
    void itShouldThrowExceptionIfEmployeeNotFound() {
        String id = "abcbc123-4567-890a-bcde-fghij123456789";
        when(employeeApiClient.get(any(), any())).thenThrow(new APIException(404, "Employee not found"));

        assertThrows(APIException.class, () -> employeeService.getEmployeeById(id));
    }

    @Test
    void shouldReturnTopKEmployeesBySalary() {
        Integer k = 2;
        when(employeeApiClient.get(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mockEmployeeList));

        List<String> receivedEmployees = employeeService.getTopEmployeesBySalary(k);

        assertEquals(2, receivedEmployees.size());
        assertEquals("Diana Prince", receivedEmployees.get(0)); // highest salary
        assertEquals("Bob Johnson", receivedEmployees.get(1));  // second highest salary

        verify(employeeApiClient, times(1)).get(argumentCaptor.capture(), any());
        assertEquals(EMPLOYEE_SERVER_API_PATH, argumentCaptor.getValue());
    }

}
