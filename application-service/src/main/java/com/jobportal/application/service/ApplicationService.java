package com.jobportal.application.service;

import com.jobportal.application.dto.ApplicationRequest;
import com.jobportal.application.dto.ApplicationSubmittedEvent;
import com.jobportal.application.entity.ApplicationStatus;
import com.jobportal.application.entity.JobApplication;
import com.jobportal.application.kafka.ApplicationEventProducer;
import com.jobportal.application.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final JobApplicationRepository repository;
    private final ApplicationEventProducer eventProducer;

    public ApplicationService(JobApplicationRepository repository, ApplicationEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    public JobApplication apply(ApplicationRequest request) {
        if (repository.existsByJobIdAndCandidateId(request.getJobId(), request.getCandidateId())) {
            throw new IllegalArgumentException("Already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .jobId(request.getJobId())
                .candidateId(request.getCandidateId())
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .build();

        JobApplication saved = repository.save(application);

        eventProducer.publishApplicationSubmitted(ApplicationSubmittedEvent.builder()
                .applicationId(saved.getId())
                .jobId(saved.getJobId())
                .candidateId(saved.getCandidateId())
                .candidateName(saved.getCandidateName())
                .candidateEmail(saved.getCandidateEmail())
                .build());

        return saved;
    }

    public List<JobApplication> getByCandidate(Long candidateId) {
        return repository.findByCandidateId(candidateId);
    }

    public List<JobApplication> getByJob(Long jobId) {
        return repository.findByJobId(jobId);
    }

    public JobApplication updateStatus(Long id, ApplicationStatus status) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
        application.setStatus(status);
        return repository.save(application);
    }
}
