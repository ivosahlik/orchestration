package cz.ivosahlik.robotization.scenarioengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Execution engine tuning parameters.
 */
@ConfigurationProperties("engine.executor")
public record EngineProperties(
        long checkIntervalMs,
        boolean recoverOnStart
) {
    public EngineProperties {
        if (checkIntervalMs <= 0) checkIntervalMs = 1_000L;
    }
}
