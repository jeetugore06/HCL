package com.cibil.cibil_score_service.service;

import com.cibil.cibil_score_service.dto.CreditRequest;
import com.cibil.cibil_score_service.dto.ScoreRequest;
import com.cibil.cibil_score_service.entity.Cibil;
import com.cibil.cibil_score_service.repository.ScoreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository repository;

    public int checkScore(CreditRequest request) {

        // Existing Score Check
         Optional<ScoreRequest> existingCustomer =
                repository.findByPanNumber(
                        request.getPanNo());

        // If score already exists
        if (existingCustomer.isPresent()) {

            return existingCustomer
                    .get()
                    .getScore();
        }

        int score = calculateScore(CreditRequest request);

        // Save New Record
        ScoreRequest customer =
                new ScoreRequest();

        customer.setPanNumber(
                request.getPanNo());

        customer.setScore(score);

        repository.save(customer);

        return score;
    }

    private int calculateScore( CreditRequest app) {

        if (app.getTotalCards() >= 2) {
            return 300;
        }

        if (app.getAnnualSalary() > 200000) {
            return 500;
        }

        if (app.getAnnualSalary() > 50000) {
            return 150;
        }

        return 50;
    }
}