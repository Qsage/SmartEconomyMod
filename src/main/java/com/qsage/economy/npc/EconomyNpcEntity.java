package com.qsage.economy.npc;

import com.qsage.economy.npc.dialogue.NpcDialogueService;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.minecraft.server.level.ServerPlayer;

public class EconomyNpcEntity extends Villager {

    public EconomyNpcEntity(
            EntityType<? extends Villager> type,
            Level level
    ) {
        super(type, level);

        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    private static final double LOOK_DISTANCE = 6.0;
    private static final float MAX_HEAD_ROTATION = 60.0f;

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            getNavigation().stop();

            setDeltaMovement(
                    0.0,
                    getDeltaMovement().y,
                    0.0
            );
        }

        if (level().isClientSide()) {
            return;
        }

        Player player = level().getNearestPlayer(
                this,
                LOOK_DISTANCE
        );

        if (player == null) {
            return;
        }

        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();

        float targetYaw = (float) (
                Math.atan2(dz, dx) * (180.0 / Math.PI)
        ) - 90.0f;

        float bodyYaw = getYRot();

        float relativeYaw = net.minecraft.util.Mth.wrapDegrees(
                targetYaw - bodyYaw
        );

        relativeYaw = net.minecraft.util.Mth.clamp(
                relativeYaw,
                -MAX_HEAD_ROTATION,
                MAX_HEAD_ROTATION
        );

        setYHeadRot(bodyYaw + relativeYaw);
    }

    @Override
    public InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NpcDialogueService.open(serverPlayer, this);
        }

        return InteractionResult.SUCCESS;
    }
}