package cz.ivosahlik.robotization.scenarioengine.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document representing a cron-based scheduled scenario trigger.
 * Replaces the old JPA scheduling tables.
 */
@Document(collection = "scheduled_jobs")
public class ScheduledJob {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String scenarioCode;
    private String threadCode;

    /** Standard Spring/Unix cron: {@code "0 0 8 * * MON-FRI"} */
    private String cronExpression;

    private boolean enabled;

    /** Mustache/template string; {@code {{now}}} replaced at trigger time. */
    private String inputDataTemplate;

    /**
     * Optional URL to fetch input data from on each cron trigger.
     * When set, the HTTP GET response body is used as {@code inputData}
     * and {@code inputDataTemplate} is ignored.
     */
    private String urlConsumer;

    /**
     * Working-hours rule controlling when the job fires.
     * Format: {@code "MON-FRI(08:00-17:00)"} – same as the legacy checker.
     */
    private String workingHoursRule;

    private List<ProcessingStepDef> processingSteps;

    private Instant lastTriggered;
    private Instant createdAt;
    private Instant updatedAt;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScenarioCode() { return scenarioCode; }
    public void setScenarioCode(String scenarioCode) { this.scenarioCode = scenarioCode; }

    public String getThreadCode() { return threadCode; }
    public void setThreadCode(String threadCode) { this.threadCode = threadCode; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getInputDataTemplate() { return inputDataTemplate; }
    public void setInputDataTemplate(String inputDataTemplate) { this.inputDataTemplate = inputDataTemplate; }

    public String getUrlConsumer() { return urlConsumer; }
    public void setUrlConsumer(String urlConsumer) { this.urlConsumer = urlConsumer; }

    public String getWorkingHoursRule() { return workingHoursRule; }
    public void setWorkingHoursRule(String workingHoursRule) { this.workingHoursRule = workingHoursRule; }

    public List<ProcessingStepDef> getProcessingSteps() { return processingSteps; }
    public void setProcessingSteps(List<ProcessingStepDef> processingSteps) { this.processingSteps = processingSteps; }

    public Instant getLastTriggered() { return lastTriggered; }
    public void setLastTriggered(Instant lastTriggered) { this.lastTriggered = lastTriggered; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
