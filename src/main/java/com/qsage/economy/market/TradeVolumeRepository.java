package com.qsage.economy.market;

import com.qsage.economy.market.model.TradeVolume;
import com.qsage.economy.market.pricing.VolumeWindow;
import net.minecraft.resources.Identifier;

import java.time.Instant;

public interface TradeVolumeRepository {

    TradeVolume getVolume(
            Identifier itemId,
            Instant from,
            Instant to
    );

    default TradeVolume getVolume(
            Identifier itemId,
            VolumeWindow window,
            Instant now
    ) {
        Instant from =
                now.minusMillis(window.durationMillis());

        return getVolume(
                itemId,
                from,
                now
        );
    }
}