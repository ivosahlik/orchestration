package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.api.dto.ScheduledJobRequest;
import cz.ivosahlik.robotization.scenarioengine.execution.ScenarioExecutionService;
import cz.ivosahlik.robotization.scenarioengine.repository.ScheduledJobRepository;
import cz.ivosahlik.robotization.scenarioengine.scheduling.SchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Demo data initializer – active only with {@code --spring.profiles.active=demo}.
 *
 * <p>On startup it:
 * <ol>
 *   <li>Submits one of each demo scenario type</li>
 *   <li>Creates a scheduled job for HELLO_WORLD (every minute)</li>
 * </ol>
 *
 * <p>Run with demo profile:
 * <pre>
 *   java -jar app.jar --spring.profiles.active=demo
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile("demo")
public class DemoDataInitializer implements ApplicationRunner {

    private final ScenarioExecutionService executionService;
    private final SchedulingService schedulingService;
    private final ScheduledJobRepository jobRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=============================================================");
        log.info(" DEMO MODE – seeding test scenarios and scheduled jobs");
        log.info("=============================================================");

        submitHelloWorld();
        submitBatchProcessing();
        submitLongRunning();
        submitReportGenerator();
        submitSuspendable();
        createScheduledJob();

        log.info("=============================================================");
        log.info(" Demo data seeded. Open http://localhost:8080/api/scenarios");
        log.info(" MongoDB UI:        http://localhost:8081");
        log.info(" Actuator health:   http://localhost:8080/actuator/health");
        log.info("=============================================================");
    }

    // ── Scenario submissions ──────────────────────────────────────────────────

    private void submitHelloWorld() {
        var exec = executionService.submit(
                UUID.randomUUID().toString(),
                "HELLO_WORLD",
                "HELLO_WORLD",
                "ivosahlik Demo",
                null,
                Instant.now(),
                "demo-host",
                "1.0.0-demo",
                "demo-user"
        );
        log.info("[DEMO] Submitted HELLO_WORLD -> correlationId={}", exec.getCorrelationId());
    }

    private void submitBatchProcessing() {
        var exec = executionService.submit(
                UUID.randomUUID().toString(),
                "BATCH_PROCESS",
                "BATCH_PROCESS",
                "8",               // 8 items
                null,
                Instant.now(),
                "demo-host",
                "1.0.0-demo",
                "demo-user"
        );
        log.info("[DEMO] Submitted BATCH_PROCESS (8 items) -> correlationId={}", exec.getCorrelationId());
    }

    private void submitLongRunning() {
        var exec = executionService.submit(
                UUID.randomUUID().toString(),
                "LONG_RUNNING",
                "LONG_RUNNING",
                "15",              // 15 iterations, 500ms each = ~7.5s total
                null,
                Instant.now(),
                "demo-host",
                "1.0.0-demo",
                "demo-user"
        );
        log.info("[DEMO] Submitted LONG_RUNNING (15 iterations) -> correlationId={}", exec.getCorrelationId());
    }

    private void submitReportGenerator() {
        var exec = executionService.submit(
                UUID.randomUUID().toString(),
                "REPORT_GENERATOR",
                "REPORT",          // uses REPORT concurrency bucket
                "monthly-sales",
                null,
                Instant.now(),
                "demo-host",
                "1.0.0-demo",
                "demo-user"
        );
        log.info("[DEMO] Submitted REPORT_GENERATOR -> correlationId={}", exec.getCorrelationId());
    }

    private void submitSuspendable() {
        var exec = executionService.submit(
                UUID.randomUUID().toString(),
                "SUSPENDABLE",
                "SUSPENDABLE",
                "5",               // 5 pages
                null,
                Instant.now(),
                "demo-host",
                "1.0.0-demo",
                "demo-user"
        );
        log.info("[DEMO] Submitted SUSPENDABLE (5 pages) -> correlationId={}", exec.getCorrelationId());
    }

    // ── Scheduled job ─────────────────────────────────────────────────────────

    private void createScheduledJob() {
        // Skip if already exists
        if (jobRepository.findByName("demo-hello-world-job").isPresent()) {
            log.info("[DEMO] Scheduled job 'demo-hello-world-job' already exists – skipping.");
            return;
        }

        var req = new ScheduledJobRequest(
                "demo-hello-world-job",
                "HELLO_WORLD",
                "HELLO_WORLD",
                "0 * * * * *",     // every minute (Spring 6-field cron: sec min hr day month weekday)
                true,
                "Scheduled at {{now}}",
                null,              // no urlConsumer
                null,              // no working-hours restriction
                null
        );
        var job = schedulingService.create(req.toScheduledJob());
        log.info("[DEMO] Created scheduled job '{}' (id={})", job.getName(), job.getId());
    }
}
