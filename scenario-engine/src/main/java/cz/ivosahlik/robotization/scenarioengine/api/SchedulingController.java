package cz.ivosahlik.robotization.scenarioengine.api;

import cz.ivosahlik.robotization.scenarioengine.api.dto.ScheduledJobRequest;
import cz.ivosahlik.robotization.scenarioengine.domain.ScheduledJob;
import cz.ivosahlik.robotization.scenarioengine.scheduling.SchedulingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for managing scheduled jobs stored in MongoDB.
 *
 * <pre>
 * GET    /api/jobs       – list all
 * POST   /api/jobs       – create
 * PUT    /api/jobs/{id}  – update
 * DELETE /api/jobs/{id}  – delete
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping
    public List<ScheduledJob> listAll() {
        return schedulingService.listAll();
    }

    @PostMapping
    public ResponseEntity<ScheduledJob> create(@Valid @RequestBody ScheduledJobRequest req) {
        ScheduledJob job = schedulingService.create(req.toScheduledJob());
        log.info("Scheduled job '{}' created.", job.getName());
        return ResponseEntity.status(201).body(job);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduledJob> update(
            @PathVariable String id,
            @Valid @RequestBody ScheduledJobRequest req
    ) {
        return schedulingService.update(id, req.toScheduledJob())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        schedulingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
