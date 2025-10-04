package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.controller.request.EmployeeCreationInput;
import com.reliaquest.api.model.Employee;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeApiClientTest {

    private HttpClient httpClient;
    private EmployeeAPIClient apiClient;

    @BeforeEach
    void setup() {
        httpClient = mock(HttpClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        String baseUrl = "http://localhost:8080";
        apiClient = new EmployeeAPIClient(httpClient, baseUrl, objectMapper);
    }

    @Test
    void testGet_successfulResponse() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        String jsonResponse = "{\n" + "  \"data\": {\n"
                + "    \"id\": \""
                + employeeId + "\",\n" + "    \"name\": \"Alice Smith\",\n"
                + "    \"role\": \"QA Engineer\",\n"
                + "    \"salary\": 1200,\n"
                + "    \"age\": 30,\n"
                + "    \"email\": \"alice.smith@gmail.com\"\n"
                + "  }\n"
                + "}";

        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<Employee> result = apiClient.get("/employees/" + employeeId, new TypeReference<>() {});

        Employee employee = result.join();
        assertNotNull(employee);
        assertEquals(employeeId, employee.getId().toString());
        assertEquals("Alice Smith", employee.getName());
        assertEquals(1200, employee.getSalary());
        assertEquals(30, employee.getAge());
        assertEquals("alice.smith@gmail.com", employee.getEmail());
    }

    @Test
    void shouldReturnEmployeeWhenPostIsSuccessfulWithDifferentInput() {
        String jsonResponse = "{\"data\":{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"Alice Johnson\"}}";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        EmployeeCreationInput input =
                new EmployeeCreationInput("Alice Johnson", 2200, 28, "Backend Developer", "alice.johnson@example.com");

        CompletableFuture<Employee> result = apiClient.post("/employees", input, new TypeReference<Employee>() {});
        Employee employee = result.join();

        assertNotNull(employee);
        assertEquals("123e4567-e89b-12d3-a456-426614174000", employee.getId().toString());
        assertEquals("Alice Johnson", employee.getName());
    }
}
