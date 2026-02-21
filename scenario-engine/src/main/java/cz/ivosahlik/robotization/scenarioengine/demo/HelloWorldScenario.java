package cz.ivosahlik.robotization.scenarioengine.demo;

import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Simplest possible demo scenario – single-shot, always succeeds.
 *
 * <p>Scenario code: {@code HELLO_WORLD}
 *
 * <p>Submit example:
 * <pre>
 * POST /api/scenarios
 * { "scenarioCode": "HELLO_WORLD", "inputData": "World" }
 * </pre>
 */
@Slf4j
@Component("HELLO_WORLD")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HelloWorldScenario extends AbstractScenario {

    public HelloWorldScenario() {
        super("HELLO_WORLD");
    }

    @Override
    protected boolean doExecute() {
        String name = getInputData() != null ? getInputData() : "World";
        log.info("Hello, {}!", name);
        setResultOK("Greeted: " + name);
        increaseSuccess();
        return false; // single-shot
    }
}
