
package com.qsage.client.skin;

import java.nio.file.Path;
import org.jetbrains.annotations.Nullable;

public final class ClientSkinCachePaths {

    private static volatile Path skinsDirectory;

    private ClientSkinCachePaths() {
    }

    public static void setSkinsDirectory(Path directory) {
        skinsDirectory = directory.toAbsolutePath().normalize();
    }

    @Nullable
    public static Path getSkinsDirectory() {
        return skinsDirectory;
    }
}
