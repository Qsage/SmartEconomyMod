package com.qsage.economy.npc;

import com.qsage.SmartEconomy;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.Villager;

public final class EconomyNpcRegistry {

    public static final ResourceKey<EntityType<?>> ECONOMY_NPC_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    SmartEconomy.id("economy_npc")
            );

    public static final EntityType<EconomyNpcEntity> ECONOMY_NPC =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    ECONOMY_NPC_KEY,
                    EntityType.Builder
                            .of(EconomyNpcEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(10)
                            .build(ECONOMY_NPC_KEY)
            );

    private EconomyNpcRegistry() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                ECONOMY_NPC,
                Villager.createAttributes()
        );

        SmartEconomy.LOGGER.info(
                "Registered Economy NPC"
        );
    }
}