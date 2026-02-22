package cz.ivosahlik.robotization.scenarioengine.execution;

import cz.ivosahlik.robotization.scenarioengine.config.EngineProperties;
import cz.ivosahlik.robotization.scenarioengine.domain.ProcessingStepDef;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioExecution;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioState;
import cz.ivosahlik.robotization.scenarioengine.reporting.ReportingService;
import cz.ivosahlik.robotization.scenarioengine.repository.ScenarioExecutionRepository;
import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import cz.ivosahlik.robotization.scenarioengine.scenario.Scenario;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Core scenario orchestrator.
 *
 * <h3>Design</h3>
 * <ul>
 *   <li><strong>MongoDB</strong> is the source of truth for scenario state.
 *       On crash/restart PROCESSING rows are recovered back to WAITING.</li>
 *   <li><strong>Virtual threads</strong> – one per running scenario, created via
 *       {@link Thread#ofVirtual()}. No platform thread pool is managed here.</li>
 *   <li><strong>Dispatcher</strong> – a single {@link ScheduledExecutorService} running on a
 *       virtual thread polls MongoDB every {@code engine.executor.check-interval-ms} ms.
 *       A {@link AtomicBoolean} flag ({@code checkNow}) lets callers trigger an immediate poll
 *       instead of waiting for the next tick.</li>
 *   <li><strong>In-memory map</strong> – only currently running scenarios are kept in memory
 *       (for signal routing and suspend). Everything else lives in MongoDB.</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ScenarioExecutionService {

    private final ScenarioExecutionRepository executionRepo;
    private final ScenarioRegistry            scenarioRegistry;
    private final ConcurrencyGuard            concurrencyGuard;
    private final ReportingService            reportingService;
    private final EngineProperties            engineProps;
    private final ApplicationContext          applicationContext;

    /** Only running scenarios – used for signal routing and suspend. */
    private final ConcurrentHashMap<String, RunningScenario> running = new ConcurrentHashMap<>();

    /** Allows callers to wake the dispatcher immediately without waiting for the next tick. */
    private final AtomicBoolean checkNow = new AtomicBoolean(true);

    private ScheduledExecutorService dispatcher;

    private final String hostname = resolveHostname();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    // Application Ready Event?
    @PostConstruct
    void start() {
        if (engineProps.recoverOnStart()) {
            recoverStuckScenarios();
        }
        // Virtual-thread-backed single-thread scheduler – no platform thread pool needed
        dispatcher = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("scenario-dispatcher").factory());
        dispatcher.scheduleWithFixedDelay(
                this::dispatchLoop,
                0,
                engineProps.checkIntervalMs(),
                TimeUnit.MILLISECONDS);
        log.info("ScenarioExecutionService started (interval={}ms, recover={})",
                engineProps.checkIntervalMs(), engineProps.recoverOnStart());
    }

    @PreDestroy
    void stop() {
        dispatcher.shutdown();
        try {
            if (!dispatcher.awaitTermination(30, TimeUnit.SECONDS)) {
                dispatcher.shutdownNow();
            }
        } catch (InterruptedException e) {
            dispatcher.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("ScenarioExecutionService stopped. {} scenario(s) were still running.", running.size());
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Register a scenario for execution. Returns immediately; actual execution is asynchronous.
     */
    public ScenarioExecution submit(
            String correlationId,
            String scenarioCode,
            String threadCode,
            String inputData,
            List<ProcessingStepDef> steps,
            Instant submitDate,
            String submitHostname,
            String clientVersionId,
            String username
    ) {
        var execution = new ScenarioExecution(
                correlationId, scenarioCode, threadCode,
                inputData, steps, submitDate, submitHostname, clientVersionId, username);
        executionRepo.save(execution);
        checkNow.set(true);
        log.info("Scenario {} submitted (correlationId={})", scenarioCode, correlationId);
        return execution;
    }

    /** Send a control signal to a running scenario (e.g. "terminate"). */
    public boolean signal(String correlationId, String signal) {
        RunningScenario rs = running.get(correlationId);
        if (rs == null) {
            log.info("No running scenario {} – signal '{}' ignored.", correlationId, signal);
            return false;
        }
        rs.scenario().processSignal(signal);
        log.info("Signal '{}' delivered to scenario {}.", signal, correlationId);
        return true;
    }

    /** Mark a running scenario for cooperative suspension after its current item finishes. */
    public boolean suspend(String correlationId) {
        RunningScenario rs = running.get(correlationId);
        if (rs == null) {
            log.info("Scenario {} not running – cannot suspend.", correlationId);
            return false;
        }
        rs.shouldSuspend().set(true);
        log.info("Scenario {} marked for suspension.", correlationId);
        return true;
    }

    /** Move a SUSPENDED scenario back to WAITING so it will be dispatched again. */
    public boolean unsuspend(String correlationId) {
        Optional<ScenarioExecution> opt = executionRepo.findById(correlationId);
        if (opt.isEmpty() || opt.get().getState() != ScenarioState.SUSPENDED) {
            log.info("Scenario {} not in SUSPENDED state – unsuspend ignored.", correlationId);
            return false;
        }
        opt.get().setState(ScenarioState.WAITING);
        executionRepo.save(opt.get());
        checkNow.set(true);
        log.info("Scenario {} unsuspended.", correlationId);
        return true;
    }

    public Optional<ScenarioExecution> getStatus(String correlationId) {
        return executionRepo.findById(correlationId);
    }

    public List<ScenarioExecution> listByState(ScenarioState state) {
        return executionRepo.findByState(state);
    }

    /** Returns the live Scenario object if it is currently running (for introspection). */
    public Scenario getRunningScenario(String correlationId) {
        RunningScenario rs = running.get(correlationId);
        return rs != null ? rs.scenario() : null;
    }

    public int runningCount() {
        return running.size();
    }

    // ── Dispatcher loop ───────────────────────────────────────────────────────

    private void dispatchLoop() {
        try {
            if (!checkNow.getAndSet(false) && running.isEmpty()) {
                // No external trigger and nothing running – still check occasionally
            }
            List<ScenarioExecution> waiting = executionRepo
                    .findByStateOrderBySubmitDateAsc(ScenarioState.WAITING);
            if (waiting.isEmpty()) return;

            log.debug("Dispatch loop: {} WAITING scenario(s)", waiting.size());
            for (ScenarioExecution execution : waiting) {
                Optional<Permit> permit = concurrencyGuard.tryAcquire(execution.getThreadCode());
                if (permit.isEmpty()) {
                    log.debug("No capacity for threadCode '{}' – skipping.", execution.getThreadCode());
                    continue;
                }
                dispatch(execution, permit.get());
            }
        } catch (Exception e) {
            log.error("Dispatch loop error", e);
        }
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────

    private void dispatch(ScenarioExecution execution, Permit permit) {
        execution.setState(ScenarioState.PROCESSING);
        execution.setRunStart(Instant.now());
        execution.setProcessingHostname(hostname);
        executionRepo.save(execution);

        Scenario scenario = scenarioRegistry.create(execution.getScenarioCode());
        if (scenario instanceof AbstractScenario abs) {
            abs.setRunId(execution.getCorrelationId());
        }

        var rs = new RunningScenario(execution, scenario, permit);
        running.put(execution.getCorrelationId(), rs);

        // One virtual thread per scenario – cheap and isolated
        Thread.ofVirtual()
                .name("scenario-%s-%s".formatted(execution.getThreadCode(), execution.getCorrelationId()))
                .start(() -> executeScenario(rs));
    }

    // ── Execution loop ────────────────────────────────────────────────────────

    private void executeScenario(RunningScenario rs) {
        String cid = rs.execution().getCorrelationId();
        log.info("Executing scenario {}", cid);
        ScenarioState finalState = ScenarioState.FAILED;
        try {
            // ── Init or resume ──────────────────────────────────────────────
            Object ctx = rs.execution().getScenarioContext();
            if (ctx == null) {
                rs.scenario().setInputData(rs.execution().getInputData());
                runSteps(rs, ProcessingStepDef.StepType.PRE_PROCESSING);
                rs.execution().setScenarioContext(rs.scenario().getContext());
            } else {
                rs.scenario().setContext(ctx);
                runSteps(rs, ProcessingStepDef.StepType.POST_RESUME);
                rs.execution().setScenarioContext(rs.scenario().getContext());
            }

            // ── Item loop ───────────────────────────────────────────────────
            while (true) {
                runSteps(rs, ProcessingStepDef.StepType.PRE_ITEM);
                boolean hasMore = rs.scenario().execute();
                runSteps(rs, ProcessingStepDef.StepType.POST_ITEM);

                if (!hasMore) {
                    runSteps(rs, ProcessingStepDef.StepType.POST_PROCESSING);
                    finalState = ScenarioState.COMPLETED;
                    break;
                }
                if (rs.shouldSuspend().get()) {
                    finalState = ScenarioState.SUSPENDED;
                    break;
                }
            }
        } catch (Throwable t) {
            log.error("Scenario {} failed", cid, t);
            runStepsSilently(rs, ProcessingStepDef.StepType.POST_PROCESSING);
            finalState = ScenarioState.FAILED;
        } finally {
            finalize(rs, finalState);
        }
    }

    // ── Finalization ──────────────────────────────────────────────────────────

    private void finalize(RunningScenario rs, ScenarioState finalState) {
        String cid = rs.execution().getCorrelationId();
        try {
            rs.execution().setLatestStatistics(rs.scenario().getStatistics());
            if (finalState == ScenarioState.COMPLETED || finalState == ScenarioState.FAILED) {
                rs.execution().setRunEnd(Instant.now());
                reportingService.save(rs.execution(), rs.scenario());
            }
            rs.execution().setState(finalState);
            executionRepo.save(rs.execution());
            log.info("Scenario {} finalised as {}", cid, finalState);
        } catch (Exception e) {
            log.error("Failed to finalise scenario {}", cid, e);
        } finally {
            running.remove(cid);
            rs.permit().close();   // release semaphore slots
            checkNow.set(true);    // wake dispatcher immediately
        }
    }

    // ── Step processing ───────────────────────────────────────────────────────

    private void runSteps(RunningScenario rs, ProcessingStepDef.StepType type) {
        List<ProcessingStepDef> steps = rs.execution().getProcessingSteps();
        if (steps == null) return;
        steps.stream()
                .filter(s -> s.stepType() == type)
                .sorted(Comparator.comparingInt(ProcessingStepDef::priority))
                .forEach(step -> {
                    StepProcessor processor = applicationContext.getBean(step.module(), StepProcessor.class);
                    processor.execute(type, rs.execution(), rs.scenario(), step.stepCode(), step.parameters());
                });
    }

    private void runStepsSilently(RunningScenario rs, ProcessingStepDef.StepType type) {
        try { runSteps(rs, type); } catch (Exception e) {
            log.warn("Step {} failed silently for scenario {}", type, rs.execution().getCorrelationId(), e);
        }
    }

    // ── Crash recovery ────────────────────────────────────────────────────────

    private void recoverStuckScenarios() {
        List<ScenarioExecution> stuck = executionRepo.findByState(ScenarioState.PROCESSING);
        if (stuck.isEmpty()) return;
        log.warn("Recovering {} PROCESSING scenario(s) back to WAITING.", stuck.size());
        stuck.forEach(e -> {
            e.setState(ScenarioState.WAITING);
            executionRepo.save(e);
        });
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static String resolveHostname() {
        try { return InetAddress.getLocalHost().getHostName(); } catch (Exception e) { return "unknown"; }
    }

    // ── Inner record ─────────────────────────────────────────────────────────

    /**
     * Lightweight carrier for a scenario that is currently executing.
     * Lives only in the in-memory map – not persisted to MongoDB.
     */
    private record RunningScenario(
            ScenarioExecution execution,
            Scenario scenario,
            Permit permit,
            AtomicBoolean shouldSuspend
    ) {
        RunningScenario(ScenarioExecution execution, Scenario scenario, Permit permit) {
            this(execution, scenario, permit, new AtomicBoolean(false));
        }
    }
}
