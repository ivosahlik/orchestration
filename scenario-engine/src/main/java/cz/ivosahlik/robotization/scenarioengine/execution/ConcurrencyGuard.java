package cz.ivosahlik.robotization.scenarioengine.execution;

import cz.ivosahlik.robotization.scenarioengine.config.ConcurrencyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Semaphore-based concurrency guard.
 *
 * <p>Replaces the old {@code SharedThreadPool} (349 lines of manual thread tracking).
 * Virtual threads are cheap; we only need to limit <em>concurrency</em>, not manage
 * platform threads. Two semaphores per acquisition:
 * <ol>
 *   <li>Per-process slot – limits how many of the same scenario type run in parallel.</li>
 *   <li>Global slot – limits the total across all process types.</li>
 * </ol>
 * Both must be available; if either is exhausted the method returns {@link Optional#empty()}.
 */
@Slf4j
@Service
public class ConcurrencyGuard {

    private final Semaphore globalSlots;
    private final Map<String, Semaphore> perProcessSlots = new ConcurrentHashMap<>();
    private final ConcurrencyProperties props;

    public ConcurrencyGuard(ConcurrencyProperties props) {
        this.props = props;
        this.globalSlots = new Semaphore(props.maxTotal(), true);
        if (props.perProcess() != null) {
            props.perProcess().forEach((code, cfg) ->
                    perProcessSlots.put(code, new Semaphore(cfg.max(), true)));
        }
        log.info("ConcurrencyGuard initialised – global max={}", props.maxTotal());
    }

    /**
     * Attempt to acquire a permit for the given process code.
     *
     * @return a {@link Permit} that <strong>must</strong> be closed when the work finishes,
     *         or {@link Optional#empty()} if no capacity is available right now.
     */
    public Optional<Permit> tryAcquire(String processCode) {
        Semaphore local = resolveProcessSemaphore(processCode);

        if (!local.tryAcquire()) {
            log.debug("No per-process slot for '{}'  (available={})", processCode, local.availablePermits());
            return Optional.empty();
        }
        if (!globalSlots.tryAcquire()) {
            local.release();
            log.debug("No global slot (available={})", globalSlots.availablePermits());
            return Optional.empty();
        }
        log.debug("Permit acquired for '{}' – global remaining={}", processCode, globalSlots.availablePermits());
        return Optional.of(new Permit(local, globalSlots));
    }

    /** Available global capacity right now. */
    public int availableGlobal() {
        return globalSlots.availablePermits();
    }

    /** Available per-process capacity right now. */
    public int availableForProcess(String processCode) {
        return resolveProcessSemaphore(processCode).availablePermits();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private Semaphore resolveProcessSemaphore(String code) {
        return perProcessSlots.computeIfAbsent(code, c -> {
            int max = props.maxForProcess(c);
            int effective = max > 0 ? max : props.maxTotal();
            log.info("Creating process semaphore for '{}' max={}", c, effective);
            return new Semaphore(effective, true);
        });
    }
}
