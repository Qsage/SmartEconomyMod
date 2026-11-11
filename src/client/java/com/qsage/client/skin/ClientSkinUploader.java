
package com.qsage.client.skin;

import com.qsage.SmartEconomy;
import com.qsage.economy.player.network.SkinUploadPayload;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public final class ClientSkinUploader {

    private static final int CHECK_INTERVAL_TICKS = 100;

    private static Object lastConnection;
    private static byte[] lastUploadedHash;

    private static Identifier lastObservedSkin;
    private static int retryTicks;

    private ClientSkinUploader() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(
                ClientSkinUploader::tick
        );
    }

    private static byte[] calculateHash(byte[] png) {
        try {
            return MessageDigest
                    .getInstance("SHA-256")
                    .digest(png);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 unavailable",
                    e
            );
        }
    }

    private static void reset() {
        lastConnection = null;
        lastUploadedHash = null;
        lastObservedSkin = null;
        retryTicks = 0;
    }

    private static void tick(Minecraft minecraft) {

        if (minecraft.getConnection() == null
                || minecraft.player == null) {

            reset();
            return;
        }

        Object connection = minecraft.getConnection();

        if (lastConnection != connection) {
            lastConnection = connection;
            lastUploadedHash = null;
            lastObservedSkin = null;
            retryTicks = 0;
        }

        if (retryTicks > 0) {
            retryTicks--;
            return;
        }

        retryTicks = CHECK_INTERVAL_TICKS;

        if (!ClientPlayNetworking.canSend(
                SkinUploadPayload.TYPE
        )) {
            return;
        }

        UUID uuid = minecraft.player.getUUID();

        PlayerInfo info = minecraft.getConnection()
                .getPlayerInfo(uuid);

        if (info == null) {
            return;
        }

        Identifier skinId = info.getSkin().body().id();

        if (skinId == null) {
            return;
        }

        if (!skinId.equals(lastObservedSkin)) {
            SmartEconomy.LOGGER.info(
                    "Observed player skin: {}",
                    skinId
            );

            lastObservedSkin = skinId;
        }

        byte[] png = ClientSkinCapture.capture(skinId);

        if (png == null || png.length == 0) {
            return;
        }

        byte[] currentHash = calculateHash(png);

        if (Arrays.equals(currentHash, lastUploadedHash)) {
            return;
        }

        ClientPlayNetworking.send(
                new SkinUploadPayload(png)
        );

        lastUploadedHash = currentHash;

        SmartEconomy.LOGGER.info(
                "Uploaded player skin {} ({} bytes)",
                skinId,
                png.length
        );
    }
}
