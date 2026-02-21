package cz.ivosahlik.robotization.scenarioengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Thread-pool limits expressed as an immutable record.
 * Replaces the old mutable {@code SharedThreadPoolConfiguration}.
 *
 * Example YAML:
 * <pre>
 * engine:
 *   concurrency:
 *     max-total: 50
 *     per-process:
 *       MY_PROCESS:
 *         max: 10
 *         min: 2
 * </pre>
 */
@ConfigurationProperties("engine.concurrency")
public record ConcurrencyProperties(
        int maxTotal,
        Map<String, ProcessConfig> perProcess
) {

    /** Per-process concurrency limits. */
    public record ProcessConfig(int max, int min) {
        public ProcessConfig {
            if (min < 0) throw new IllegalArgumentException("min must be >= 0, got " + min);
            if (max < min) throw new IllegalArgumentException("max must be >= min, got max=" + max + " min=" + min);
        }
    }

    /** Max concurrent scenarios for a given process code; 0 means no per-process limit. */
    public int maxForProcess(String code) {
        if (perProcess == null) return 0;
        ProcessConfig cfg = perProcess.get(code);
        return cfg != null ? cfg.max() : 0;
    }

    /** Min reserved slots for a given process code. */
    public int minForProcess(String code) {
        if (perProcess == null) return 0;
        ProcessConfig cfg = perProcess.get(code);
        return cfg != null ? cfg.min() : 0;
    }
}
