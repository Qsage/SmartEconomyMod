package com.qsage.client.network;

import com.qsage.economy.market.model.TradeSide;
import com.qsage.economy.market.network.ExchangeRefreshPayload;
import com.qsage.economy.market.network.ExchangeTradePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;

public final class ExchangeNetworking {

    private ExchangeNetworking() {
    }

    public static void sendTrade(
            Identifier itemId,
            TradeSide side,
            long quantity
    ) {
        System.out.println(
                "Sending payload: "
                        + side
                        + " "
                        + itemId
                        + " x"
                        + quantity
        );

        ClientPlayNetworking.send(
                new ExchangeTradePayload(
                        itemId,
                        side,
                        quantity
                )
        );
    }

    public static void requestRefresh() {
        ClientPlayNetworking.send(
                new ExchangeRefreshPayload()
        );
    }
}