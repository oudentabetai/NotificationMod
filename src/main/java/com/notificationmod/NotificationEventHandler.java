package com.notificationmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Listens for multiplayer events and triggers Windows notifications:
 * <ul>
 *   <li>Other players joining or leaving the server (checked once per second via the client tick).</li>
 *   <li>Player chat messages received in-game.</li>
 *   <li>Player taking damage (checked once per second via the client tick).</li>
 *   <li>Player death.</li>
 * </ul>
 */
public class NotificationEventHandler {

    /** How many client ticks to wait between player-list and health comparisons (20 ticks = 1 second). */
    private static final int CHECK_INTERVAL = 20;

    private final Set<String> currentPlayers = new HashSet<>();
    private int tickCounter = 0;
    /**
     * Set to {@code true} right after connecting so that the very first snapshot of the
     * player list does not produce spurious "player joined" notifications for players
     * who were already in the server.
     */
    private boolean firstCheck = true;

    /**
     * The local player's health as of the last CHECK_INTERVAL tick.
     * Initialised to {@code -1} to indicate "not yet recorded".
     */
    private float lastHealth = -1.0f;

    // -------------------------------------------------------------------------
    // Connection events
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        currentPlayers.clear();
        tickCounter = 0;
        firstCheck = true;
        lastHealth = -1.0f;
    }

    @SubscribeEvent
    public void onClientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        currentPlayers.clear();
        firstCheck = true;
        lastHealth = -1.0f;
    }

    // -------------------------------------------------------------------------
    // Chat event  (type 0 = regular player chat; type 1 = system; type 2 = action bar)
    // Only notify for type-0 player chat messages.
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        // Only notify for player chat messages (type 0); skip system and action-bar messages.
        if (event.getType() != ChatType.CHAT) {
            return;
        }
        if (shouldNotify(NotificationConfig.messageNotificationMode)) {
            String message = event.getMessage().getUnformattedText();
            WindowsNotification.sendNotification("Minecraft Chat", message);
        }
    }

    // -------------------------------------------------------------------------
    // In-game config GUI saved
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (NotificationMod.MOD_ID.equals(event.getModID())) {
            NotificationConfig.syncFromConfig();
        }
    }

    // -------------------------------------------------------------------------
    // Per-tick player-list comparison and health tracking
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getConnection() == null) {
            currentPlayers.clear();
            firstCheck = true;
            lastHealth = -1.0f;
            return;
        }

        // ---- Health / damage / death tracking ----
        if (mc.player != null) {
            float health = mc.player.getHealth();
            if (lastHealth < 0) {
                // First reading after connecting – establish baseline.
                lastHealth = health;
            } else if (health <= 0 && lastHealth > 0) {
                // Transition from alive to dead.
                if (shouldNotify(NotificationConfig.deathNotificationMode)) {
                    WindowsNotification.sendNotification("You Died!", mc.getSession().getUsername() + " has died.");
                }
                NotificationMod.LOGGER.info("Local player died.");
                lastHealth = health;
            } else if (health < lastHealth) {
                // Health decreased – player took damage.
                if (shouldNotify(NotificationConfig.damageNotificationMode)) {
                    WindowsNotification.sendNotification("Damage Taken",
                            String.format("HP: %.1f \u2192 %.1f", lastHealth, health));
                }
                lastHealth = health;
            } else {
                // Health unchanged or increased (regeneration / respawn).
                // Updating lastHealth here ensures subsequent damage/death events are
                // detected correctly, including deaths after a respawn.
                lastHealth = health;
            }
        }

        // ---- Player join/leave tracking ----
        Set<String> newPlayers = new HashSet<>();
        for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
            newPlayers.add(info.getGameProfile().getName());
        }

        if (firstCheck) {
            // Initialize the baseline without sending any notifications.
            currentPlayers.addAll(newPlayers);
            firstCheck = false;
            return;
        }

        String ownName = mc.getSession().getUsername();

        // Detect players who just joined.
        for (String player : newPlayers) {
            if (!currentPlayers.contains(player) && !player.equals(ownName)) {
                if (shouldNotify(NotificationConfig.joinNotificationMode)) {
                    WindowsNotification.sendNotification("Player Joined", player + " joined the server");
                }
                NotificationMod.LOGGER.info("Player joined: {}", player);
            }
        }

        // Detect players who just left.
        for (String player : currentPlayers) {
            if (!newPlayers.contains(player) && !player.equals(ownName)) {
                if (shouldNotify(NotificationConfig.joinNotificationMode)) {
                    WindowsNotification.sendNotification("Player Left", player + " left the server");
                }
                NotificationMod.LOGGER.info("Player left: {}", player);
            }
        }

        currentPlayers.clear();
        currentPlayers.addAll(newPlayers);
    }

    /**
     * Returns {@code true} if a notification with the given mode should be sent now.
     * When the mode is {@link NotificationConfig.NotificationMode#ALWAYS}, always returns {@code true}.
     * When the mode is {@link NotificationConfig.NotificationMode#BACKGROUND_ONLY}, returns {@code true}
     * only when the Minecraft window is not currently focused.
     */
    private static boolean shouldNotify(NotificationConfig.NotificationMode mode) {
        return mode == NotificationConfig.NotificationMode.ALWAYS || WindowsNotification.isWindowInBackground();
    }
}
