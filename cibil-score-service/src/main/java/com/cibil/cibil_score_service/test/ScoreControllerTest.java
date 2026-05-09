package com.cibil.cibil_score_service.test;

import com.cibil.cibil_score_service.controller.ScoreController;
import com.cibil.cibil_score_service.dto.CreditRequest;
import com.cibil.cibil_score_service.dto.ScoreRequest;
import com.cibil.cibil_score_service.service.ScoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(SpringExtension.class)
@WebMvcTest(ScoreController.class)
public class ScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScoreService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCheckScore() throws Exception {

        ScoreRequest request = new ScoreRequest();
        request.setFullName("Rahul");
        request.setPanNo("ABCDE1234F");
        request.setAnnualSalary(250000.0);
        request.setTotalCards(1);

        when(service.checkScore(any(ScoreRequest.class)))
                .thenReturn(500);

        mockMvc.perform(post("/api/cibil/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("500"));
    }
}