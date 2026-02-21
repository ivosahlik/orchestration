package cz.ivosahlik.robotization.scenarioengine.workinghours;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Evaluates working-hours rules.
 *
 * <p>Multiple rules are OR-ed: if any one rule matches the current time, processing is allowed.
 * Rules are separated by {@code "|"} or stored as a list.
 *
 * <p>Examples:
 * <pre>
 *   "MON-FRI(08:00-17:00)"               → business days, 8–17
 *   "MON-FRI(08:00-12:00)|SAT(09:00-12:00)" → weekdays morning OR Saturday morning
 * </pre>
 */
@Slf4j
@Service
public class WorkingHoursService {

    /**
     * Returns {@code true} if the current time matches at least one rule in the spec.
     *
     * @param ruleSpec  pipe-separated rule string; {@code null} or empty means "always allowed".
     */
    public boolean isWithinWorkingHours(String ruleSpec) {
        return isWithinWorkingHours(ruleSpec, LocalDateTime.now());
    }

    /** Testable overload – pass an explicit {@link LocalDateTime}. */
    public boolean isWithinWorkingHours(String ruleSpec, LocalDateTime now) {
        if (ruleSpec == null || ruleSpec.isBlank()) return true;

        List<TimeRule> rules = parse(ruleSpec);
        boolean allowed = rules.stream().anyMatch(r -> r.matches(now));
        log.debug("Working hours check at {} for rules '{}': {}", now, ruleSpec, allowed);
        return allowed;
    }

    private List<TimeRule> parse(String spec) {
        return Arrays.stream(spec.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(TimeRule::parse)
                .toList();
    }
}
