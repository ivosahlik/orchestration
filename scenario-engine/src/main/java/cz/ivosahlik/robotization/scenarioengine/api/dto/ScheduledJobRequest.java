package cz.ivosahlik.robotization.scenarioengine.api.dto;

import cz.ivosahlik.robotization.scenarioengine.domain.ProcessingStepDef;
import cz.ivosahlik.robotization.scenarioengine.domain.ScheduledJob;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request body for creating or updating a scheduled job.
 */
public record ScheduledJobRequest(

        @NotBlank String name,
        @NotBlank String scenarioCode,
        String threadCode,

        /**
         * Spring/Unix cron expression (6 fields: second minute hour day month weekday).
         * Example: {@code "0 0 8 * * MON-FRI"}
         */
        @NotBlank String cronExpression,

        boolean enabled,

        String inputDataTemplate,

        /**
         * Optional URL to fetch input data from on each cron trigger.
         * When set, the HTTP GET response body is used as input data
         * and {@code inputDataTemplate} is ignored.
         */
        String urlConsumer,

        /** Optional working-hours guard, e.g. {@code "MON-FRI(08:00-17:00)"}. */
        String workingHoursRule,

        List<ProcessingStepDef> processingSteps
) {
    /**
     * Convert request to domain object for persistence.
     */
    public ScheduledJob toScheduledJob() {
        ScheduledJob job = new ScheduledJob();
        job.setName(name);
        job.setScenarioCode(scenarioCode);
        job.setThreadCode(threadCode != null ? threadCode : scenarioCode);
        job.setCronExpression(cronExpression);
        job.setEnabled(enabled);
        job.setInputDataTemplate(inputDataTemplate);
        job.setUrlConsumer(urlConsumer);
        job.setWorkingHoursRule(workingHoursRule);
        job.setProcessingSteps(processingSteps);
        return job;
    }
}
