package cz.ivosahlik.robotization.scenarioengine.repository;

import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ScenarioRunRepository extends MongoRepository<ScenarioRun, String> {

    Optional<ScenarioRun> findByCorrelationId(String correlationId);

    Page<ScenarioRun> findByScenarioCode(String scenarioCode, Pageable pageable);

    List<ScenarioRun> findByScenarioCodeAndProcessingStartBetween(
            String scenarioCode, Instant from, Instant to);

    long countByScenarioCodeAndResult(String scenarioCode, String result);
}
