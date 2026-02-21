package cz.ivosahlik.robotization.scenarioengine.domain;

import java.util.Map;

/**
 * Immutable definition of a pre/post processing step attached to a scenario execution.
 * Replaces the old mutable {@code ScenarioProcessingStep}.
 */
public record ProcessingStepDef(
        String module,
        StepType stepType,
        String stepCode,
        int priority,
        Map<String, Object> parameters
) {

    public enum StepType {
        PRE_PROCESSING,
        POST_PROCESSING,
        PRE_ITEM,
        POST_ITEM,
        POST_RESUME
    }
}
