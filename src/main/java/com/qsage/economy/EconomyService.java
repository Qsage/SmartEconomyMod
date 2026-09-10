package com.qsage.economy;

import com.qsage.economy.money.MoneySink;
import com.qsage.economy.money.MoneySource;
import com.qsage.economy.storage.EconomyRepository;
import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.transaction.TransactionType;
import com.qsage.economy.wallet.Wallet;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

public final class EconomyService {

    private final EconomyRepository repository;
    private final Consumer<UUID> balanceChangeListener;

    public EconomyService(
            EconomyRepository repository,
            Consumer<UUID> balanceChangeListener
    ) {
        this.repository = repository;
        this.balanceChangeListener = balanceChangeListener;
    }

    // =========================================================
    // HELPER
    // =========================================================

    private void notifyBalanceChanged(
            UUID playerId
    ) {
        balanceChangeListener.accept(
                playerId
        );
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

            tx.saveWallet(
                    playerId,
                    wallet.deposit(amount)
            );

            tx.addMoneyCreated(amount);

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

        notifyBalanceChanged(playerId);
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

        notifyBalanceChanged(playerId);
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

        notifyBalanceChanged(from);
        notifyBalanceChanged(to);
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

        notifyBalanceChanged(playerId);
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
        notifyBalanceChanged(playerId);
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
        notifyBalanceChanged(playerId);
    }

    public long calculateWalletSupply() {
       return repository.calculateWalletSupply();
    }

    public boolean isConsistent(){
        long walletSupply = calculateWalletSupply();

        long moneySupply = getMoneySupply();

        return walletSupply == moneySupply;
    }

    public String getIntegrityReport() {

        long walletSupply =
                calculateWalletSupply();

        long created =
                getMoneyCreated();

        long destroyed =
                getMoneyDestroyed();

        long moneySupply =
                Math.subtractExact(
                        created,
                        destroyed
                );

        long difference =
                Math.subtractExact(
                        walletSupply,
                        moneySupply
                );

        return "=== Economy Integrity ===\n"
                + "Wallet supply: " + walletSupply + "\n"
                + "Created: " + created + "\n"
                + "Destroyed: " + destroyed + "\n"
                + "Supply: " + moneySupply + "\n"
                + "Difference: " + difference + "\n"
                + "Status: "
                + (difference == 0
                ? "OK"
                : "CORRUPTED");
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