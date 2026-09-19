package com.jobportal.application.repository;

import com.jobportal.application.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCandidateId(Long candidateId);
    List<JobApplication> findByJobId(Long jobId);
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
}
