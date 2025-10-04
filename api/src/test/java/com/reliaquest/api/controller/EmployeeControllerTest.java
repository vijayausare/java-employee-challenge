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
import org.junit.jupiter.api.BeforeEach;
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

    List<Employee> mockEmployeeList;
    EmployeeServerMocks employeeServerMocks = new EmployeeServerMocks(employeeServerWireMockRule);

    @BeforeEach
    void setUp() {
        mockEmployeeList = List.of(
                new Employee(UUID.randomUUID(), "Alice Smith", 1200, 30, "QA Engineer", "alice.smith@gmail.com"),
                new Employee(UUID.randomUUID(), "Bob Johnson", 2500, 35, "DevOps Engineer", "bob.johnson@gmail.com"),
                new Employee(
                        UUID.randomUUID(), "Charlie Brown", 1800, 28, "Backend Developer", "charlie.brown@gmail.com"),
                new Employee(
                        UUID.randomUUID(), "Diana Prince", 3000, 32, "Frontend Developer", "diana.prince@gmail.com"),
                new Employee(UUID.randomUUID(), "Ethan Hunt", 2200, 40, "Security Analyst", "ethan.hunt@gmail.com"),
                new Employee(
                        UUID.randomUUID(),
                        "Fiona Gallagher",
                        1500,
                        26,
                        "Support Engineer",
                        "fiona.gallagher@gmail.com"),
                new Employee(UUID.randomUUID(), "George Michael", 2700, 33, "Tech Lead", "george.michael@gmail.com"));
    }

    @Test
    void shouldReturnAllEmployees() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(mockEmployeeList.size())))
                .andExpect(jsonPath("$.[0].name").value("Alice Smith"))
                .andExpect(jsonPath("$.[1].name").value("Bob Johnson"))
                .andExpect(jsonPath("$.[2].name").value("Charlie Brown"));
    }

    @Test
    void shouldReturnEmployeeById() throws Exception {
        Employee mockEmployee = mockEmployeeList.get(0);
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee/" + mockEmployee.getId(),
                200,
                getEnclosedResponse(objectMapper.writeValueAsString(mockEmployee)));

        mockMvc.perform(get("/" + mockEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.salary").value(1200))
                .andExpect(jsonPath("$.title").value("QA Engineer"));
    }

    @Test
    void shouldReturnErrorIfEmployeeNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee/" + randomId, 404, "Employee not found with ID " + randomId);

        mockMvc.perform(get("/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Employee not found with ID " + randomId));
    }

    @Test
    void shouldSearchEmployeesByName() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/search/an")) // matches Diana and Ethan
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].name").value("Diana Prince"))
                .andExpect(jsonPath("$.[1].name").value("Ethan Hunt"));
    }

    @Test
    void shouldReturnHighestSalary() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3000)); // Diana Prince has the highest
    }

    @Test
    void shouldReturnTopTenHighestEarningEmployeeNames() throws Exception {
        employeeServerMocks.mockGetApiCall(
                "/api/v1/employee", 200, getEnclosedResponse(objectMapper.writeValueAsString(mockEmployeeList)));

        mockMvc.perform(get("/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(mockEmployeeList.size())))
                .andExpect(jsonPath("$.[0]").value("Diana Prince"))
                .andExpect(jsonPath("$.[1]").value("George Michael"))
                .andExpect(jsonPath("$.[2]").value("Bob Johnson"));
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        EmployeeCreationInput input =
                new EmployeeCreationInput("Harry Potter", 1800, 28, "Wizard", "harry@hogwarts.com");

        String response =
                """
          {
            "data": {
              "id": "f1d2d2f9-0a45-4f6b-9fdd-5bb4f31d0c90",
              "name": "Harry Potter",
              "salary": 1800,
              "age": 28,
              "title": "Wizard",
              "email": "harry@hogwarts.com"
            },
            "status": "Successfully processed request."
          }
          """;

        employeeServerMocks.mockPostApiCall("/api/v1/employee", 200, objectMapper.writeValueAsString(input), response);

        mockMvc.perform(post("/")
                        .content(objectMapper.writeValueAsString(input))
                        .contentType("application/json"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("f1d2d2f9-0a45-4f6b-9fdd-5bb4f31d0c90"))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.salary").value(1800))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.title").value("Wizard"));
    }

    @Test
    void shouldDeleteEmployeeById() throws Exception {
        Employee mockEmployee = mockEmployeeList.get(0);
        employeeServerMocks.mockDeleteApiCall(
                "/api/v1/employee",
                200,
                objectMapper.writeValueAsString(new DeleteEmployeeInput(mockEmployee.getName())),
                """
                  {
                    "data": true
                  }
                  """);

        mockMvc.perform(delete("/" + mockEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(mockEmployee.getName()));
    }

    @Test
    void shouldReturnBadRequestIfEmployeeNotFoundWhileDeleting() throws Exception {
        UUID randomId = UUID.randomUUID();
        employeeServerMocks.mockGetApiCall("/api/v1/employee/" + randomId, 200, getEnclosedResponse("null"));

        mockMvc.perform(delete("/" + randomId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Employee not found with ID " + randomId));
    }

    private String getEnclosedResponse(String response) {
        return """
          {
            "data": %s
          }
          """.formatted(response);
    }
}
