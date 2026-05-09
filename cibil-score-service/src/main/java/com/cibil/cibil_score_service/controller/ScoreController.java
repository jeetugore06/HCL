package com.cibil.cibil_score_service.controller;

import com.cibil.cibil_score_service.dto.CreditRequest;
import com.cibil.cibil_score_service.dto.ScoreRequest;
import com.cibil.cibil_score_service.repository.ScoreRepository;
import com.cibil.cibil_score_service.service.ScoreService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cibil")
public class ScoreController {

     @Autowired
    private ScoreService service;

     @PostMapping("/check")
    public int check(
            @RequestBody CreditRequest request) {

        return service.checkScore(request);
    }
}