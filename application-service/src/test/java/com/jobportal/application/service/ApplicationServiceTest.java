package com.jobportal.application.service;

import com.jobportal.application.dto.ApplicationRequest;
import com.jobportal.application.entity.JobApplication;
import com.jobportal.application.kafka.ApplicationEventProducer;
import com.jobportal.application.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private ApplicationEventProducer eventProducer;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void applySavesAndPublishesEvent() {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(1L);
        request.setCandidateId(2L);
        request.setCandidateName("Jane Doe");
        request.setCandidateEmail("jane@example.com");

        when(repository.existsByJobIdAndCandidateId(1L, 2L)).thenReturn(false);
        when(repository.save(any(JobApplication.class))).thenReturn(
                JobApplication.builder().id(10L).jobId(1L).candidateId(2L)
                        .candidateName("Jane Doe").candidateEmail("jane@example.com").build());

        JobApplication result = applicationService.apply(request);

        assertEquals(10L, result.getId());
        verify(eventProducer).publishApplicationSubmitted(any());
    }

    @Test
    void applyThrowsWhenDuplicate() {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(1L);
        request.setCandidateId(2L);
        request.setCandidateName("Jane Doe");
        request.setCandidateEmail("jane@example.com");

        when(repository.existsByJobIdAndCandidateId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> applicationService.apply(request));
    }
}
