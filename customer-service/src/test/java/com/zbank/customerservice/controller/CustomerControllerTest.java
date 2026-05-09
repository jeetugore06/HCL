package com.zbank.customerservice.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbank.customerservice.config.SecurityConfig;
import com.zbank.customerservice.dto.CardActivationRequest;
import com.zbank.customerservice.dto.CardActivationResponse;
import com.zbank.customerservice.dto.GetUserResponse;
import com.zbank.customerservice.service.CardActivationService;
import com.zbank.customerservice.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardActivationService cardActivationService;

    @Test
    void activateCard_shouldReturn200ForValidRequest() throws Exception {
        CardActivationRequest request = TestDataFactory.validActivationRequest();
        CardActivationResponse response = new CardActivationResponse("Card activated successfully", "jwt-token", "Bearer", 3600L);
        when(cardActivationService.activateCard(request)).thenReturn(response);

        mockMvc.perform(post("/customer-service/activate-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Card activated successfully"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void activateCard_shouldReturn400ForInvalidRequest() throws Exception {
        String invalidPayload = """
                {
                  "creditCardNumber": "123",
                  "pin": "12",
                  "documentNumber": "DOC-123",
                  "newPin": "5678",
                  "newPinConfirmed": "5678"
                }
                """;

        mockMvc.perform(post("/customer-service/activate-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUser_shouldReturnCustomerNameWhenHeaderPresent() throws Exception {
        when(cardActivationService.getUserFromToken("Bearer token-value"))
                .thenReturn(new GetUserResponse("Sohum Shah"));

        mockMvc.perform(get("/customer-service/get-user")
                        .header("Authorization", "Bearer token-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Sohum Shah"));

        verify(cardActivationService).getUserFromToken("Bearer token-value");
    }

    @Test
    void getUser_shouldReturn400WhenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(get("/customer-service/get-user"))
                .andExpect(status().isBadRequest());
    }
}
