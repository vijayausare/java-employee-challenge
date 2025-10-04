package com.reliaquest.api.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

public class EmployeeServerMocks {
    private final WireMockExtension employeeServerExtension;

    public EmployeeServerMocks(WireMockExtension employeeServerExtension) {
        this.employeeServerExtension = employeeServerExtension;
    }

    public void mockGetApiCall(String uri, Integer status, String response) {
        employeeServerExtension.stubFor(
                get(urlEqualTo(uri)).willReturn(aResponse().withStatus(status).withBody(response)));
    }

    public void mockPostApiCall(String uri, Integer status, String request, String response) {
        employeeServerExtension.stubFor(post(urlEqualTo(uri))
                .withRequestBody(equalToJson(request))
                .willReturn(aResponse().withStatus(status).withBody(response)));
    }

    public void mockDeleteApiCall(String uri, Integer status, String request, String response) {
        employeeServerExtension.stubFor(delete(urlEqualTo(uri))
                .withRequestBody(equalToJson(request))
                .willReturn(aResponse().withStatus(status).withBody(response)));
    }
}
