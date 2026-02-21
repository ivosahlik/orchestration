package cz.ivosahlik.robotization.scenarioengine;

import cz.ivosahlik.robotization.scenarioengine.domain.ScenarioResult;
import cz.ivosahlik.robotization.scenarioengine.execution.ScenarioExecutionService;
import cz.ivosahlik.robotization.scenarioengine.repository.ScenarioExecutionRepository;
import cz.ivosahlik.robotization.scenarioengine.repository.ScenarioRunRepository;
import cz.ivosahlik.robotization.scenarioengine.repository.ScheduledJobRepository;
import cz.ivosahlik.robotization.scenarioengine.scenario.AbstractScenario;
import cz.ivosahlik.robotization.scenarioengine.workinghours.TimeRule;
import cz.ivosahlik.robotization.scenarioengine.workinghours.WorkingHoursService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ScenarioEngineApplication.class,
        properties = "spring.autoconfigure.exclude=" +
                "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration," +
                "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration"
)
class ScenarioEngineApplicationTests {

    // Mock all MongoDB infrastructure so no real DB connection is needed
    @MockitoBean MongoTemplate mongoTemplate;
    @MockitoBean ScenarioExecutionRepository executionRepository;
    @MockitoBean ScenarioRunRepository runRepository;
    @MockitoBean ScheduledJobRepository scheduledJobRepository;

    @Autowired ScenarioExecutionService executionService;
    @Autowired WorkingHoursService workingHoursService;

    // ── Context loads ─────────────────────────────────────────────────────────

    @Test
    void contextLoads() {
        assertThat(executionService).isNotNull();
    }

    // ── Working hours ─────────────────────────────────────────────────────────

    @Test
    void timeRule_parsesAndMatches() {
        TimeRule rule = TimeRule.parse("MON-FRI(08:00-17:00)");
        // Monday 12:00 → matches
        assertThat(rule.matches(LocalDateTime.of(2025, 1, 6, 12, 0))).isTrue();
        // Saturday → no match
        assertThat(rule.matches(LocalDateTime.of(2025, 1, 11, 12, 0))).isFalse();
        // Before 08:00 → no match
        assertThat(rule.matches(LocalDateTime.of(2025, 1, 6, 7, 59))).isFalse();
        // At 17:00 exactly → no match (exclusive upper bound)
        assertThat(rule.matches(LocalDateTime.of(2025, 1, 6, 17, 0))).isFalse();
    }

    @Test
    void timeRule_parsesCommaSeparatedDays() {
        TimeRule rule = TimeRule.parse("MON,WED,FRI(09:00-12:00)");
        assertThat(rule.days()).containsExactlyInAnyOrder(
                DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        assertThat(rule.from()).isEqualTo(LocalTime.of(9, 0));
        assertThat(rule.to()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void workingHoursService_nullRule_alwaysAllowed() {
        assertThat(workingHoursService.isWithinWorkingHours(null)).isTrue();
        assertThat(workingHoursService.isWithinWorkingHours("")).isTrue();
    }

    @Test
    void workingHoursService_pipeChain_orSemantics() {
        // MON morning OR SAT morning
        String rule = "MON-FRI(08:00-17:00)|SAT(09:00-12:00)";
        // Saturday 10:00 – matches second rule
        assertThat(workingHoursService.isWithinWorkingHours(rule,
                LocalDateTime.of(2025, 1, 11, 10, 0))).isTrue();
        // Sunday – matches nothing
        assertThat(workingHoursService.isWithinWorkingHours(rule,
                LocalDateTime.of(2025, 1, 12, 10, 0))).isFalse();
    }

    // ── ScenarioResult auto-detection ────────────────────────────────────────

    @Test
    void scenarioResult_from_allSuccess() {
        assertThat(ScenarioResult.from(5, 5, 0)).isEqualTo(ScenarioResult.OK);
    }

    @Test
    void scenarioResult_from_partial() {
        assertThat(ScenarioResult.from(5, 3, 2)).isEqualTo(ScenarioResult.PARTIAL);
    }

    @Test
    void scenarioResult_from_allFail() {
        assertThat(ScenarioResult.from(5, 0, 5)).isEqualTo(ScenarioResult.NOT_OK);
    }

    @Test
    void scenarioResult_from_empty() {
        assertThat(ScenarioResult.from(0, 0, 0)).isEqualTo(ScenarioResult.OK);
    }

    // ── Register a minimal test scenario bean ─────────────────────────────────

    @Configuration
    static class TestConfig {

        @Bean("TEST_SCENARIO")
        @Scope("prototype")
        AbstractScenario testScenario() {
            return new AbstractScenario("TEST_SCENARIO") {
                @Override
                protected boolean doExecute() {
                    increaseTotal();
                    increaseSuccess();
                    return false;
                }
            };
        }
    }
}
