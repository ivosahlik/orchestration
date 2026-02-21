package cz.ivosahlik.robotization.scenarioengine.workinghours;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable record representing one working-hours rule.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>{@code "MONDAY(09:00-17:00)"} – single day</li>
 *   <li>{@code "MON-FRI(08:00-17:00)"} – day range</li>
 *   <li>{@code "MON,WED,FRI(09:00-12:00)"} – comma-separated days</li>
 * </ul>
 */
public record TimeRule(Set<DayOfWeek> days, LocalTime from, LocalTime to) {

    private static final Pattern RULE_PATTERN =
            Pattern.compile("([A-Z,\\-]+)\\((\\d{2}:\\d{2})-(\\d{2}:\\d{2})\\)", Pattern.CASE_INSENSITIVE);

    /** Returns {@code true} if {@code dt} falls within this rule. */
    public boolean matches(LocalDateTime dt) {
        return days.contains(dt.getDayOfWeek())
                && !dt.toLocalTime().isBefore(from)
                && dt.toLocalTime().isBefore(to);
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Parse a rule string like {@code "MON-FRI(08:00-17:00)"}.
     *
     * @throws IllegalArgumentException on parse failure.
     */
    public static TimeRule parse(String rule) {
        Matcher m = RULE_PATTERN.matcher(rule.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse working-hours rule: '" + rule + "'");
        }
        Set<DayOfWeek> days = parseDays(m.group(1));
        LocalTime from = LocalTime.parse(m.group(2));
        LocalTime to   = LocalTime.parse(m.group(3));
        return new TimeRule(days, from, to);
    }

    private static Set<DayOfWeek> parseDays(String spec) {
        Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        // Range: MON-FRI
        if (spec.contains("-")) {
            String[] parts = spec.split("-", 2);
            DayOfWeek start = toDayOfWeek(parts[0]);
            DayOfWeek end   = toDayOfWeek(parts[1]);
            for (DayOfWeek d = start; ; d = d.plus(1)) {
                result.add(d);
                if (d == end) break;
            }
            return result;
        }
        // Comma list: MON,WED,FRI  or single day
        for (String part : spec.split(",")) {
            result.add(toDayOfWeek(part.trim()));
        }
        return result;
    }

    private static DayOfWeek toDayOfWeek(String s) {
        return switch (s.toUpperCase()) {
            case "MON", "MONDAY"    -> DayOfWeek.MONDAY;
            case "TUE", "TUESDAY"   -> DayOfWeek.TUESDAY;
            case "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY;
            case "THU", "THURSDAY"  -> DayOfWeek.THURSDAY;
            case "FRI", "FRIDAY"    -> DayOfWeek.FRIDAY;
            case "SAT", "SATURDAY"  -> DayOfWeek.SATURDAY;
            case "SUN", "SUNDAY"    -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Unknown day: " + s);
        };
    }
}
