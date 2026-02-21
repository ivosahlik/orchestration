package cz.ivosahlik.robotization.scenarioengine.domain;

/**
 * Signals that can be sent to a running scenario.
 */
public enum ScenarioSignal {
    TERMINATE("terminate"),
    CLEAR_HEALTH_REGISTER("clearHealthRegister");

    private final String code;

    ScenarioSignal(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ScenarioSignal fromCode(String code) {
        for (ScenarioSignal s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        throw new IllegalArgumentException("Unknown signal: " + code);
    }
}
