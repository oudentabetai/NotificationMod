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

    private static final String CATEGORY = "notifications";

    /** Notification mode for player join/leave events. */
    public static NotificationMode joinNotificationMode = NotificationMode.BACKGROUND_ONLY;

    /** Notification mode for chat message events. */
    public static NotificationMode messageNotificationMode = NotificationMode.BACKGROUND_ONLY;

    private NotificationConfig() {}

    /**
     * Loads (and if necessary creates) the configuration file.
     *
     * @param configFile The {@link File} pointing to the mod's config file.
     */
    public static void load(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        String validValues = "Valid values: " + NotificationMode.ALWAYS.name() + ", " + NotificationMode.BACKGROUND_ONLY.name();

        joinNotificationMode = parseMode(
                config.getString(
                        "join_notification_mode",
                        CATEGORY,
                        NotificationMode.BACKGROUND_ONLY.name(),
                        "When to send player join/leave notifications. " + validValues));

        messageNotificationMode = parseMode(
                config.getString(
                        "message_notification_mode",
                        CATEGORY,
                        NotificationMode.BACKGROUND_ONLY.name(),
                        "When to send chat message notifications. " + validValues));

        if (config.hasChanged()) {
            config.save();
        }
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
