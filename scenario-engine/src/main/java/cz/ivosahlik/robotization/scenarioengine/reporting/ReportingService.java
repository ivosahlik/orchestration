package cz.ivosahlik.robotization.scenarioengine.reporting;

import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioExecution;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioRun;
import cz.ivosahlik.robotization.scenarioengine.repository.ScenarioRunRepository;
import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import cz.ivosahlik.robotization.scenarioengine.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts a completed {@link ScenarioExecution} + its {@link Scenario} into
 * a {@link ScenarioRun} document and persists it to MongoDB.
 *
 * <p>Replaces the old JPA-based {@code ReportingServiceDB} and file-based {@code ReportingServiceFile}.
 */
@Slf4j
@Service
public class ReportingService {

    private final ScenarioRunRepository runRepository;

    public ReportingService(ScenarioRunRepository runRepository) {
        this.runRepository = runRepository;
    }

    /** Build and persist a {@link ScenarioRun} from completed execution data. */
    public ScenarioRun save(ScenarioExecution execution, Scenario scenario) {
        ScenarioRun run = buildRun(execution, scenario);
        try {
            runRepository.save(run);
            log.info("Saved ScenarioRun for correlationId={}", execution.getCorrelationId());
        } catch (Exception e) {
            log.error("Failed to persist ScenarioRun for correlationId={}", execution.getCorrelationId(), e);
        }
        return run;
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private ScenarioRun buildRun(ScenarioExecution exec, Scenario scenario) {
        ScenarioRun run = new ScenarioRun();
        run.setCorrelationId(exec.getCorrelationId());
        run.setScenarioCode(exec.getScenarioCode());
        run.setSubmitDate(exec.getSubmitDate());
        run.setProcessingStart(exec.getRunStart());
        run.setProcessingEnd(exec.getRunEnd());
        run.setUsername(exec.getUsername());
        run.setSubmitHostname(exec.getSubmitHostname());
        run.setProcessingHostname(exec.getProcessingHostname());
        run.setClientVersionId(exec.getClientVersionId());
        run.setServerVersionId(exec.getServerVersionId());
        run.setScreenshotPaths(exec.getScreenshots());

        extractStatistics(run, scenario.getStatistics());
        return run;
    }

    private void extractStatistics(ScenarioRun run, Map<String, Object> stats) {
        Map<String, String> details = new HashMap<>();
        stats.forEach((key, value) -> {
            switch (key) {
                case AbstractScenario.KEY_RESULT        -> run.setResult(String.valueOf(value));
                case AbstractScenario.KEY_TOTAL_COUNT   -> run.setTotalCount(toLong(value));
                case AbstractScenario.KEY_SUCCESS_COUNT -> run.setSuccessCount(toLong(value));
                case AbstractScenario.KEY_FAIL_COUNT    -> run.setFailCount(toLong(value));
                // All other keys go into the details map
                default -> { if (value != null) details.put(key, String.valueOf(value)); }
            }
        });
        if (!details.isEmpty()) run.setDetails(details);
    }

    private static long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (NumberFormatException e) { return 0L; }
    }
}
