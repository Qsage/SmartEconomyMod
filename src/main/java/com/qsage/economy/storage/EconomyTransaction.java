package com.qsage.economy.storage;

import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.wallet.Wallet;

import java.util.UUID;

public interface EconomyTransaction {

    Wallet getWallet(UUID playerId);

    void saveWallet(
            UUID playerId,
            Wallet wallet
    );

    long getMoneyCreated();

    long getMoneyDestroyed();

    void addMoneyCreated(
            long amount
    );

    void addMoneyDestroyed(
            long amount
    );

    void saveTransaction(
            Transaction transaction
    );
}