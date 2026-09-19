package com.jobportal.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotNull
    private Long jobId;

    @NotNull
    private Long candidateId;

    @NotBlank
    private String candidateName;

    @NotBlank
    private String candidateEmail;
}
