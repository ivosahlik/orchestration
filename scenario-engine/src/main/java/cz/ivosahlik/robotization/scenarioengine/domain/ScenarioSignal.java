package cz.ivosahlik.robotization.scenarioengine.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Signals that can be sent to a running scenario.
 */
@AllArgsConstructor
@Getter
public enum ScenarioSignal {
    TERMINATE("terminate"),
    CLEAR_HEALTH_REGISTER("clearHealthRegister");

    private final String code;

    public static ScenarioSignal fromCode(String code) {
        for (ScenarioSignal s : values()) {
            if (s.code.equalsIgnoreCase(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown signal: " + code);
    }
}
