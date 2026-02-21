package cz.ivosahlik.robotization.scenarioengine.execution;

import cz.ivosahlik.robotization.scenarioengine.domain.ProcessingStepDef;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioExecution;
import cz.ivosahlik.robotization.scenarioengine.scenario.Scenario;

import java.util.Map;

/**
 * SPI for pluggable pre/post-processing steps attached to scenario executions.
 * Beans are looked up by the {@code module} name in {@link ProcessingStepDef}.
 */
public interface StepProcessor {

    void execute(
            ProcessingStepDef.StepType type,
            ScenarioExecution execution,
            Scenario scenario,
            String stepCode,
            Map<String, Object> parameters
    );
}
