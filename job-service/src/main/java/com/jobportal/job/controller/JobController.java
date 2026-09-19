package com.jobportal.job.controller;

import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.entity.Job;
import com.jobportal.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<Job> createJob(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Job>> getByRecruiter(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Job>> search(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String location) {
        return ResponseEntity.ok(jobService.search(keyword, location));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
