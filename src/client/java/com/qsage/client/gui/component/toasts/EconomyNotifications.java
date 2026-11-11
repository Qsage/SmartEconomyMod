
package com.qsage.client.gui.component.toasts;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class EconomyNotifications {

    private EconomyNotifications() {
    }

    public static void show(
            EconomyToast.Type type,
            Component title,
            Component message
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.getInstance().gui.toastManager().addToast(
                new EconomyToast(type, title, message)
        );
    }

    public static void success(
            Component title,
            Component message
    ) {
        show(EconomyToast.Type.SUCCESS, title, message);
    }

    public static void error(
            Component title,
            Component message
    ) {
        show(EconomyToast.Type.ERROR, title, message);
    }

    public static void info(
            Component title,
            Component message
    ) {
        show(EconomyToast.Type.INFO, title, message);
    }
}
