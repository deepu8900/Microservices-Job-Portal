package com.jobportal.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.notification.entity.Notification;
import com.jobportal.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "job.posted", groupId = "notification-service-group")
    public void onJobPosted(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Notification notification = Notification.builder()
                .recipientId(node.get("recruiterId").asLong())
                .message("Your job \"" + node.get("title").asText() + "\" at " + node.get("company").asText() + " is now live")
                .type("JOB_POSTED")
                .build();
        notificationRepository.save(notification);
    }

    @KafkaListener(topics = "application.submitted", groupId = "notification-service-group")
    public void onApplicationSubmitted(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Notification notification = Notification.builder()
                .recipientId(node.get("candidateId").asLong())
                .message("Your application for job #" + node.get("jobId").asLong() + " was submitted successfully")
                .type("APPLICATION_SUBMITTED")
                .build();
        notificationRepository.save(notification);
    }
}
