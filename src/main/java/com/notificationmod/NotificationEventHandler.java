package com.notificationmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
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
 *   <li>Your own connection to / disconnection from a server.</li>
 * </ul>
 */
public class NotificationEventHandler {

    /** How many client ticks to wait between player-list comparisons (20 ticks = 1 second). */
    private static final int CHECK_INTERVAL = 20;

    private final Set<String> currentPlayers = new HashSet<>();
    private int tickCounter = 0;
    /**
     * Set to {@code true} right after connecting so that the very first snapshot of the
     * player list does not produce spurious "player joined" notifications for players
     * who were already in the server.
     */
    private boolean firstCheck = true;

    // -------------------------------------------------------------------------
    // Connection events
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        currentPlayers.clear();
        tickCounter = 0;
        firstCheck = true;
        WindowsNotification.sendNotification(NotificationMod.MOD_NAME, "Connected to server");
    }

    @SubscribeEvent
    public void onClientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        currentPlayers.clear();
        firstCheck = true;
        WindowsNotification.sendNotification(NotificationMod.MOD_NAME, "Disconnected from server");
    }

    // -------------------------------------------------------------------------
    // Chat event  (type 0 = regular player chat; type 1 = system; type 2 = action bar)
    // We notify for both type-0 and type-1 so that vanilla join/leave messages
    // that arrive via the chat channel are not silently dropped.
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        // Skip action-bar messages (type 2) – they are not real chat.
        if (event.getType() == 2) {
            return;
        }
        String message = event.getMessage().getUnformattedText();
        WindowsNotification.sendNotification("Minecraft Chat", message);
    }

    // -------------------------------------------------------------------------
    // Per-tick player-list comparison for other-player join/leave detection
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
            return;
        }

        // Build the current snapshot of players in the server.
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
                WindowsNotification.sendNotification("Player Joined", player + " joined the server");
                NotificationMod.LOGGER.info("Player joined: {}", player);
            }
        }

        // Detect players who just left.
        for (String player : currentPlayers) {
            if (!newPlayers.contains(player) && !player.equals(ownName)) {
                WindowsNotification.sendNotification("Player Left", player + " left the server");
                NotificationMod.LOGGER.info("Player left: {}", player);
            }
        }

        currentPlayers.clear();
        currentPlayers.addAll(newPlayers);
    }
}
