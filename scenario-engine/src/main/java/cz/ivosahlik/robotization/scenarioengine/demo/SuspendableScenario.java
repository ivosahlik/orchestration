package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Demo scenario that can be suspended mid-execution and later resumed.
 * Uses {@link #getContext()}/{@link #setContext(Object)} to persist progress across suspend/resume cycles.
 *
 * <p>Scenario code: {@code SUSPENDABLE}
 *
 * <p>Workflow:
 * <ol>
 *   <li>Submit the scenario – it starts processing pages 1..10</li>
 *   <li>POST /api/scenarios/{id}/suspend – pauses after current page</li>
 *   <li>POST /api/scenarios/{id}/unsuspend – resumes from saved page</li>
 * </ol>
 *
 * <p>Submit example:
 * <pre>
 * POST /api/scenarios
 * { "scenarioCode": "SUSPENDABLE", "inputData": "10" }
 * </pre>
 */
@Slf4j
@Component("SUSPENDABLE")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SuspendableScenario extends AbstractScenario {

    private int totalPages = 10;
    private int currentPage = 0;

    public SuspendableScenario() {
        super("SUSPENDABLE");
    }

    @Override
    public void setInputData(String inputData) {
        super.setInputData(inputData);
        try {
            if (inputData != null && !inputData.isBlank()) {
                totalPages = Integer.parseInt(inputData.trim());
            }
        } catch (NumberFormatException ignored) {}
        increaseTotal(totalPages);
        log.info("Suspendable scenario: {} pages to process", totalPages);
    }

    @Override
    public void setContext(Object context) {
        super.setContext(context);
        if (context instanceof Integer savedPage) {
            currentPage = savedPage;
            log.info("Resumed from page {}/{}", currentPage, totalPages);
        }
    }

    @Override
    public Object getContext() {
        return currentPage; // persist current page for resume
    }

    @Override
    protected boolean doExecute() {
        currentPage++;
        setScenarioStep("page-" + currentPage);
        setCurrentItem("page-" + currentPage);
        log.info("Processing page {}/{}", currentPage, totalPages);

        simulateWork(400); // simulate page processing

        increaseSuccess();

        boolean hasMore = currentPage < totalPages;
        if (!hasMore) {
            log.info("All {} pages processed successfully", totalPages);
            resetCurrentItem();
        }
        return hasMore;
    }

    @Override
    protected void extendStatistics(Map<String, Object> statistics) {
        statistics.put("currentPage", currentPage);
        statistics.put("totalPages", totalPages);
    }

    private void simulateWork(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
