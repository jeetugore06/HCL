package com.hcl.hackathon.registration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.hackathon.registration.dto.ApplicationDetailDTO;
import com.hcl.hackathon.registration.dto.ApplicationRequestDTO;
import com.hcl.hackathon.registration.dto.ApplicationResponseDTO;
import com.hcl.hackathon.registration.service.RegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> apply(@Valid @RequestBody ApplicationRequestDTO request) {
        log.info("POST /api/v1/applications — incoming application for email: {}", request.getEmail());
        ApplicationResponseDTO response = registrationService.processApplication(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Returns the oldest application currently in the SUBMITTED queue.
     * 200 with the application body when one is available; 204 No Content otherwise.
     */
    @GetMapping("/pending/next")
    public ResponseEntity<ApplicationDetailDTO> getNextPending() {
        log.info("GET /api/v1/applications/pending/next");
        return registrationService.getNextPendingApplication()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<ApplicationDetailDTO> getByReference(@PathVariable String reference) {
        log.info("GET /api/v1/applications/reference/{}", reference);
        return ResponseEntity.ok(registrationService.getApplicationByReference(reference));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/applications/{}", id);
        registrationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
