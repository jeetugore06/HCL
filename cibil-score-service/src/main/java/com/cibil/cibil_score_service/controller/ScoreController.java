package com.cibil.controller;

import com.cibil.dto.ScoreRequest;
import com.cibil.entity.Score;
import com.cibil.service.ScoreService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cibil")
public class ScoreController {

     @Autowired
    private ScoreService service;

     @PostMapping("/check")
    public int check(
            @RequestBody ScoreRequest request) {

        return service.checkScore(request);
    }
}