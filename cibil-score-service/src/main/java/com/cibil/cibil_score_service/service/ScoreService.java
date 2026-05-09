package com.cibil.service;

import com.cibil.cibil_score_service.FeignClient.ApplicationClient;
import com.cibil.cibil_score_service.dto.ApplicationDTO;
import com.cibil.cibil_score_service.dto.CreditRequest;
import com.cibil.dto.ScoreRequest;
import com.cibil.entity.Score;
import com.cibil.repository.ScoreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository repository;

    @Autowired
    private ApplicationClient client;

    public ScoreRequest checkScore(CreditRequest request) {

        // Existing Score Check
         Optional<CustomerCredit> existingCustomer =
                repository.findByPanNumber(
                        request.getPanNo());

        // If score already exists
        if (existingCustomer.isPresent()) {

            return existingCustomer
                    .get()
                    .getScore();
        }

        int score = calculateScore(request);

        // Save New Record
        CustomerCredit customer =
                new CustomerCredit();

        customer.setPanNumber(
                request.getPanNo());

        customer.setScore(score);

        repository.save(customer);

        return score;
    }

    private int calculateScore(ApplicationDTO app) {

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