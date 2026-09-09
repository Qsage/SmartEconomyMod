package com.qsage.economy.wallet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Wallet(
        long available,
        long locked
) {

    public static final Codec<Wallet> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.LONG
                                    .fieldOf("available")
                                    .forGetter(Wallet::available),

                            Codec.LONG
                                    .fieldOf("locked")
                                    .forGetter(Wallet::locked)
                    ).apply(instance, Wallet::new)
            );

    public Wallet {
        if (available < 0) {
            throw new IllegalArgumentException(
                    "Available balance cannot be negative"
            );
        }

        if (locked < 0) {
            throw new IllegalArgumentException(
                    "Locked balance cannot be negative"
            );
        }
    }

    public static Wallet empty() {
        return new Wallet(0, 0);
    }

    public long total() {
        return Math.addExact(
                available,
                locked
        );
    }

    public Wallet deposit(long amount) {
        validatePositive(amount);

        return new Wallet(
                Math.addExact(available, amount),
                locked
        );
    }

    public Wallet withdraw(long amount) {
        validatePositive(amount);

        if (available < amount) {
            throw new IllegalStateException(
                    "Insufficient funds"
            );
        }

        return new Wallet(
                available - amount,
                locked
        );
    }

    public Wallet lock(long amount) {
        validatePositive(amount);

        if (available < amount) {
            throw new IllegalStateException(
                    "Insufficient funds"
            );
        }

        return new Wallet(
                available - amount,
                Math.addExact(locked, amount)
        );
    }

    public Wallet unlock(long amount) {
        validatePositive(amount);

        if (locked < amount) {
            throw new IllegalStateException(
                    "Insufficient locked funds"
            );
        }

        return new Wallet(
                Math.addExact(available, amount),
                locked - amount
        );
    }

    public Wallet spendLocked(long amount) {
        validatePositive(amount);

        if (locked < amount) {
            throw new IllegalStateException(
                    "Insufficient locked funds"
            );
        }

        return new Wallet(
                available,
                locked - amount
        );
    }

    private static void validatePositive(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }
}