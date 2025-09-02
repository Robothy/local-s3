package com.robothy.s3.core.utils.s3vectors;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time formatting in S3 vectors operations.
 * Provides consistent date formatting across all S3 vectors services.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Formats a timestamp in milliseconds to ISO 8601 string format.
     * Used for consistent date formatting in S3 vectors API responses.
     *
     * @param timestampMillis the timestamp in milliseconds since epoch
     * @return the formatted ISO 8601 date string
     */
    public static String formatTimestamp(long timestampMillis) {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestampMillis));
    }

    /**
     * Gets the current timestamp in milliseconds.
     * Provides a centralized way to get current time for consistent behavior.
     *
     * @return the current timestamp in milliseconds since epoch
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
