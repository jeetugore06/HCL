package com.hcl.hackathon.scoring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.hcl.hackathon.scoring.dto.response.CardRegistrationResponse;
import com.hcl.hackathon.scoring.service.CardRegistrationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardRegistrationController.class)
public class CardRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardRegistrationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterCardSuccessfully()
            throws Exception {

        CardRegistrationResponse response =
                CardRegistrationResponse.builder()
                        .applicationId("APP1001")
                        .status("APPROVED")
                        .cardType("PLATINUM")
                        .creditLimit(40000.0)
                        .maskedCardNumber(
                                "XXXX-XXXX-XXXX-1234")
                        .build();

        when(service.registerCard(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/card/APP1001")
                                .contentType(
                                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("APPROVED"))
                .andExpect(jsonPath("$.cardType")
                        .value("PLATINUM"));
    }
}