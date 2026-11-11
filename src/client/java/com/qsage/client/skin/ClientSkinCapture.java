
package com.qsage.client.skin;

import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientSkinCapture {

    private static final int MAX_SKIN_BYTES = 128 * 1024;

    private ClientSkinCapture() {
    }

    public static byte[] capture(Identifier skinId) {
        Path directory = ClientSkinCachePaths.getSkinsDirectory();

        if (directory == null || skinId == null) {
            return null;
        }

        if (!"minecraft".equals(skinId.getNamespace())) {
            return null;
        }

        String path = skinId.getPath();

        if (!path.startsWith("skins/")) {
            return null;
        }

        String hash = path.substring("skins/".length());

        if (!hash.matches("[0-9a-f]{40}")) {
            return null;
        }

        Path file = directory
                .resolve(hash.substring(0, 2))
                .resolve(hash);

        try {
            if (!Files.isRegularFile(file)) {
                return null;
            }

            long size = Files.size(file);

            if (size <= 0 || size > MAX_SKIN_BYTES) {
                return null;
            }

            byte[] data = Files.readAllBytes(file);

            // Проверяем сигнатуру PNG.
            byte[] signature = {
                    (byte) 0x89, 0x50, 0x4E, 0x47,
                    0x0D, 0x0A, 0x1A, 0x0A
            };

            if (data.length < signature.length) {
                return null;
            }

            for (int i = 0; i < signature.length; i++) {
                if (data[i] != signature[i]) {
                    return null;
                }
            }

            return data;

        } catch (IOException e) {
            return null;
        }
    }
}
