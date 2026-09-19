package com.jobportal.application.controller;

import com.jobportal.application.dto.ApplicationRequest;
import com.jobportal.application.entity.ApplicationStatus;
import com.jobportal.application.entity.JobApplication;
import com.jobportal.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplication> apply(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(request));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<JobApplication>> getByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getByCandidate(candidateId));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplication>> getByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getByJob(jobId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobApplication> updateStatus(@PathVariable Long id, @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }
}
