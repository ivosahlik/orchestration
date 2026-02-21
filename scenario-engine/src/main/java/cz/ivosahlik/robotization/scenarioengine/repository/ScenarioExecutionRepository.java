package cz.ivosahlik.robotization.scenarioengine.repository;

import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioExecution;
import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioState;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScenarioExecutionRepository extends MongoRepository<ScenarioExecution, String> {

    List<ScenarioExecution> findByState(ScenarioState state);

    List<ScenarioExecution> findByStateOrderBySubmitDateAsc(ScenarioState state);

    List<ScenarioExecution> findByScenarioCodeAndState(String scenarioCode, ScenarioState state);

    long countByState(ScenarioState state);

    long countByThreadCodeAndState(String threadCode, ScenarioState state);
}
