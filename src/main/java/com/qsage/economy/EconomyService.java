package com.qsage.economy;

import com.qsage.economy.money.MoneySink;
import com.qsage.economy.money.MoneySource;
import com.qsage.economy.storage.EconomyRepository;
import com.qsage.economy.storage.EconomyTransaction;
import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.transaction.TransactionType;
import com.qsage.economy.wallet.Wallet;

import java.time.Instant;
import java.util.UUID;

public final class EconomyService {

    private final EconomyRepository repository;

    public EconomyService(EconomyRepository repository) {
        this.repository = repository;
    }

    // =========================================================
    // READ
    // =========================================================

    public Wallet getWallet(UUID playerId) {
        return repository.getWallet(playerId);
    }

    public long getAvailable(UUID playerId) {
        return getWallet(playerId).available();
    }

    public long getLocked(UUID playerId) {
        return getWallet(playerId).locked();
    }

    public long getTotal(UUID playerId) {
        return getWallet(playerId).total();
    }

    public long getMoneyCreated() {
        return repository.getMoneyCreated();
    }

    public long getMoneyDestroyed() {
        return repository.getMoneyDestroyed();
    }

    public long getMoneySupply() {
        return Math.subtractExact(
                getMoneyCreated(),
                getMoneyDestroyed()
        );
    }

    // =========================================================
    // DEPOSIT
    // =========================================================

    public void deposit(
            UUID playerId,
            long amount,
            MoneySource source,
            String reason
    ) {
        validateAmount(amount);

        repository.transaction(tx -> {

            Wallet wallet =
                    tx.getWallet(playerId);

            Wallet updated =
                    wallet.deposit(amount);

            tx.saveWallet(
                    playerId,
                    updated
            );

            tx.addMoneyCreated(
                    amount
            );

            tx.saveTransaction(
                    new Transaction(
                            UUID.randomUUID(),
                            Instant.now(),
                            TransactionType.DEPOSIT,
                            playerId,
                            null,
                            amount,
                            source + ": " + reason
                    )
            );
        });
    }

    // =========================================================
    // WITHDRAW
    // =========================================================

    public void withdraw(
            UUID playerId,
            long amount,
            MoneySink sink,
            String reason
    ) {
        validateAmount(amount);

        repository.transaction(tx -> {

            Wallet wallet =
                    tx.getWallet(playerId);

            Wallet updated =
                    wallet.withdraw(amount);

            tx.saveWallet(
                    playerId,
                    updated
            );

            tx.addMoneyDestroyed(
                    amount
            );

            tx.saveTransaction(
                    new Transaction(
                            UUID.randomUUID(),
                            Instant.now(),
                            TransactionType.WITHDRAW,
                            playerId,
                            null,
                            amount,
                            sink + ": " + reason
                    )
            );
        });
    }

    // =========================================================
    // TRANSFER
    // =========================================================

    public void transfer(
            UUID from,
            UUID to,
            long amount,
            String reason
    ) {
        validateAmount(amount);

        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "Cannot transfer to yourself"
            );
        }

        repository.transaction(tx -> {

            Wallet sender =
                    tx.getWallet(from);

            Wallet receiver =
                    tx.getWallet(to);

            if (sender.available() < amount) {
                throw new IllegalStateException(
                        "Insufficient funds"
                );
            }

            tx.saveWallet(
                    from,
                    sender.withdraw(amount)
            );

            tx.saveWallet(
                    to,
                    receiver.deposit(amount)
            );

            tx.saveTransaction(
                    new Transaction(
                            UUID.randomUUID(),
                            Instant.now(),
                            TransactionType.TRANSFER,
                            from,
                            to,
                            amount,
                            reason
                    )
            );
        });
    }

    // =========================================================
    // LOCK
    // =========================================================

    public void lock(
            UUID playerId,
            long amount,
            String reason
    ) {
        validateAmount(amount);

        repository.transaction(tx -> {

            Wallet wallet =
                    tx.getWallet(playerId);

            tx.saveWallet(
                    playerId,
                    wallet.lock(amount)
            );

            tx.saveTransaction(
                    new Transaction(
                            UUID.randomUUID(),
                            Instant.now(),
                            TransactionType.LOCK,
                            playerId,
                            null,
                            amount,
                            reason
                    )
            );
        });
    }

    // =========================================================
    // UNLOCK
    // =========================================================

    public void unlock(
            UUID playerId,
            long amount,
            String reason
    ) {
        validateAmount(amount);

        repository.transaction(tx -> {

            Wallet wallet =
                    tx.getWallet(playerId);

            tx.saveWallet(
                    playerId,
                    wallet.unlock(amount)
            );

            tx.saveTransaction(
                    new Transaction(
                            UUID.randomUUID(),
                            Instant.now(),
                            TransactionType.UNLOCK,
                            playerId,
                            null,
                            amount,
                            reason
                    )
            );
        });
    }

    // =========================================================
    // BURN LOCKED
    // =========================================================

    public void burnLocked(
            UUID playerId,
            long amount,
            MoneySink sink,
            String reason
    ) {
        validateAmount(amount);

        repository.transaction(tx -> {

            Wallet wallet =
                    tx.getWallet(playerId);

            tx.saveWallet(
                    playerId,
                    wallet.spendLocked(amount)
            );

            tx.addMoneyDestroyed(
                    amount
            );

            tx.saveTransaction(
                    new Transaction(
                            UUID.randomUUID(),
                            Instant.now(),
                            TransactionType.BURN,
                            playerId,
                            null,
                            amount,
                            sink + ": " + reason
                    )
            );
        });
    }

    // =========================================================

    private static void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }
}