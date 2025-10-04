package com.reliaquest.api.controller;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.reliaquest.api.controller.request.DeleteEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @RegisterExtension
    public static WireMockExtension employeeServerWireMockRule =
            WireMockExtension.newInstance().options(wireMockConfig().port(8112)).build();

    // Updated employee list
    List<Employee> mockEmployeeList = List.of(
            new Employee(UUID.randomUUID(), "Alice Smith", 1200, 30, "QA Engineer", "alice.smith@gmail.com"),
            new Employee(UUID.randomUUID(), "Bob Johnson", 2500, 35, "DevOps Engineer", "bob.johnson@gmail.com"),
            new Employee(UUID.randomUUID(), "Charlie Brown", 1800, 28, "Backend Developer", "charlie.brown@gmail.com"),
            new Employee(UUID.randomUUID(), "Diana Prince", 3000, 32, "Frontend Developer", "diana.prince@gmail.com"),
            new Employee(UUID.randomUUID(), "Ethan Hunt", 2200, 40, "Security Analyst", "ethan.hunt@gmail.com"),
            new Employee(UUID.randomUUID(), "Fiona Gallagher", 1500, 26, "Support Engineer", "fiona.gallagher@gmail.com"),
            new Employee(UUID.randomUUID(), "George Michael", 2700, 33, "Tech Lead", "george.michael@gmail.com")
    );

    EmployeeServerMocks employeeServerMocks = new EmployeeServerMocks(employeeServerWireMockRule);

    @Test
    void shouldReturnAllEmployees() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(mockEmployeeList.size())));
    }

    @Test
    void itShouldReturnEmployeeById() throws Exception {
        Employee mockEmployee = mockEmployeeList.get(0);
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee/" + mockEmployee.getId(),
                200,
                getEnclosedResponse(objectMapper.writeValueAsString(mockEmployee)));

        mockMvc.perform(get("/" + mockEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(mockEmployee.getName()))
                .andExpect(jsonPath("$.age").value(mockEmployee.getAge()))
                .andExpect(jsonPath("$.salary").value(mockEmployee.getSalary()))
                .andExpect(jsonPath("$.title").value(mockEmployee.getTitle()));
    }

    @Test
    void shouldDeleteEmployeeById() throws Exception {
        Employee mockEmployee = mockEmployeeList.get(0);

        // Mock GET call to check if employee exists
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee/" + mockEmployee.getId(),
                200,
                getEnclosedResponse(objectMapper.writeValueAsString(mockEmployee))
        );

        // Mock DELETE call
        String deleteResponse = """
              {
                "data": true
              }
              """;

        String deleteEmployeeInput = objectMapper.writeValueAsString(new DeleteEmployeeInput(mockEmployee.getName()));
        employeeServerMocks.mockDeleteApiCall("/api/v1/employee", 200, deleteEmployeeInput, deleteResponse);

        mockMvc.perform(delete("/" + mockEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(mockEmployee.getName()));
    }

    @Test
    void itShouldReturnBadRequestIfEmployeeNotFoundWhileDeleting() throws Exception {
        Employee mockEmployee = mockEmployeeList.get(0);

        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee/" + mockEmployee.getId(),
                200,
                getEnclosedResponse("null")
        );

        mockMvc.perform(delete("/" + mockEmployee.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Employee not found with ID " + mockEmployee.getId()));
    }

    String getEnclosedResponse(String response) {
        return """
      {
        "data" : %s
      }
      """.formatted(response);
    }
}
