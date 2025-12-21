package com.vietct.OrderFlow.outbox.service;

import com.vietct.OrderFlow.common.lock.DistributedLockManager;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Profile("kafka")
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";
    private static final String DEFAULT_TOPIC = "orderflow.outbox.default";

    private static final String OUTBOX_LOCK_NAME = "outbox:publisher";
    private static final Duration OUTBOX_LOCK_TTL = Duration.ofSeconds(5);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DistributedLockManager lockManager;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           DistributedLockManager lockManager) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.lockManager = lockManager;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishUnprocessedEvents() {
        Optional<String> lockToken = lockManager.tryAcquireLock(OUTBOX_LOCK_NAME, OUTBOX_LOCK_TTL);
        if (lockToken.isEmpty()) {
            return;
        }

        String token = lockToken.get();
        try {
            doPublishUnprocessedEvents();
        } finally {
            lockManager.releaseLock(OUTBOX_LOCK_NAME, token);
        }
    }

    protected void doPublishUnprocessedEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop100ByProcessedAtIsNullOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return;
        }

        log.info("OutboxPublisher: found {} unprocessed events", events.size());

        for (OutboxEvent event : events) {
            try {
                publishSingleEvent(event);
            } catch (Exception ex) {
                log.error(
                        "Failed to publish outbox event id={} aggregateType={} eventType={}: {}",
                        event.getId(), event.getAggregateType(), event.getEventType(), ex.getMessage(), ex
                );
            }
        }
    }

    @Transactional
    protected void publishSingleEvent(OutboxEvent event) throws Exception {
        String topic = resolveTopic(event);
        String key = event.getAggregateId().toString();
        String payload = event.getPayload();

        var result = kafkaTemplate.send(topic, key, payload).get();
        RecordMetadata metadata = result.getRecordMetadata();

        event.setProcessedAt(Instant.now());
        outboxEventRepository.save(event);

        log.info(
                "Published outbox event id={} to topic={} partition={} offset={}",
                event.getId(), topic, metadata.partition(), metadata.offset()
        );
    }

    private String resolveTopic(OutboxEvent event) {
        if ("PAYMENT".equalsIgnoreCase(event.getAggregateType())) {
            return PAYMENT_EVENTS_TOPIC;
        }
        return DEFAULT_TOPIC;
    }
}
