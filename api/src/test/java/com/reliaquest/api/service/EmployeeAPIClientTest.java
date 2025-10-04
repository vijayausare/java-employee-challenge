package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.controller.request.DeleteEmployeeInput;
import com.reliaquest.api.controller.request.EmployeeCreationInput;
import com.reliaquest.api.model.Employee;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
    void testGet_successfulResponse() {
        String jsonResponse = "{\"data\":{\"id\":\"596205c5-e4dc-4b0e-89dc-b2ec6dc758ea\",\"name\":\"Alice Smith\"}}";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<Employee> result =
                apiClient.get("/employees/596205c5-e4dc-4b0e-89dc-b2ec6dc758ea", new TypeReference<>() {});

        Employee employee = result.join();
        assertNotNull(employee);
        assertEquals("596205c5-e4dc-4b0e-89dc-b2ec6dc758ea", employee.getId().toString());
        assertEquals("Alice Smith", employee.getName());
    }

    @Test
    void testPost_successfulResponse() {
        String jsonResponse = "{\"data\":{\"id\":\"987e6543-e21b-45d3-b456-123456789abc\",\"name\":\"Bob Johnson\"}}";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<Employee> result = apiClient.post(
                "/employees",
                new EmployeeCreationInput("Bob Johnson", 2500, 35, "DevOps Engineer", "bob.johnson@example.com"),
                new TypeReference<Employee>() {});

        Employee employee = result.join();
        assertNotNull(employee);
        assertEquals("987e6543-e21b-45d3-b456-123456789abc", employee.getId().toString());
        assertEquals("Bob Johnson", employee.getName());
    }

    @Test
    void testDelete_successfulResponse() {
        String jsonResponse = "{\"data\":true}";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<Boolean> result = apiClient.delete("/employee", new DeleteEmployeeInput("Charlie Brown"));

        boolean isDeleted = result.join();
        assertTrue(isDeleted);
    }

    @Test
    void testGet_unsuccessfulResponse() {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpResponse.body()).thenReturn("Employee not found");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<Employee> result =
                apiClient.get("/employees/596205c5-e4dc-4b0e-89dc-b2ec6dc758ea", new TypeReference<>() {});

        assertThrows(CompletionException.class, result::join);
    }

    @Test
    void testPost_unsuccessfulResponse() {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("Invalid input");

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<Employee> result = apiClient.post(
                "/employees",
                new EmployeeCreationInput("Diana Prince", 3000, 32, "Frontend Developer", "diana.prince@example.com"),
                new TypeReference<Employee>() {});

        assertThrows(CompletionException.class, result::join);
    }

    @Test
    void testDelete_unsuccessfulResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("Invalid input");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<Boolean> result = apiClient.delete("/employee", new DeleteEmployeeInput("Ethan Hunt"));

        assertThrows(CompletionException.class, result::join);
    }
}
