package cz.ivosahlik.robotization.scenarioengine.repository;

import cz.ivosahlik.robotization.scenarioengine.domain.ScheduledJob;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduledJobRepository extends MongoRepository<ScheduledJob, String> {

    Optional<ScheduledJob> findByName(String name);

    List<ScheduledJob> findByEnabled(boolean enabled);
}
