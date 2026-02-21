package cz.ivosahlik.robotization.scenarioengine.scenario;

/**
 * Strategy interface for scenario execution counters.
 * Implementations can be in-memory (default) or delegated to a persistent store.
 * Eliminates the dual-path {@code if (persistentStatistics == null)} pattern.
 */
public interface ScenarioCounters {

    int getTotal();
    int getSuccess();
    int getFail();

    void setTotal(int v);
    void setSuccess(int v);
    void setFail(int v);

    default void increaseTotal() { increaseTotal(1); }
    default void increaseSuccess() { increaseSuccess(1); }
    default void increaseFail() { increaseFail(1); }
    default void decreaseTotal() { increaseTotal(-1); }

    default void increaseTotal(int amount) { setTotal(getTotal() + amount); }
    default void increaseSuccess(int amount) { setSuccess(getSuccess() + amount); }
    default void increaseFail(int amount) { setFail(getFail() + amount); }

    default void calculateFail() { setFail(getTotal() - getSuccess()); }
    default void calculateTotal() { setTotal(getSuccess() + getFail()); }
}
