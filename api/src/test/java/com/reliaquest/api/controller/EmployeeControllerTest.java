package com.reliaquest.api.controller;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.reliaquest.api.controller.request.DeleteEmployeeInput;
import com.reliaquest.api.controller.request.EmployeeCreationInput;
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
            new Employee(
                    UUID.randomUUID(), "Fiona Gallagher", 1500, 26, "Support Engineer", "fiona.gallagher@gmail.com"),
            new Employee(UUID.randomUUID(), "George Michael", 2700, 33, "Tech Lead", "george.michael@gmail.com"));

    EmployeeServerMocks employeeServerMocks = new EmployeeServerMocks(employeeServerWireMockRule);

    @Test
    void shouldReturnAllEmployees() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(mockEmployeeList.size())));
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
    void shouldReturnErrorWhileGettingEmployeeByIdWhenEmployeeDoesNotExist() throws Exception {
        Employee mockEmployee = mockEmployeeList.get(0);
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee/" + mockEmployee.getId(), 404, "Employee not found with ID " + mockEmployee.getId());

        mockMvc.perform(get("/" + mockEmployee.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Employee not found with ID " + mockEmployee.getId()));
    }

    @Test
    void itShouldSearchEmployeeByName() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/search/Li"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].name").value("Alice Smith"))
                .andExpect(jsonPath("$.[1].name").value("Charlie Brown"));
    }

    @Test
    void shouldReturnHighestSalaryOfEmployee() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3000));
    }

    @Test
    void shouldReturnNamesOfTop10EarningEmployees() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        EmployeeCreationInput employeeCreationInput =
                new EmployeeCreationInput("John Clair", 1000, 30, "CTO", "john@rq.com");

        String createEmployeeResponse =
                """
      {
        "data": {
          "id": "4a3a170b-22cd-4ac2-aad1-9bb5b34a1507",
          "employee_name": "John Clair",
          "employee_salary": 1000,
          "employee_age": 30,
          "employee_title": "CTO",
          "employee_email": "john@rq.com"
        },
        "status": "Successfully processed request."
      }
      """;

        employeeServerMocks.mockPostApiCall(
                "/api/v1/employee",
                200,
                objectMapper.writeValueAsString(employeeCreationInput),
                createEmployeeResponse);

        mockMvc.perform(post("/")
                        .content(objectMapper.writeValueAsString(employeeCreationInput))
                        .contentType("application/json"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507"))
                .andExpect(jsonPath("$.name").value("John Clair"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.salary").value(1000))
                .andExpect(jsonPath("$.title").value("CTO"));
    }

    @Test
    void shouldDeleteEmployeeById() throws Exception {
        Employee mockEmployee = mockEmployeeList.get(0);

        // Mock GET call to check if employee exists
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee/" + mockEmployee.getId(),
                200,
                getEnclosedResponse(objectMapper.writeValueAsString(mockEmployee)));

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
                "/api/v1/employee/" + mockEmployee.getId(), 200, getEnclosedResponse("null"));

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
