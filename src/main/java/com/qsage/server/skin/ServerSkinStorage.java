package com.qsage.server.skin;

import net.minecraft.server.MinecraftServer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ServerSkinStorage {

    private static final int MAX_BYTES = 128 * 1024;

    private ServerSkinStorage() {
    }

    public static boolean save(
            Path economyDirectory,
            UUID uuid,
            byte[] png
    ) {
        if (png == null || png.length == 0
                || png.length > MAX_BYTES) {
            return false;
        }

        try {
            // Проверяем сигнатуру PNG.
            byte[] signature = {
                    (byte) 0x89, 0x50, 0x4E, 0x47,
                    0x0D, 0x0A, 0x1A, 0x0A
            };

            if (png.length < 24) {
                return false;
            }

            for (int i = 0; i < signature.length; i++) {
                if (png[i] != signature[i]) {
                    return false;
                }
            }

            // Читаем размеры из заголовка PNG до декодирования.
            int width = readInt(png, 16);
            int height = readInt(png, 20);

            if (width != 64 || (height != 64 && height != 32)) {
                return false;
            }

            BufferedImage image = ImageIO.read(
                    new ByteArrayInputStream(png)
            );

            if (image == null
                    || image.getWidth() != 64
                    || image.getHeight() != height) {
                return false;
            }

            // Пока принимаем только современные скины.
            // Для старых 64x32 позже добавим преобразование.
            if (height != 64) {
                return false;
            }

            Path directory = economyDirectory.resolve("skins");
            Files.createDirectories(directory);

            Path target = directory.resolve(uuid + ".png");
            Path temporary = Files.createTempFile(
                    directory, "skin-", ".tmp"
            );

            try {
                Files.write(temporary, png);
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } finally {
                Files.deleteIfExists(temporary);
            }

            return true;

        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    public static byte[] load(
            Path economyDirectory,
            UUID uuid
    ) throws IOException {

        Path file = economyDirectory
                .resolve("skins")
                .resolve(uuid + ".png");

        if (!Files.isRegularFile(file)) {
            return null;
        }

        long size = Files.size(file);

        if (size <= 0 || size > MAX_BYTES) {
            return null;
        }

        return Files.readAllBytes(file);
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }
}
