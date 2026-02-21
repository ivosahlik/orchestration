package cz.ivosahlik.robotization.scenarioengine.scheduling;

import cz.ivosahlik.robotization.scenarioengine.domain.ScheduledJob;
import cz.ivosahlik.robotization.scenarioengine.execution.ScenarioExecutionService;
import cz.ivosahlik.robotization.scenarioengine.repository.ScheduledJobRepository;
import cz.ivosahlik.robotization.scenarioengine.workinghours.WorkingHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Evaluates all enabled {@link ScheduledJob} documents and submits scenario executions
 * when the cron expression fires and working-hours rules allow it.
 *
 * <p><strong>No Quartz, no JPA, no platform threads</strong> – Spring's {@code @Scheduled}
 * with virtual threads handles the tick; MongoDB stores all job state.
 */
@Slf4j
@Service
public class SchedulingService {

    private final ScheduledJobRepository     jobRepository;
    private final ScenarioExecutionService   executionService;
    private final WorkingHoursService        workingHoursService;
    private final RestClient                 restClient = RestClient.create();

    public SchedulingService(
            ScheduledJobRepository jobRepository,
            ScenarioExecutionService executionService,
            WorkingHoursService workingHoursService
    ) {
        this.jobRepository       = jobRepository;
        this.executionService    = executionService;
        this.workingHoursService = workingHoursService;
    }

    // ── Scheduler tick ────────────────────────────────────────────────────────

    /**
     * Runs every minute. Virtual thread from Spring Boot 4 thread executor.
     * For sub-minute jobs, lower the fixedRate or use a custom cron.
     */
//    @Scheduled(fixedRate = 60_000)
    @Scheduled(fixedRate = 100)
    public void tick() {
        List<ScheduledJob> jobs = jobRepository.findByEnabled(true);
        if (jobs.isEmpty()) return;
        log.debug("Scheduling tick – evaluating {} job(s)", jobs.size());
        jobs.forEach(this::evaluate);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public ScheduledJob create(ScheduledJob job) {
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        return jobRepository.save(job);
    }

    public Optional<ScheduledJob> update(String id, ScheduledJob patch) {
        return jobRepository.findById(id).map(existing -> {
            existing.setName(patch.getName());
            existing.setScenarioCode(patch.getScenarioCode());
            existing.setThreadCode(patch.getThreadCode());
            existing.setCronExpression(patch.getCronExpression());
            existing.setEnabled(patch.isEnabled());
            existing.setInputDataTemplate(patch.getInputDataTemplate());
            existing.setUrlConsumer(patch.getUrlConsumer());
            existing.setWorkingHoursRule(patch.getWorkingHoursRule());
            existing.setProcessingSteps(patch.getProcessingSteps());
            existing.setUpdatedAt(Instant.now());
            return jobRepository.save(existing);
        });
    }

    public void delete(String id) {
        jobRepository.deleteById(id);
    }

    public List<ScheduledJob> listAll() {
        return jobRepository.findAll();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void evaluate(ScheduledJob job) {
        try {
            CronExpression cron = CronExpression.parse(job.getCronExpression());
            ZonedDateTime  now  = ZonedDateTime.now(ZoneId.systemDefault());
            ZonedDateTime  last = job.getLastTriggered() != null
                    ? job.getLastTriggered().atZone(ZoneId.systemDefault())
                    : now.minusYears(10);

            ZonedDateTime nextAfterLast = cron.next(last);
            if (nextAfterLast == null || nextAfterLast.isAfter(now)) return;

            // Working-hours guard
            if (!workingHoursService.isWithinWorkingHours(job.getWorkingHoursRule())) {
                log.debug("Job '{}' skipped – outside working hours.", job.getName());
                return;
            }

            log.info("Triggering job '{}'", job.getName());
            String correlationId = UUID.randomUUID().toString();
            String inputData = job.getUrlConsumer() != null && !job.getUrlConsumer().isBlank()
                    ? fetchFromUrl(job.getUrlConsumer(), job.getName())
                    : resolveInputData(job.getInputDataTemplate(), now);

            executionService.submit(
                    correlationId,
                    job.getScenarioCode(),
                    job.getThreadCode() != null ? job.getThreadCode() : job.getScenarioCode(),
                    inputData,
                    job.getProcessingSteps(),
                    now.toInstant(),
                    null, null,
                    "scheduler"
            );

            job.setLastTriggered(now.toInstant());
            jobRepository.save(job);
            log.info("Job '{}' triggered with correlationId={}", job.getName(), correlationId);

        } catch (Exception e) {
            log.error("Failed to evaluate job '{}'", job.getName(), e);
        }
    }

    private String resolveInputData(String template, ZonedDateTime now) {
        if (template == null) return null;
        return template.replace("{{now}}", now.toInstant().toString());
    }

    private String fetchFromUrl(String url, String jobName) {
        log.debug("Job '{}' – fetching input data from: {}", jobName, url);
        String body = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
        log.debug("Job '{}' – fetched {} chars from URL", jobName, body != null ? body.length() : 0);
        return body;
    }
}
