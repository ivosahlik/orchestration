package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Batch scenario that processes N items one at a time, simulating item-level work.
 * Demonstrates the multi-step execution loop (execute() returns true until done).
 *
 * <p>Scenario code: {@code BATCH_PROCESS}
 *
 * <p>Submit example (inputData = number of items to process):
 * <pre>
 * POST /api/scenarios
 * { "scenarioCode": "BATCH_PROCESS", "inputData": "10" }
 * </pre>
 */
@Slf4j
@Component("BATCH_PROCESS")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BatchProcessingScenario extends AbstractScenario {

    private List<String> items;
    private int currentIndex = 0;

    public BatchProcessingScenario() {
        super("BATCH_PROCESS");
    }

    @Override
    public void setInputData(String inputData) {
        super.setInputData(inputData);
        int count = 5; // default
        try {
            if (inputData != null && !inputData.isBlank()) {
                count = Integer.parseInt(inputData.trim());
            }
        } catch (NumberFormatException ignored) {}

        items = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            items.add("item-%03d".formatted(i));
        }
        increaseTotal(items.size());
        log.info("Batch prepared: {} items to process", items.size());
    }

    @Override
    protected boolean doExecute() {
        if (isTerminateSignal()) {
            log.info("Terminate signal received – stopping batch at index {}", currentIndex);
            setResultNotOK("Terminated by signal", "processed " + currentIndex + " of " + items.size());
            return false;
        }

        String item = items.get(currentIndex);
        setCurrentItem(item);
        setScenarioStep("processing-" + currentIndex);

        log.info("Processing [{}] {}/{}", item, currentIndex + 1, items.size());

        // Simulate some work
        simulateWork(50);

        // 90% success rate simulation
        if (currentIndex % 10 == 7) {
            increaseFail();
            log.warn("Item {} failed (simulated)", item);
        } else {
            increaseSuccess();
        }

        currentIndex++;
        boolean hasMore = currentIndex < items.size();
        if (!hasMore) {
            resetCurrentItem();
            log.info("Batch complete: total={}, success={}, fail={}", getTotal(), getSuccess(), getFail());
        }
        return hasMore;
    }

    @Override
    protected void extendStatistics(Map<String, Object> statistics) {
        statistics.put("totalItems", items != null ? items.size() : 0);
        statistics.put("processedItems", currentIndex);
    }

    private void simulateWork(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
