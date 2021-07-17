package xyz.shenmj.tools;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * ISO8601 DateTime 帮助类
 *
 * @author SHEN Minjiang
 */
public class ISO8601DateTime {
    private ISO8601DateTime() {
    }

    private static final String DATE_FORMAT_ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSS'Z']";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_ISO8601_PATTERN);

    public static String getPattern() {
        return DATE_FORMAT_ISO8601_PATTERN;
    }

    public static DateTimeFormatter getFormatter() {
        return dateTimeFormatter;
    }

    public static String format(LocalDateTime dateTime) {
        return dateTimeFormatter.format(dateTime);
    }

    public static String format(Long epochMilliseconds) {
        return format(toEpochDateTime(epochMilliseconds));
    }

    public static LocalDateTime toEpochDateTime(Long epochMilliseconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilliseconds), ZoneId.of("UTC"));
    }

    public static String formattedUtcNow() {
        return format(utcNow());
    }

    public static LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static LocalDateTime parse(String dateTime) {
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
    }

    public boolean zonedSameDay(LocalDateTime from, LocalDateTime to, ZoneId zoneId) {
        ZonedDateTime zonedFrom = ZonedDateTime.of(from, ZoneOffset.UTC).withZoneSameInstant(zoneId);
        ZonedDateTime zonedTo = ZonedDateTime.of(to, ZoneOffset.UTC).withZoneSameInstant(zoneId);
        return zonedFrom.toLocalDate().equals(zonedTo.toLocalDate());
    }

    public static void main(String[] args) {
    }
}
