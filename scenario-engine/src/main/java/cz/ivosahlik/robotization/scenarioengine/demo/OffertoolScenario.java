package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Scenario that processes an Offertool offer received from the Kafka producer.
 *
 * <p>Scenario code: {@code offertool}
 *
 * <p>Input: raw JSON string of {@link OffertoolOfferEvent}.
 * Submitted automatically by {@link OffertoolKafkaConsumer} on each Kafka message.
 */
@Slf4j
@Component("OFFERTOOL_BATCH_PROCESS")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OffertoolScenario extends AbstractScenario {

    // ObjectMapper is thread-safe for deserialization – no need to inject into prototype
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OffertoolOfferEvent offer;

    public OffertoolScenario() {
        super("OFFERTOOL_BATCH_PROCESS");
    }

    @Override
    public void setInputData(String inputData) {
        super.setInputData(inputData);
        try {
            offer = MAPPER.readValue(inputData, OffertoolOfferEvent.class);
            log.info("Offer loaded: offerId={}, variantId={}, ca={}, contractId={}, variantState={}",
                    offer.offerId(), offer.variantId(), offer.ca(), offer.contractId(), offer.variantState());
        } catch (Exception e) {
            log.error("Failed to parse offer input data", e);
        }
    }

    @Override
    protected boolean doExecute() {
        if (offer == null) {
            setResultFailedInit("Failed to parse offer data", "Invalid JSON input");
            return false;
        }

        setScenarioStep("processing-offer");
        log.info("Processing offer: offerId={}, variantId={}, platform={}, type={}, contractDuration={}m",
                offer.offerId(), offer.variantId(), offer.platform(), offer.type(), offer.contractDuration());

        // TODO: implement business processing logic for the offer

        setResultOK("Offer processed: offerId=%d variantId=%d".formatted(offer.offerId(), offer.variantId()));
//        setResultFailedInit("Failed to parse offer data", "Invalid JSON input");
        increaseSuccess();
        return false; // single-shot
    }

    @Override
    protected void extendStatistics(Map<String, Object> statistics) {
        if (offer != null) {
            statistics.put("offerId",      offer.offerId());
            statistics.put("variantId",    offer.variantId());
            statistics.put("platform",     offer.platform());
            statistics.put("type",         offer.type());
            statistics.put("variantState", offer.variantState());
        }
    }
}
