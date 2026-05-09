package com.hcl.hackathon.scoring.controller;

import com.hcl.hackathon.scoring.dto.request.CardRegistrationRequest;
import com.hcl.hackathon.scoring.dto.response.CardRegistrationResponse;
import com.hcl.hackathon.scoring.service.CardRegistrationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardRegistrationController {

    private final CardRegistrationService service;

    @PostMapping("/{appId}")
    public ResponseEntity<CardRegistrationResponse>
    registerCard(@PathVariable String appId) {

        CardRegistrationRequest request =
                new CardRegistrationRequest();

        request.setApplicationId(appId);

        return ResponseEntity.ok(
                service.registerCard(request));
    }
}