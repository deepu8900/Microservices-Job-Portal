package com.jobportal.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationSubmittedEvent {
    private Long applicationId;
    private Long jobId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
}
