package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Long-running scenario with many iterations and configurable sleep between steps.
 * Designed for testing: terminate signal, concurrent execution, and engine stats monitoring.
 *
 * <p>Scenario code: {@code LONG_RUNNING}
 *
 * <p>Submit example (inputData = total iterations, optionally with sleep ms):
 * <pre>
 * POST /api/scenarios
 * { "scenarioCode": "LONG_RUNNING", "inputData": "20" }
 * </pre>
 *
 * <p>Send TERMINATE to stop early:
 * <pre>
 * POST /api/scenarios/{id}/signal
 * { "signal": "TERMINATE" }
 * </pre>
 */
@Slf4j
@Component("LONG_RUNNING")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LongRunningScenario extends AbstractScenario {

    private static final int DEFAULT_ITERATIONS = 20;
    private static final long STEP_SLEEP_MS = 500;

    private int totalIterations = DEFAULT_ITERATIONS;
    private int iteration = 0;

    public LongRunningScenario() {
        super("LONG_RUNNING");
    }

    @Override
    public void setInputData(String inputData) {
        super.setInputData(inputData);
        try {
            if (inputData != null && !inputData.isBlank()) {
                totalIterations = Integer.parseInt(inputData.trim());
            }
        } catch (NumberFormatException ignored) {}
        increaseTotal(totalIterations);
        log.info("Long running scenario configured: {} iterations, {}ms/step", totalIterations, STEP_SLEEP_MS);
    }

    @Override
    protected boolean doExecute() {
        if (isTerminateSignal()) {
            log.info("TERMINATE received at iteration {}/{}", iteration, totalIterations);
            setResultNotOK("Terminated early", "completed " + iteration + " of " + totalIterations + " iterations");
            return false;
        }

        iteration++;
        setScenarioStep("iteration-" + iteration);
        setCurrentItem("step-" + iteration);
        log.info("Long running iteration {}/{}", iteration, totalIterations);

        try {
            Thread.sleep(STEP_SLEEP_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        increaseSuccess();
        return iteration < totalIterations;
    }

    @Override
    protected void extendStatistics(Map<String, Object> statistics) {
        statistics.put("iteration", iteration);
        statistics.put("totalIterations", totalIterations);
        statistics.put("progressPct", totalIterations > 0 ? (iteration * 100 / totalIterations) : 0);
    }
}
