package com.qsage.client.npc;

import com.qsage.economy.npc.EconomyNpcEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;

public class EconomyNpcRenderer extends VillagerRenderer {

    public EconomyNpcRenderer(
            EntityRendererProvider.Context context
    ) {
        super(context);
    }
}