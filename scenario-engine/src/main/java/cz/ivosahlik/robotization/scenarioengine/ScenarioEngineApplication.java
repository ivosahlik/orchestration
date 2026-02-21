package cz.ivosahlik.robotization.scenarioengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan               // picks up record-based @ConfigurationProperties
@EnableScheduling
public class ScenarioEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScenarioEngineApplication.class, args);
    }
}
