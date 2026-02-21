package cz.ivosahlik.robotization.scenarioengine.execution;

import cz.ivosahlik.robotization.scenarioengine.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Creates prototype-scoped {@link Scenario} beans by scenario code.
 * Beans must be registered with a name matching their {@code code}.
 */
@Slf4j
@Service
public class ScenarioRegistry {

    private final ApplicationContext context;

    public ScenarioRegistry(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Creates a new scenario instance for the given code.
     *
     * @throws IllegalArgumentException if no bean with this name/code exists.
     */
    public Scenario create(String scenarioCode) {
        try {
            Scenario scenario = context.getBean(scenarioCode, Scenario.class);
            log.info("Created scenario instance for code '{}'", scenarioCode);
            return scenario;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "No scenario bean found for code '%s'. Register a @Component @Scope(\"prototype\") with that name."
                            .formatted(scenarioCode), e);
        }
    }

    public boolean exists(String scenarioCode) {
        return context.containsBean(scenarioCode);
    }
}
