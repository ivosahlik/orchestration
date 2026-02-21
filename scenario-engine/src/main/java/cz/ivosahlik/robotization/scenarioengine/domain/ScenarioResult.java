package cz.ivosahlik.robotization.scenarioengine.domain;

/**
 * Business result of a completed scenario run.
 */
public enum ScenarioResult {
    /** All items processed successfully. */
    OK,
    /** Some items succeeded, some failed. */
    PARTIAL,
    /** All items failed or scenario could not process anything. */
    NOT_OK,
    /** Scenario failed during initialisation (pre-processing step). */
    FAILED_INIT,
    /** Result not yet determined. */
    UNKNOWN;

    /** Derive result from counters. */
    public static ScenarioResult from(int total, int success, int fail) {
        if (total == 0 && success == 0 && fail == 0) return OK;
        if (success > 0 && fail == 0) return OK;
        if (success > 0) return PARTIAL;
        return NOT_OK;
    }
}
