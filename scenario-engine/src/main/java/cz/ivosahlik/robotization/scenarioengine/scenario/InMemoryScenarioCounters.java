package cz.ivosahlik.robotization.scenarioengine.scenario;

/**
 * Default in-memory counter implementation.
 */
public final class InMemoryScenarioCounters implements ScenarioCounters {

    private int total;
    private int success;
    private int fail;

    @Override public int getTotal()   { return total; }
    @Override public int getSuccess() { return success; }
    @Override public int getFail()    { return fail; }

    @Override public void setTotal(int v)   { this.total   = v; }
    @Override public void setSuccess(int v) { this.success = v; }
    @Override public void setFail(int v)    { this.fail    = v; }
}
