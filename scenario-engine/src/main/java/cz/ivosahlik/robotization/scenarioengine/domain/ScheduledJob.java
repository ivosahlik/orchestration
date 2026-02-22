package cz.ivosahlik.robotization.scenarioengine.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document representing a cron-based scheduled scenario trigger.
 * Replaces the old JPA scheduling tables.
 */
@Setter
@Getter
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

}
