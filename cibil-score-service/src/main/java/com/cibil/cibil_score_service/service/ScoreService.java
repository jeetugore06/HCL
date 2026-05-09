package com.cibil.cibil_score_service.service;

import com.cibil.cibil_score_service.dto.CreditRequest;
import com.cibil.cibil_score_service.dto.ScoreRequest;
import com.cibil.cibil_score_service.entity.Cibil;
import com.cibil.cibil_score_service.repository.ScoreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Optional;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository repository;

    public int checkScore(CreditRequest request) {

        // Existing Score Check
        Optional<Cibil> existingCustomer =
                repository.findByPanNo(request.getPanNo());

        // If already exists
        if (existingCustomer.isPresent()) {
            return existingCustomer.get().getScore();
        }

        // Calculate score
        int score = calculateScore(request);

        // Save new record
        Cibil customer = new Cibil();
        customer.setPanNo(request.getPanNo());
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