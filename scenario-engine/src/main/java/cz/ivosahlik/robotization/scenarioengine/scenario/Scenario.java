package cz.ivosahlik.robotization.scenarioengine.scenario;

import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioResult;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioState;

import java.util.Map;

/**
 * Contract every business scenario must fulfil.
 *
 * <p>Execution model:
 * <ul>
 *   <li>{@link #execute()} is called repeatedly until it returns {@code false}
 *       (single-item scenarios always return {@code false}).</li>
 *   <li>Context can be serialised between executions to support suspend/resume.</li>
 * </ul>
 */
public interface Scenario {

    /** Unique scenario type code (e.g. {@code "MY_SCENARIO"}). */
    String getCode();

    /** Called once before the first {@link #execute()} with the raw input payload. */
    void setInputData(String inputData);

    /**
     * Perform one unit of work.
     *
     * @return {@code true} if more items remain, {@code false} when done.
     */
    boolean execute();

    /** Send a control signal to a running scenario (e.g. {@code "terminate"}). */
    void processSignal(String signal);

    ScenarioState getState();
    ScenarioResult getResult();

    /** Returns all statistics as a string-keyed map for reporting. */
    Map<String, Object> getStatistics();

    /** Returns a serialisable context object used for suspend/resume. */
    Object getContext();

    /** Restores context after a resume. */
    void setContext(Object context);

    String getId();
    String getRunId();
    void setRunId(String runId);
}
