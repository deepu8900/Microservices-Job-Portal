package com.jobportal.job.service;

import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.entity.Job;
import com.jobportal.job.kafka.JobEventProducer;
import com.jobportal.job.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobEventProducer jobEventProducer;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJobSavesAndPublishesEvent() {
        JobRequest request = new JobRequest();
        request.setTitle("Backend Engineer");
        request.setCompany("Acme Corp");
        request.setLocation("Remote");
        request.setRecruiterId(1L);

        Job saved = Job.builder().id(1L).title("Backend Engineer").company("Acme Corp")
                .recruiterId(1L).build();

        when(jobRepository.save(any(Job.class))).thenReturn(saved);

        Job result = jobService.createJob(request);

        assertEquals("Backend Engineer", result.getTitle());
        verify(jobEventProducer).publishJobPosted(any());
    }

    @Test
    void getJobThrowsWhenNotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());
        try {
            jobService.getJob(99L);
        } catch (IllegalArgumentException ex) {
            assertEquals("Job not found: 99", ex.getMessage());
        }
    }
}
