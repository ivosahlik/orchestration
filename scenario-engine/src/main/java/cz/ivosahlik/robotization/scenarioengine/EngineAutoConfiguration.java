package cz.ivosahlik.robotization.scenarioengine;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot auto-configuration entry point for the scenario engine.
 *
 * <p>When {@code ubot-scenario-engine} is on the classpath of another Spring Boot
 * application, this class is discovered automatically via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 * No {@code @ComponentScan} or {@code @Import} is needed in the consuming application.
 *
 * <p>Registers all engine beans: execution service, concurrency guard, repositories,
 * scheduler, reporting service, working-hours service, and REST controllers.
 */
@AutoConfiguration
@ComponentScan(basePackages = "cz.ivosahlik.robotization.scenarioengine")
@EnableMongoRepositories(basePackages = "cz.ivosahlik.robotization.scenarioengine.repository")
@ConfigurationPropertiesScan(basePackages = "cz.ivosahlik.robotization.scenarioengine.config")
@EnableScheduling
public class EngineAutoConfiguration {
    // no beans defined here – component scan picks up everything
}
