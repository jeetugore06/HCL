package com.cibil.service;

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

    public ScoreRequest checkScore(Integer appId) {

        // Existing Score Check
        Optional<ScoreRequest> existing =
                repository.findByAppId(appId);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Call Another Microservice
        ApplicationDTO applicationData =
                client.getApplication(appId);

        int score = calculateScore(applicationData);

        ScoreRequest credit = new ScoreRequest();

        credit.setAppId(appId);
        credit.setScore(score);
        repository.save(credit)
        
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