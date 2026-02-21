package cz.ivosahlik.robotization.scenarioengine.scenario;

import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioResult;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioSignal;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioState;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for all business scenarios.
 *
 * <h3>What changed vs. the old design</h3>
 * <ul>
 *   <li>Counter dual-path ({@code if persistentStatistics == null}) removed – always delegates
 *       to {@link ScenarioCounters}; inject a custom implementation for persistent counters.</li>
 *   <li>Signal handling uses a switch expression instead of string comparisons.</li>
 *   <li>{@code new Date()} replaced with modern {@link java.time} types elsewhere in the stack.</li>
 *   <li>State tracking simplified – only {@code PROCESSING} and {@code COMPLETED} needed here;
 *       the execution lifecycle is tracked by MongoDB in {@code ScenarioExecution}.</li>
 * </ul>
 *
 * <h3>Implementing a scenario</h3>
 * <pre>
 * {@code @Component @Scope("prototype")}
 * public class MyScenario extends AbstractScenario {
 *     public MyScenario() { super("MY_SCENARIO"); }
 *
 *     @Override
 *     protected boolean doExecute() {
 *         // business logic …
 *         increaseSuccess();
 *         return false; // single-shot scenario
 *     }
 * }
 * </pre>
 */
@Slf4j
public abstract class AbstractScenario implements Scenario {

    // ── Static keys for statistics map ───────────────────────────────────────
    public static final String KEY_RESULT        = "result";
    public static final String KEY_STATE         = "state";
    public static final String KEY_TOTAL_COUNT   = "totalCount";
    public static final String KEY_SUCCESS_COUNT = "successCount";
    public static final String KEY_FAIL_COUNT    = "failCount";
    public static final String KEY_MESSAGE       = "message";
    public static final String KEY_DETAIL        = "detail";
    public static final String KEY_CURRENT_ITEM  = "currentItem";
    public static final String KEY_TERMINATING   = "terminating";
    public static final String KEY_SCENARIO_STEP = "scenarioStep";

    // ── State ────────────────────────────────────────────────────────────────
    private final String code;
    private final ScenarioCounters counters;

    private String id;
    private String runId;
    private ScenarioState state   = ScenarioState.WAITING;
    private ScenarioResult result = ScenarioResult.UNKNOWN;

    private String inputData;
    private Object context;

    private String message;
    private String detail;
    private Object currentItem;
    private String scenarioStep;

    private volatile boolean terminateSignal;
    private volatile boolean healthRegisterSignal;

    // ── Constructors ─────────────────────────────────────────────────────────

    protected AbstractScenario(String code) {
        this(code, new InMemoryScenarioCounters());
    }

    protected AbstractScenario(String code, ScenarioCounters counters) {
        this.code     = code;
        this.counters = counters;
        this.id       = UUID.randomUUID().toString();
        log.info("Created scenario '{}' with id '{}'", code, id);
    }

    // ── Scenario interface ────────────────────────────────────────────────────

    @Override
    public final boolean execute() {
        state = ScenarioState.PROCESSING;
        try {
            return doExecute();
        } catch (Exception ex) {
            log.warn("Scenario [{}][{}] failed with exception", code, id, ex);
            setResultNotOK("Processing failed with exception", ex.getMessage());
            return false;
        } finally {
            if (state == ScenarioState.PROCESSING) {
                state = ScenarioState.COMPLETED;
                autoDetectResult();
            }
        }
    }

    /**
     * Template method – subclasses put their business logic here.
     *
     * @return {@code true} if more items remain; {@code false} when done.
     */
    protected boolean doExecute() {
        return false;
    }

