package cz.ivosahlik.robotization.scenarioengine.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Event payload produced by the Offertool system and consumed via Kafka.
 *
 * <p>Unknown fields are ignored to remain forward-compatible with producer changes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OffertoolOfferEvent(
        @JsonProperty("offerId")            long offerId,
        @JsonProperty("variantId")          long variantId,
        @JsonProperty("testAppLogin")        String testAppLogin,
        @JsonProperty("email")              String email,
        @JsonProperty("ca")                 String ca,
        @JsonProperty("ico")                String ico,
        @JsonProperty("contractId")         String contractId,
        @JsonProperty("platform")           String platform,
        @JsonProperty("type")               String type,
        @JsonProperty("contractDuration")   int contractDuration,
        @JsonProperty("contractStartDate")  String contractStartDate,
        @JsonProperty("contractEndDate")    String contractEndDate,
        @JsonProperty("variantState")       String variantState,
        @JsonProperty("clientCommitment")   PriceAmount clientCommitment
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceAmount(
            @JsonProperty("priceWithVat")    BigDecimal priceWithVat,
            @JsonProperty("priceWithoutVat") BigDecimal priceWithoutVat
    ) {}
}
