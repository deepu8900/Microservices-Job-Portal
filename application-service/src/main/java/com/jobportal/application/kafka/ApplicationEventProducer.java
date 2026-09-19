package com.jobportal.application.kafka;

import com.jobportal.application.dto.ApplicationSubmittedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventProducer {

    public static final String TOPIC = "application.submitted";

    private final KafkaTemplate<String, ApplicationSubmittedEvent> kafkaTemplate;

    public ApplicationEventProducer(KafkaTemplate<String, ApplicationSubmittedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishApplicationSubmitted(ApplicationSubmittedEvent event) {
        kafkaTemplate.send(TOPIC, event.getApplicationId().toString(), event);
    }
}
