
package com.qsage.client.player;

import com.qsage.client.skin.ClientSkinRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientPlayerSkins {

    private static final Map<UUID, Identifier> CACHE =
            new HashMap<>();

    private ClientPlayerSkins() {
    }

    public static Identifier getSkin(UUID uuid) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.getConnection() != null) {
            PlayerInfo info = minecraft.getConnection()
                    .getPlayerInfo(uuid);

            if (info != null) {
                Identifier skin = info.getSkin().body().id();

                if (skin != null) {
                    CACHE.put(uuid, skin);
                    return skin;
                }
            }
        }

        Identifier serverSkin =
                ClientSkinRegistry.getTexture(uuid);

        if (serverSkin != null) {
            return serverSkin;
        }

        return CACHE.get(uuid);
    }

    public static void clear() {
        CACHE.clear();
        ClientSkinRegistry.clear();
    }
}
