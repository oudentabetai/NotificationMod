package com.notificationmod;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Manages per-notification-type configuration for NotificationMod.
 *
 * <p>Two modes are available for each notification type:
 * <ul>
 *   <li>{@link NotificationMode#ALWAYS} – notify regardless of whether the game window is focused.</li>
 *   <li>{@link NotificationMode#BACKGROUND_ONLY} – notify only when the game window is not focused.</li>
 * </ul>
 */
public final class NotificationConfig {

    /** Controls when a notification of a given type should be displayed. */
    public enum NotificationMode {
        /** Always send the notification, regardless of window focus. */
        ALWAYS,
        /** Send the notification only when the Minecraft window is in the background. */
        BACKGROUND_ONLY
    }

    public static final String CATEGORY = "notifications";

    /** The underlying {@link Configuration} instance; used by the in-game config GUI. */
    public static Configuration configuration;

    /** Notification mode for player join/leave events. */
    public static NotificationMode joinNotificationMode = NotificationMode.BACKGROUND_ONLY;

    /** Notification mode for chat message events. */
    public static NotificationMode messageNotificationMode = NotificationMode.BACKGROUND_ONLY;

    /** Notification mode for damage events. */
    public static NotificationMode damageNotificationMode = NotificationMode.BACKGROUND_ONLY;

    /** Notification mode for death events. */
    public static NotificationMode deathNotificationMode = NotificationMode.BACKGROUND_ONLY;

    private NotificationConfig() {}

    /**
     * Loads (and if necessary creates) the configuration file, then syncs values into
     * the static fields via {@link #syncFromConfig()}.
     *
     * @param configFile The {@link File} pointing to the mod's config file.
     */
    public static void load(File configFile) {
        configuration = new Configuration(configFile);
        configuration.load();

        String[] validValues = {NotificationMode.ALWAYS.name(), NotificationMode.BACKGROUND_ONLY.name()};
        String validComment = "Valid values: " + NotificationMode.ALWAYS.name() + ", " + NotificationMode.BACKGROUND_ONLY.name();

        configuration.getString("join_notification_mode", CATEGORY,
                NotificationMode.BACKGROUND_ONLY.name(),
                "When to send player join/leave notifications. " + validComment,
                validValues);

        configuration.getString("message_notification_mode", CATEGORY,
                NotificationMode.BACKGROUND_ONLY.name(),
                "When to send chat message notifications. " + validComment,
                validValues);

        configuration.getString("damage_notification_mode", CATEGORY,
                NotificationMode.BACKGROUND_ONLY.name(),
                "When to send damage notifications. " + validComment,
                validValues);

        configuration.getString("death_notification_mode", CATEGORY,
                NotificationMode.BACKGROUND_ONLY.name(),
                "When to send death notifications. " + validComment,
                validValues);

        if (configuration.hasChanged()) {
            configuration.save();
        }

        syncFromConfig();
    }

    /**
     * Re-reads all notification-mode values from the in-memory {@link Configuration} object
     * and updates the corresponding static fields.  Called automatically after {@link #load}
     * and whenever the player saves changes through the in-game config GUI.
     */
    public static void syncFromConfig() {
        if (configuration == null) {
            return;
        }
        joinNotificationMode = parseMode(
                configuration.get(CATEGORY, "join_notification_mode", NotificationMode.BACKGROUND_ONLY.name()).getString());
        messageNotificationMode = parseMode(
                configuration.get(CATEGORY, "message_notification_mode", NotificationMode.BACKGROUND_ONLY.name()).getString());
        damageNotificationMode = parseMode(
                configuration.get(CATEGORY, "damage_notification_mode", NotificationMode.BACKGROUND_ONLY.name()).getString());
        deathNotificationMode = parseMode(
                configuration.get(CATEGORY, "death_notification_mode", NotificationMode.BACKGROUND_ONLY.name()).getString());
    }

    private static NotificationMode parseMode(String value) {
        try {
            return NotificationMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            NotificationMod.LOGGER.warn("Invalid notification mode '{}', falling back to BACKGROUND_ONLY.", value);
            return NotificationMode.BACKGROUND_ONLY;
        }
    }
}
