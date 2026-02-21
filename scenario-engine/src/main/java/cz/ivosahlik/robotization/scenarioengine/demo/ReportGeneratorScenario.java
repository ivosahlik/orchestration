package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Demo report generation scenario – uses the REPORT thread code bucket to simulate
 * limited-concurrency report tasks. Single-shot, simulates data collection and PDF export.
 *
 * <p>Scenario code: {@code REPORT_GENERATOR}
 * <p>Thread code bucket: configured externally via SubmitRequest threadCode field
 *
 * <p>Submit example:
 * <pre>
 * POST /api/scenarios
 * { "scenarioCode": "REPORT_GENERATOR", "threadCode": "REPORT", "inputData": "monthly-sales" }
 * </pre>
 */
@Slf4j
@Component("REPORT_GENERATOR")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportGeneratorScenario extends AbstractScenario {

    private String reportType;
    private String generatedReportPath;

    public ReportGeneratorScenario() {
        super("REPORT_GENERATOR");
    }

    @Override
    public void setInputData(String inputData) {
        super.setInputData(inputData);
        reportType = (inputData != null && !inputData.isBlank()) ? inputData.trim() : "default-report";
    }

    @Override
    protected boolean doExecute() {
        setScenarioStep("collecting-data");
        log.info("Collecting data for report: {}", reportType);
        simulateWork(200);

        setScenarioStep("aggregating");
        log.info("Aggregating data...");
        simulateWork(300);

        setScenarioStep("generating-pdf");
        log.info("Generating PDF...");
        simulateWork(500);

        generatedReportPath = "/reports/%s-%s.pdf".formatted(reportType, Instant.now().getEpochSecond());
        log.info("Report generated: {}", generatedReportPath);

        increaseSuccess();
        setResultOK("Report generated: " + generatedReportPath);
        return false;
    }

    @Override
    protected void extendStatistics(Map<String, Object> statistics) {
        statistics.put("reportType", reportType);
        statistics.put("reportPath", generatedReportPath);
    }

    private void simulateWork(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
