package com.jobportal.job.service;

import com.jobportal.job.dto.JobPostedEvent;
import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.entity.Job;
import com.jobportal.job.kafka.JobEventProducer;
import com.jobportal.job.repository.JobRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobEventProducer jobEventProducer;

    public JobService(JobRepository jobRepository, JobEventProducer jobEventProducer) {
        this.jobRepository = jobRepository;
        this.jobEventProducer = jobEventProducer;
    }

    @CacheEvict(value = "jobSearch", allEntries = true)
    public Job createJob(JobRequest request) {
        Job job = Job.builder()
                .title(request.getTitle())
                .company(request.getCompany())
                .description(request.getDescription())
                .location(request.getLocation())
                .salary(request.getSalary())
                .recruiterId(request.getRecruiterId())
                .build();

        Job saved = jobRepository.save(job);

        jobEventProducer.publishJobPosted(JobPostedEvent.builder()
                .jobId(saved.getId())
                .title(saved.getTitle())
                .company(saved.getCompany())
                .recruiterId(saved.getRecruiterId())
                .build());

        return saved;
    }

    public Job getJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
    }

    public List<Job> getJobsByRecruiter(Long recruiterId) {
        return jobRepository.findByRecruiterId(recruiterId);
    }

    @Cacheable(value = "jobSearch", key = "#keyword + '-' + #location")
    public List<Job> search(String keyword, String location) {
        return jobRepository.search(keyword, location);
    }

    @CacheEvict(value = "jobSearch", allEntries = true)
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}
