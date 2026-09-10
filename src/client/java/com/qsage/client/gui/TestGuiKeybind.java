package com.qsage.client.gui;

import com.qsage.SmartEconomy;
import com.qsage.client.gui.screen.TestEconomyScreen;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import com.mojang.blaze3d.platform.InputConstants;

public final class TestGuiKeybind {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            SmartEconomy.MOD_ID,
                            "test_gui"
                    )
            );

    private static final KeyMapping OPEN_GUI =
            KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "key.smart_economy.open_test_gui",
                            InputConstants.Type.KEYSYM,
                            InputConstants.KEY_M,
                            CATEGORY
                    )
            );

    private TestGuiKeybind() {
    }

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {

                    while (OPEN_GUI.consumeClick()) {

                        if (client.player == null) {
                            return;
                        }

                        Minecraft.getInstance().gui.setScreen(
                                new TestEconomyScreen()
                        );
                    }
                }
        );
    }
}