package com.notificationmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entry point for NotificationMod.
 * <p>
 * Sends Windows balloon notifications for:
 * <ul>
 *   <li>Other players joining or leaving the server.</li>
 *   <li>Player chat messages received in-game.</li>
 * </ul>
 */
@Mod(modid = NotificationMod.MOD_ID, name = NotificationMod.MOD_NAME, version = NotificationMod.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.12.2]")
public class NotificationMod {

    public static final String MOD_ID = "notificationmod";
    public static final String MOD_NAME = "Notification Mod";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        WindowsNotification.init();
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new NotificationEventHandler());
        LOGGER.info("{} initialized.", MOD_NAME);
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        WindowsNotification.cleanup();
    }
}
