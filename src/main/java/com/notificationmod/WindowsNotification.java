package com.notificationmod;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

/**
 * Utility class that sends Windows balloon notifications via Java AWT SystemTray.
 * Falls back to a no-op on platforms where SystemTray is not supported.
 */
public final class WindowsNotification {

    private static TrayIcon trayIcon;
    private static boolean initialized = false;

    private WindowsNotification() {}

    /**
     * Initialize the system tray icon.  Must be called once during mod pre-init.
     */
    public static void init() {
        if (!SystemTray.isSupported()) {
            NotificationMod.LOGGER.warn("SystemTray is not supported on this platform – notifications disabled.");
            return;
        }
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image icon = createIcon();
            trayIcon = new TrayIcon(icon, NotificationMod.MOD_NAME);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            initialized = true;
            NotificationMod.LOGGER.info("SystemTray notifications initialized.");
        } catch (AWTException e) {
            NotificationMod.LOGGER.error("Failed to initialize SystemTray", e);
        }
    }

    /**
     * Sends a balloon/toast notification.
     *
     * @param title   The notification title.
     * @param message The notification body.
     */
    public static void sendNotification(String title, String message) {
        if (!initialized || trayIcon == null) {
            return;
        }
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }

    /**
     * Remove the tray icon when the mod shuts down to avoid orphaned icons.
     */
    public static void cleanup() {
        if (initialized && trayIcon != null) {
            try {
                SystemTray.getSystemTray().remove(trayIcon);
            } catch (IllegalArgumentException e) {
                NotificationMod.LOGGER.warn("TrayIcon was not present in SystemTray during cleanup", e);
            }
            trayIcon = null;
            initialized = false;
        }
    }

    /** Creates a simple 16×16 green square icon used in the system tray. */
    private static Image createIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x4CAF50)); // Material Green
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        return image;
    }
}
