
package com.qsage.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import com.qsage.SmartEconomy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientSkinRegistry {

    private static final int MAX_BYTES = 128 * 1024;

    private static final Map<UUID, Identifier> TEXTURES =
            new HashMap<>();

    private ClientSkinRegistry() {
    }

    public static void receive(UUID uuid, byte[] png) {
        if (uuid == null || png == null
                || png.length == 0 || png.length > MAX_BYTES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        // Этот метод должен вызываться на клиентском потоке.
        if (!minecraft.isSameThread()) {
            minecraft.execute(() -> receive(uuid, png));
            return;
        }

        NativeImage image = null;

        try {
            image = NativeImage.read(png);

            if (image.getWidth() != 64 || image.getHeight() != 64) {
                SmartEconomy.LOGGER.warn(
                        "Invalid skin dimensions for {}: {}x{}",
                        uuid,
                        image.getWidth(),
                        image.getHeight()
                );
                return;
            }

            Identifier textureId = SmartEconomy.id(
                    "player_skins/" + uuid.toString().replace("-", "")
            );

            DynamicTexture texture = new DynamicTexture(
                    () -> "SmartEconomy player skin " + uuid,
                    image
            );

            // DynamicTexture теперь владеет изображением.
            image = null;

            minecraft.getTextureManager().register(
                    textureId,
                    texture
            );

            TEXTURES.put(uuid, textureId);

            SmartEconomy.LOGGER.info(
                    "Registered server skin texture for {}",
                    uuid
            );

        } catch (IOException | RuntimeException e) {
            SmartEconomy.LOGGER.error(
                    "Failed to register server skin for " + uuid,
                    e
            );

        } finally {
            if (image != null) {
                image.close();
            }
        }
    }

    public static Identifier getTexture(UUID uuid) {
        return TEXTURES.get(uuid);
    }

    public static void clear() {
        Minecraft minecraft = Minecraft.getInstance();

        for (Identifier textureId : TEXTURES.values()) {
            minecraft.getTextureManager().release(textureId);
        }

        TEXTURES.clear();
    }
}
