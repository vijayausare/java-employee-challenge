package com.reliaquest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.APIException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class EmployeeAPIClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public EmployeeAPIClient(
            HttpClient httpClient,
            @Value("${urls.employee_server_base_url}") String baseUrl,
            ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    public <T> CompletableFuture<T> get(String uri, TypeReference<T> typeReference) {
        HttpRequest request =
                HttpRequest.newBuilder(URI.create(baseUrl + uri)).GET().build();
        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    if (response.statusCode() != 200) {
                        throw new APIException(response.statusCode(), response.body());
                    }
                    String body = response.body();
                    try {
                        JsonNode responseString = objectMapper.readValue(body, JsonNode.class);
                        return objectMapper.readValue(responseString.get("data").toString(), typeReference);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public <T> CompletableFuture<T> post(String uri, Object body, TypeReference<T> typeReference) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + uri))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .header("Content-Type", "application/json")
                    .build();

            return httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApplyAsync(response -> {
                        if (response.statusCode() != 200) {
                            throw new APIException(response.statusCode(), response.body());
                        }
                        try {
                            JsonNode responseString = objectMapper.readValue(response.body(), JsonNode.class);
                            return objectMapper.readValue(
                                    responseString.get("data").toString(), typeReference);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> delete(String uri, Object body) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + uri))
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .header("Content-Type", "application/json")
                    .build();

            return httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApplyAsync(response -> {
                        if (response.statusCode() != 200) {
                            throw new APIException(response.statusCode(), response.body());
                        }
                        try {
                            JsonNode responseString = objectMapper.readValue(response.body(), JsonNode.class);
                            return responseString.get("data").asBoolean();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
