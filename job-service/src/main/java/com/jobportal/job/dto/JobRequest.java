package com.jobportal.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String company;

    private String description;

    private String location;

    private Double salary;

    @NotNull
    private Long recruiterId;
}
