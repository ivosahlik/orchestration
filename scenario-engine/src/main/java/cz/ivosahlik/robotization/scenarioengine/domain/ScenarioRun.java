package cz.ivosahlik.robotization.scenarioengine.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Immutable reporting record written once a scenario reaches COMPLETED or FAILED.
 * Replaces the old JPA {@code ScenarioRun} entity.
 */
@Document(collection = "scenario_runs")
@CompoundIndex(def = "{'scenarioCode': 1, 'processingStart': -1}", name = "scenarioCode_start")
public class ScenarioRun {

    @Id
    private String id;

    @Indexed(unique = true)
    private String correlationId;

    @Indexed
    private String scenarioCode;

    private String result;
    private long totalCount;
    private long successCount;
    private long failCount;

    private Instant submitDate;
    private Instant processingStart;
    private Instant processingEnd;

    private String username;
    private String submitHostname;
    private String processingHostname;
    private String clientVersionId;
    private String serverVersionId;

    /** Key-value pairs from scenario statistics (all non-standard keys). */
    private Map<String, String> details;

    private List<String> screenshotPaths;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getScenarioCode() { return scenarioCode; }
    public void setScenarioCode(String scenarioCode) { this.scenarioCode = scenarioCode; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public long getSuccessCount() { return successCount; }
    public void setSuccessCount(long successCount) { this.successCount = successCount; }

    public long getFailCount() { return failCount; }
    public void setFailCount(long failCount) { this.failCount = failCount; }

    public Instant getSubmitDate() { return submitDate; }
    public void setSubmitDate(Instant submitDate) { this.submitDate = submitDate; }

    public Instant getProcessingStart() { return processingStart; }
    public void setProcessingStart(Instant processingStart) { this.processingStart = processingStart; }

    public Instant getProcessingEnd() { return processingEnd; }
    public void setProcessingEnd(Instant processingEnd) { this.processingEnd = processingEnd; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSubmitHostname() { return submitHostname; }
    public void setSubmitHostname(String submitHostname) { this.submitHostname = submitHostname; }

    public String getProcessingHostname() { return processingHostname; }
    public void setProcessingHostname(String processingHostname) { this.processingHostname = processingHostname; }

    public String getClientVersionId() { return clientVersionId; }
    public void setClientVersionId(String clientVersionId) { this.clientVersionId = clientVersionId; }

    public String getServerVersionId() { return serverVersionId; }
    public void setServerVersionId(String serverVersionId) { this.serverVersionId = serverVersionId; }

    public Map<String, String> getDetails() { return details; }
    public void setDetails(Map<String, String> details) { this.details = details; }

    public List<String> getScreenshotPaths() { return screenshotPaths; }
    public void setScreenshotPaths(List<String> screenshotPaths) { this.screenshotPaths = screenshotPaths; }
}
