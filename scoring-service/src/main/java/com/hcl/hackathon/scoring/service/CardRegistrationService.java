package com.hcl.hackathon.scoring.service;

import com.hcl.hackathon.scoring.dto.request.CardRegistrationRequest;
import com.hcl.hackathon.scoring.dto.response.CardRegistrationResponse;

public interface CardRegistrationService {

    CardRegistrationResponse registerCard(
            CardRegistrationRequest request);

}