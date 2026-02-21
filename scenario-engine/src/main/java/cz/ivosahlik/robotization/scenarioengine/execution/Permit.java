package cz.ivosahlik.robotization.scenarioengine.execution;

import java.util.concurrent.Semaphore;

/**
 * A released token representing reserved concurrency capacity.
 * Implements {@link AutoCloseable} so it can be used in try-with-resources.
 *
 * <p>Holds two semaphore permits: one global and one per-process.
 * Both are released atomically on {@link #close()}.
 */
public record Permit(Semaphore processSlot, Semaphore globalSlot) implements AutoCloseable {

    @Override
    public void close() {
        processSlot.release();
        globalSlot.release();
    }
}
