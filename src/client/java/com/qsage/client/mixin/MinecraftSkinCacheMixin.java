
package com.qsage.client.mixin;

import com.qsage.client.skin.ClientSkinCachePaths;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Minecraft.class)
public abstract class MinecraftSkinCacheMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/SkinManager;<init>(Ljava/nio/file/Path;Lnet/minecraft/server/Services;Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;Ljava/util/concurrent/Executor;)V"
            ),
            index = 0
    )
    private Path smartEconomy$captureSkinsDirectory(Path directory) {
        ClientSkinCachePaths.setSkinsDirectory(directory);

        System.out.println(
                "[SmartEconomy] Minecraft skins directory: " + directory
        );

        return directory;
    }
}
