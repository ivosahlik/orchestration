package cz.ivosahlik.robotization.scenarioengine.domain;

/**
 * Lifecycle state of a scenario execution document in MongoDB.
 * Transitions: WAITING → PROCESSING → COMPLETED | FAILED | SUSPENDED
 *              SUSPENDED → WAITING  (via unsuspend)
 */
public enum ScenarioState {
    WAITING,
    PROCESSING,
    COMPLETED,
    SUSPENDED,
    FAILED
}