    @Override
    public void processSignal(String signal) {
        log.info("Processing signal '{}' on scenario '{}'", signal, id);
        try {
            switch (ScenarioSignal.fromCode(signal)) {
                case TERMINATE              -> { terminateSignal = true;      log.info("Terminate signal set."); }
                case CLEAR_HEALTH_REGISTER  -> { healthRegisterSignal = true; log.info("Clear-health-register signal set."); }
            }
        } catch (IllegalArgumentException e) {
            log.warn("Signal '{}' not recognised.", signal);
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        var stats = new HashMap<String, Object>();
        stats.put(KEY_RESULT,        result);
        stats.put(KEY_STATE,         state);
        stats.put(KEY_TOTAL_COUNT,   counters.getTotal());
        stats.put(KEY_SUCCESS_COUNT, counters.getSuccess());
        stats.put(KEY_FAIL_COUNT,    counters.getFail());
        stats.put(KEY_MESSAGE,       message);
        stats.put(KEY_DETAIL,        detail);
        if (state != ScenarioState.COMPLETED) {
            stats.put(KEY_CURRENT_ITEM, currentItem);
            stats.put(KEY_TERMINATING,  terminateSignal);
        }
        if (scenarioStep != null) stats.put(KEY_SCENARIO_STEP, scenarioStep);
        extendStatistics(stats);
        return stats;
    }

    /** Override to add scenario-specific entries to the statistics map. */
    protected void extendStatistics(Map<String, Object> statistics) {}

    // ── Context (suspend / resume) ────────────────────────────────────────────

    @Override
    public Object getContext() { return context; }

    @Override
    public void setContext(Object context) {
        log.info("Context restored for scenario '{}'", id);
        this.context = context;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    @Override public String getCode()   { return code; }
    @Override public String getId()     { return id; }
    @Override public String getRunId()  { return runId; }

    @Override
    public void setRunId(String runId) {
        log.info("RunId set to '{}' for scenario '{}'", runId, id);
        this.runId = runId;
    }

    @Override
    public void setInputData(String inputData) {
        log.info("InputData set for scenario '{}'", id);
        this.inputData = inputData;
        state = ScenarioState.WAITING;
    }

    @Override public ScenarioState  getState()  { return state; }
    @Override public ScenarioResult getResult() { return result; }

    public boolean isTerminateSignal()        { return terminateSignal; }
    public boolean isHealthRegisterSignal()   { return healthRegisterSignal; }
    public void clearHealthRegisterSignal()   { healthRegisterSignal = false; }

    // ── Protected helpers for subclasses ─────────────────────────────────────

    protected String getInputData()  { return inputData; }
    protected Object getCurrentItem() { return currentItem; }

    protected void setCurrentItem(Object item) {
        this.currentItem = item;
        log.debug("Current item: '{}'", item);
    }

    protected void resetCurrentItem() { currentItem = null; }

    protected void setScenarioStep(String step) {
        log.info("Entering step [{}]", step);
        this.scenarioStep = step;
    }

    // ── Result helpers ────────────────────────────────────────────────────────

    protected void setResultOK()                                     { result = ScenarioResult.OK; }
    protected void setResultOK(String message)                       { result = ScenarioResult.OK;   setMessage(message); }
    protected void setResultNotOK()                                  { result = ScenarioResult.NOT_OK; }
    protected void setResultNotOK(String message, String detail)     { result = ScenarioResult.NOT_OK; setMessage(message); setDetail(detail); }
    protected void setResultPartial()                                { result = ScenarioResult.PARTIAL; }
    protected void setResultFailedInit()                             { result = ScenarioResult.FAILED_INIT; }
    protected void setResultFailedInit(String message, String detail){ result = ScenarioResult.FAILED_INIT; setMessage(message); setDetail(detail); }

    protected void setMessage(String msg)    { log.info("Message: {}", msg);    this.message = msg; }
    protected void setDetail(String detail)  { log.info("Detail: {}", detail);  this.detail  = detail; }

    // ── Counter helpers ───────────────────────────────────────────────────────

    protected int getTotal()   { return counters.getTotal(); }
    protected int getSuccess() { return counters.getSuccess(); }
    protected int getFail()    { return counters.getFail(); }

    protected void increaseTotal()               { counters.increaseTotal();  log.debug("total={}", getTotal()); }
    protected void increaseTotal(int n)          { counters.increaseTotal(n); log.debug("total={}", getTotal()); }
    protected void decreaseTotal()               { counters.decreaseTotal();  log.debug("total={}", getTotal()); }
    protected void increaseSuccess()             { counters.increaseSuccess(); log.debug("success={}", getSuccess()); }
    protected void increaseSuccess(int n)        { counters.increaseSuccess(n); log.debug("success={}", getSuccess()); }
    protected void increaseFail()                { counters.increaseFail();   log.debug("fail={}", getFail()); }
    protected void increaseFail(int n)           { counters.increaseFail(n);  log.debug("fail={}", getFail()); }
    protected void calculateFail()               { counters.calculateFail(); }
    protected void calculateTotal()              { counters.calculateTotal(); }

    protected void setCounts(int total, int success, int fail) {
        counters.setTotal(total);
        counters.setSuccess(success);
        counters.setFail(fail);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void autoDetectResult() {
        if (result == ScenarioResult.UNKNOWN) {
            result = ScenarioResult.from(counters.getTotal(), counters.getSuccess(), counters.getFail());
            log.info("Auto-detected result: {}", result);
        }
    }

    @Override
    public String toString() {
        return "Scenario{code='%s', id='%s', state=%s, result=%s}".formatted(code, id, state, result);
    }
}
