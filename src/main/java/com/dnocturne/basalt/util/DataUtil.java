package com.dnocturne.basalt.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for handling data type conversions.
 *
 * <p>Useful for handling deserialized data from JSON/YAML where numeric
 * types may come back as strings or different number types.</p>
 */
public final class DataUtil {

    private DataUtil() {
    }

    /**
     * Safely convert an object to a double.
     *
     * <p>Handles Number types (Integer, Long, Float, Double) and String representations.</p>
     *
     * @param value        The value to convert
     * @param defaultValue The default value if conversion fails
     * @return The converted double, or defaultValue if null or not convertible
     */
    public static double toDouble(@Nullable Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Safely convert an object to an int.
     *
     * @param value        The value to convert
     * @param defaultValue The default value if conversion fails
     * @return The converted int, or defaultValue if null or not convertible
     */
    public static int toInt(@Nullable Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Safely convert an object to a long.
     *
     * @param value        The value to convert
     * @param defaultValue The default value if conversion fails
     * @return The converted long, or defaultValue if null or not convertible
     */
    public static long toLong(@Nullable Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Safely convert an object to a float.
     *
     * @param value        The value to convert
     * @param defaultValue The default value if conversion fails
     * @return The converted float, or defaultValue if null or not convertible
     */
    public static float toFloat(@Nullable Object value, float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String str) {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Safely convert an object to a boolean.
     *
     * <p>Accepts Boolean, Number (0 = false, non-zero = true), and String
     * ("true"/"false", "yes"/"no", "1"/"0").</p>
     *
     * @param value        The value to convert
     * @param defaultValue The default value if conversion fails
     * @return The converted boolean, or defaultValue if null or not convertible
     */
    public static boolean toBoolean(@Nullable Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0;
        }
        if (value instanceof String str) {
            String lower = str.toLowerCase();
            if (lower.equals("true") || lower.equals("yes") || lower.equals("1")) {
                return true;
            }
            if (lower.equals("false") || lower.equals("no") || lower.equals("0")) {
                return false;
            }
        }
        return defaultValue;
    }

    /**
     * Safely convert an object to a String.
     *
     * @param value        The value to convert
     * @param defaultValue The default value if null
     * @return The string representation, or defaultValue if null
     */
    public static @NotNull String toString(@Nullable Object value, @NotNull String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Clamp an int value between min and max.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Clamp a long value between min and max.
     */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(value, max));
    }
}
