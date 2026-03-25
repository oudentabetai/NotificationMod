package com.notificationmod;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

/**
 * In-game configuration screen for NotificationMod.
 * <p>
 * Displays all properties from the {@value NotificationConfig#CATEGORY} category of the
 * mod's config file as interactive elements (cycle buttons for enum-valued options).
 * Changes are saved to disk and synced into the static {@link NotificationConfig} fields
 * automatically when the player clicks "Done".
 */
public class NotificationGuiConfig extends GuiConfig {

    public NotificationGuiConfig(GuiScreen parentScreen) {
        super(parentScreen, getConfigElements(), NotificationMod.MOD_ID, false, false,
                NotificationMod.MOD_NAME + " Configuration");
    }

    @SuppressWarnings("unchecked")
    private static List<IConfigElement> getConfigElements() {
        return new ConfigElement(NotificationConfig.configuration.getCategory(NotificationConfig.CATEGORY))
                .getChildElements();
    }
}
