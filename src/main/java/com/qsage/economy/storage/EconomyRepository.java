package com.qsage.economy.storage;


import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.wallet.Wallet;

import java.util.List;
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

    List<Transaction> getTransactionHistory(
            UUID playerId,
            int limit,
            int offset
    );

}