package com.qsage.economy;

import com.qsage.SmartEconomy;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class EconomyAttachments {

    public static final AttachmentType<EconomyState> ECONOMY_STATE =
            AttachmentRegistry.create(
                    SmartEconomy.id("economy_state"),
                    builder -> builder
                            .initializer(EconomyState::empty)
                            .persistent(EconomyState.CODEC)
            );

    private EconomyAttachments() {
    }
}