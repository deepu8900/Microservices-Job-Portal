package com.jobportal.job.kafka;

import com.jobportal.job.dto.JobPostedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobEventProducer {

    public static final String TOPIC = "job.posted";

    private final KafkaTemplate<String, JobPostedEvent> kafkaTemplate;

    public JobEventProducer(KafkaTemplate<String, JobPostedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishJobPosted(JobPostedEvent event) {
        kafkaTemplate.send(TOPIC, event.getJobId().toString(), event);
    }
}
