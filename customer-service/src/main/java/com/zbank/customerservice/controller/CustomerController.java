package com.zbank.customerservice.controller;

import com.zbank.customerservice.dto.CardActivationRequest;
import com.zbank.customerservice.dto.CardActivationResponse;
import com.zbank.customerservice.dto.GetUserResponse;
import com.zbank.customerservice.service.CardActivationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer-service")
public class CustomerController {

    private final CardActivationService cardActivationService;

    public CustomerController(CardActivationService cardActivationService) {
        this.cardActivationService = cardActivationService;
    }

    @PostMapping("/activate-card")
    public ResponseEntity<CardActivationResponse> activateCard(
            @Valid @RequestBody CardActivationRequest request
    ) {
        return ResponseEntity.ok(cardActivationService.activateCard(request));
    }

    @GetMapping("/get-user")
    public ResponseEntity<GetUserResponse> getUser(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(cardActivationService.getUserFromToken(authorizationHeader));
    }
}
