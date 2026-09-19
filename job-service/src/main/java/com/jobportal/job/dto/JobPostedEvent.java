package com.jobportal.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostedEvent {
    private Long jobId;
    private String title;
    private String company;
    private Long recruiterId;
}
