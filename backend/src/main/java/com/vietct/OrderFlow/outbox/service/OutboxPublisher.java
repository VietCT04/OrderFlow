package com.vietct.OrderFlow.outbox.service;

import com.vietct.OrderFlow.outbox.domain.OutboxEvent;
import com.vietct.OrderFlow.outbox.repository.OutboxEventRepository;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@Profile("dev")
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishUnprocessedEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop100ByProcessedAtIsNullOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return;
        }

        log.info("OutboxPublisher: found {} unprocessed events", events.size());

        for (OutboxEvent event : events) {
            try {
                String topic = resolveTopic(event);
                String key = event.getAggregateId().toString();
                String payload = event.getPayload();

                var future = kafkaTemplate.send(topic, key, payload);

                RecordMetadata metadata = future.get().getRecordMetadata();

                event.setProcessedAt(Instant.now());

                log.info("Published outbox event id={} to topic={} partition={} offset={}",
                        event.getId(), topic, metadata.partition(), metadata.offset());

            } catch (Exception ex) {
                log.error("Failed to publish outbox event id={}: {}", event.getId(), ex.getMessage());
            }
        }

    }

    private String resolveTopic(OutboxEvent event) {
        if ("PAYMENT".equals(event.getAggregateType())) {
            return PAYMENT_EVENTS_TOPIC;
        }
        return "orderflow.outbox.default";
    }
}
