package cz.ivosahlik.robotization.scenarioengine.api.dto;

import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioExecution;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioState;

import java.time.Instant;
import java.util.Map;

/**
 * Read-only projection returned by the status endpoint.
 */
public record ScenarioStatus(
        String correlationId,
        String scenarioCode,
        String threadCode,
        ScenarioState state,
        Instant submitDate,
        Instant runStart,
        Instant runEnd,
        String username,
        Map<String, Object> statistics
) {
    public static ScenarioStatus from(ScenarioExecution exec) {
        return new ScenarioStatus(
                exec.getCorrelationId(),
                exec.getScenarioCode(),
                exec.getThreadCode(),
                exec.getState(),
                exec.getSubmitDate(),
                exec.getRunStart(),
                exec.getRunEnd(),
                exec.getUsername(),
                exec.getLatestStatistics()
        );
    }
}
