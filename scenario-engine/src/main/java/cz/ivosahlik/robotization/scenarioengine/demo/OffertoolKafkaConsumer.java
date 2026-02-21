package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.execution.ScenarioExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka consumer for Offertool offer events.
 *
 * <p>Listens on {@code offertool.kafka.topic}, parses the payload into
 * {@link OffertoolOfferEvent}, and submits an {@code offertool} scenario for each message.
 * The engine dispatcher picks up the submitted execution and moves it to PROCESSING state.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OffertoolKafkaConsumer {

    private final ScenarioExecutionService executionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${offertool.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String payload = record.value();
        log.info("Received offertool message: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset());

        try {
            OffertoolOfferEvent event = objectMapper.readValue(payload, OffertoolOfferEvent.class);
            log.info("Offertool event parsed: offerId={}, variantId={}, variantState={}, login={}",
                    event.offerId(), event.variantId(), event.variantState(), event.testAppLogin());

            String correlationId = UUID.randomUUID().toString();

            executionService.submit(
                    correlationId,
                    "offertool",
                    "DEFAULT",
                    payload,
                    null,
                    Instant.now(),
                    "kafka-offertool",
                    null,
                    event.testAppLogin()
            );

            log.info("Offertool scenario submitted → PROCESSING: correlationId={}, offerId={}, variantId={}",
                    correlationId, event.offerId(), event.variantId());

        } catch (Exception e) {
            log.error("Failed to process offertool Kafka message at offset={}: {}",
                    record.offset(), payload, e);
        }
    }
}
