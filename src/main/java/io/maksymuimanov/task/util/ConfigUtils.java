package io.maksymuimanov.task.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * Provides convenient access to system configuration properties used by the
 * asynchronous API aggregator service. This utility allows retrieving typed
 * configuration values (String, Integer, Long, Duration) with safe fallbacks
 * when system properties are not defined.
 *
 * <p>All methods are thread-safe and designed for lightweight use during
 * Netty server initialization or runtime configuration loading.</p>
 */
@UtilityClass
public class ConfigUtils {
    /**
     * Returns the system property value for the specified key or the given default
     * if the property is not set.
     *
     * @param key the name of the system property
     * @param defaultValue the value to return if the property is undefined
     * @return the resolved String property value or the default
     */
    public String getOrDefault(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    /**
     * Returns the system property value as an {@link Integer} or the default
     * if the property is not defined or cannot be parsed.
     *
     * @param key the name of the system property
     * @param defaultValue the value to return if the property is undefined
     * @return the resolved Integer property value or the default
     */
    public Integer getOrDefault(String key, Integer defaultValue) {
        return Integer.getInteger(key, defaultValue);
    }

    /**
     * Returns the system property value as a {@link Long} or the default
     * if the property is not defined or cannot be parsed.
     *
     * @param key the name of the system property
     * @param defaultValue the value to return if the property is undefined
     * @return the resolved Long property value or the default
     */
    public Long getOrDefault(String key, Long defaultValue) {
        return Long.getLong(key, defaultValue);
    }

    /**
     * Returns the system property value as a {@link Boolean}, or the default
     * if the property is not defined. Accepts standard boolean string values
     * such as {@code "true"} or {@code "false"}.
     *
     * @param key the name of the system property
     * @param defaultValue the value to return if the property is undefined
     * @return the resolved Boolean property value or the default
     */
    public Boolean getOrDefault(String key, Boolean defaultValue) {
        return Boolean.parseBoolean(getOrDefault(key, Boolean.toString(defaultValue)).toLowerCase());
    }

    /**
     * Returns the system property value as a {@link Duration}, using the given
     * default if the property is not defined. The property value is interpreted
     * as milliseconds.
     *
     * @param key the name of the system property
     * @param defaultValue the duration to return if the property is undefined
     * @return the resolved Duration property value or the default
     */
    public Duration getOrDefault(String key, Duration defaultValue) {
        return Duration.ofMillis(getOrDefault(key, defaultValue.toMillis()));
    }

    /**
     * Returns the system property value as an enum constant of the same type as
     * the provided default value. If the property is not defined, the default
     * enum constant is returned.
     *
     * @param <T> the enum type
     * @param key the name of the system property
     * @param defaultValue the default enum constant to return if the property is undefined
     * @return the resolved enum constant or the default if no property is set
     */
    public <T extends Enum<T>> T getOrDefault(String key, T defaultValue) {
        return Enum.valueOf(defaultValue.getDeclaringClass(), getOrDefault(key, defaultValue.name()));
    }
}