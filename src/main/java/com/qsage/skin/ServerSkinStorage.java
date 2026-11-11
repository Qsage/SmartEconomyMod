
package com.qsage.skin;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.UUID;

public final class ServerSkinStorage {

    public static final int MAX_PNG_BYTES = 128 * 1024;

    private final Path directory;

    public ServerSkinStorage(Path worldDirectory) throws IOException {
        this.directory = worldDirectory.resolve("economy").resolve("skins");
        Files.createDirectories(directory);
    }

    public void save(UUID uuid, byte[] png) throws IOException {
        if (png == null || png.length == 0
                || png.length > MAX_PNG_BYTES) {
            throw new IOException("Invalid skin data size");
        }

        validatePng(png);

        Path target = directory.resolve(uuid + ".png");
        Path temp = Files.createTempFile(directory, "skin-", ".tmp");

        try {
            Files.write(temp, png);
            Files.move(
                    temp,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public byte[] load(UUID uuid) throws IOException {
        Path path = directory.resolve(uuid + ".png");

        if (!Files.isRegularFile(path)) {
            return null;
        }

        if (Files.size(path) > MAX_PNG_BYTES) {
            throw new IOException("Stored skin is too large");
        }

        return Files.readAllBytes(path);
    }

    private static void validatePng(byte[] png) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(png)
        )) {
            if (input == null) {
                throw new IOException("Cannot read image");
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IOException("Unsupported image format");
            }

            ImageReader reader = readers.next();

            try {
                if (!"png".equalsIgnoreCase(reader.getFormatName())) {
                    throw new IOException("Image is not PNG");
                }

                reader.setInput(input);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width != 64 || (height != 64 && height != 32)) {
                    throw new IOException("Invalid skin dimensions");
                }

                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new IOException("Cannot decode skin");
                }
            } finally {
                reader.dispose();
            }
        }
    }
}
