package com.cibil.cibil_score_service.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.cibil.dto.EmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "application-service",
        url = "http://localhost:8082")
public interface ApplicationClient {

    @GetMapping("/api/application/{appId}")
    ApplicationDTO getApplication(
            @PathVariable Integer appId);
}