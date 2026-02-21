package cz.ivosahlik.robotization.scenarioengine.api.dto;

import cz.ivosahlik.robotization.scenarioengine.domain.ProcessingStepDef;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST request body for submitting a new scenario execution.
 */
public record SubmitRequest(

        /** Optional – server generates one if absent. */
        String correlationId,

        @NotBlank
        String scenarioCode,

        /** Process/thread quota bucket. Defaults to scenarioCode if absent. */
        String threadCode,

        String inputData,

        List<ProcessingStepDef> processingSteps,

        Instant submitDate,

        String submitHostname,

        String clientVersionId,

        String username
) {
    /** Normalised correlation ID – never null. */
    public String resolvedCorrelationId() {
        return (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString();
    }

    /** Normalised thread code – falls back to scenarioCode. */
    public String resolvedThreadCode() {
        return (threadCode != null && !threadCode.isBlank()) ? threadCode : scenarioCode;
    }
}
