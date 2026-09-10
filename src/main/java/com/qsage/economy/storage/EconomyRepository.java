package com.qsage.economy.storage;

import com.qsage.economy.wallet.Wallet;

import java.util.UUID;
import java.util.function.Consumer;

public interface EconomyRepository {

    Wallet getWallet(UUID playerId);

    long getMoneyCreated();

    long getMoneyDestroyed();

    long calculateWalletSupply();

    void transaction(
            Consumer<EconomyTransaction> action
    );
}