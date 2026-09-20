package com.qsage.economy.market;

import com.qsage.economy.market.model.ExchangeQuote;
import com.qsage.economy.market.model.ExchangeTrade;
import com.qsage.economy.market.model.TradeVolume;
import net.minecraft.resources.Identifier;

import java.time.Instant;
import java.util.List;

public interface ExchangeRepository {

    ExchangeQuote getQuote(Identifier itemId);

    List<ExchangeQuote> getQuotes();

    void saveQuote(ExchangeQuote quote);

    void saveTrade(ExchangeTrade trade);

    TradeVolume getVolume(
            Identifier itemId,
            Instant from,
            Instant to
    );
}