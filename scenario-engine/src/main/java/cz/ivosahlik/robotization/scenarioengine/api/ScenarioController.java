package cz.ivosahlik.robotization.scenarioengine.api;

import cz.ivosahlik.robotization.scenarioengine.api.dto.ScenarioStatus;
import cz.ivosahlik.robotization.scenarioengine.api.dto.SignalRequest;
import cz.ivosahlik.robotization.scenarioengine.api.dto.SubmitRequest;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioExecution;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioState;
import cz.ivosahlik.robotization.scenarioengine.execution.ScenarioExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for scenario lifecycle management.
 *
 * <pre>
 * POST   /api/scenarios          – submit a new scenario
 * GET    /api/scenarios/{id}     – get status
 * GET    /api/scenarios?state=   – list by state
 * POST   /api/scenarios/{id}/signal   – send signal (terminate, etc.)
 * POST   /api/scenarios/{id}/suspend  – suspend a running scenario
 * POST   /api/scenarios/{id}/unsuspend – resume a suspended scenario
 * GET    /api/scenarios/stats    – engine stats
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioExecutionService executionService;

    // ── Submit ────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ScenarioStatus> submit(@Valid @RequestBody SubmitRequest req) {
        ScenarioExecution execution = executionService.submit(
                req.resolvedCorrelationId(),
                req.scenarioCode(),
                req.resolvedThreadCode(),
                req.inputData(),
                req.processingSteps(),
                req.submitDate(),
                req.submitHostname(),
                req.clientVersionId(),
                req.username()
        );
        log.info("Scenario {} submitted via API, correlationId={}", req.scenarioCode(), execution.getCorrelationId());
        return ResponseEntity.accepted().body(ScenarioStatus.from(execution));
    }

    // ── Status ────────────────────────────────────────────────────────────────

    @GetMapping("/{correlationId}")
    public ResponseEntity<ScenarioStatus> getStatus(@PathVariable String correlationId) {
        return executionService.getStatus(correlationId)
                .map(e -> ResponseEntity.ok(ScenarioStatus.from(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<ScenarioStatus> listByState(
            @RequestParam(defaultValue = "WAITING") ScenarioState state
    ) {
        return executionService.listByState(state)
                .stream()
                .map(ScenarioStatus::from)
                .toList();
    }

    // ── Control ───────────────────────────────────────────────────────────────

    @PostMapping("/{correlationId}/signal")
    public ResponseEntity<Void> signal(
            @PathVariable String correlationId,
            @Valid @RequestBody SignalRequest req
    ) {
        boolean delivered = executionService.signal(correlationId, req.signal());
        return delivered ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{correlationId}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable String correlationId) {
        boolean ok = executionService.suspend(correlationId);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{correlationId}/unsuspend")
    public ResponseEntity<Void> unsuspend(@PathVariable String correlationId) {
        boolean ok = executionService.unsuspend(correlationId);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // ── Engine stats ──────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "running", executionService.runningCount(),
                "waiting", executionService.listByState(ScenarioState.WAITING).size(),
                "suspended", executionService.listByState(ScenarioState.SUSPENDED).size()
        );
    }
}
