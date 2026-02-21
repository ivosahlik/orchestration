package cz.ivosahlik.robotization.scenarioengine.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Primary MongoDB document representing one scenario execution from submission to completion.
 * Replaces the old in-memory {@code ScenarioExecutionData} + file-based storage.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>Created with state {@code WAITING} when a client submits a scenario.</li>
 *   <li>Moved to {@code PROCESSING} when the dispatcher picks it up.</li>
 *   <li>Moved to {@code COMPLETED}, {@code FAILED} or {@code SUSPENDED} when done.</li>
 * </ol>
 */
@Getter
@Document(collection = "scenario_executions")
@CompoundIndex(def = "{'state': 1, 'submitDate': 1}", name = "state_submitDate")
@CompoundIndex(def = "{'threadCode': 1, 'state': 1}", name = "threadCode_state")
public class ScenarioExecution {

    @Id
    private String correlationId;

    @Indexed
    private String scenarioCode;

    private String threadCode;

    @Setter
    @Indexed
    private ScenarioState state;

    private String inputData;

    /** Serialised scenario context saved for suspend/resume. */
    @Setter
    private Object scenarioContext;

    @Setter
    private Map<String, Object> latestStatistics;

    private List<String> screenshots;

    private List<ProcessingStepDef> processingSteps;

    private Instant submitDate;
    @Setter
    private Instant runStart;
    @Setter
    private Instant runEnd;

    private String username;
    private String submitHostname;
    @Setter
    private String processingHostname;
    private String clientVersionId;
    @Setter
    private String serverVersionId;

    // ── Constructors ─────────────────────────────────────────────────────────

    public ScenarioExecution() {}

    public ScenarioExecution(
            String correlationId,
            String scenarioCode,
            String threadCode,
            String inputData,
            List<ProcessingStepDef> processingSteps,
            Instant submitDate,
            String submitHostname,
            String clientVersionId,
            String username
    ) {
        this.correlationId = correlationId;
        this.scenarioCode = scenarioCode;
        this.threadCode = threadCode;
        this.inputData = inputData;
        this.processingSteps = processingSteps;
        this.submitDate = submitDate != null ? submitDate : Instant.now();
        this.submitHostname = submitHostname;
        this.clientVersionId = clientVersionId;
        this.username = username;
        this.state = ScenarioState.WAITING;
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public void addScreenshot(String path) {
        if (screenshots == null) screenshots = new java.util.ArrayList<>();
        screenshots.add(path);
    }

    @Override
    public String toString() {
        return "ScenarioExecution{correlationId='%s', scenarioCode='%s', state=%s}"
                .formatted(correlationId, scenarioCode, state);
    }
}
