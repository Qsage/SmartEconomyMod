package com.qsage.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qsage.economy.wallet.Wallet;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record EconomyState(
        Map<UUID, Wallet> wallets,
        long moneyCreated,
        long moneyDestroyed
) {

    public static final Codec<EconomyState> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.unboundedMap(
                                            Codec.STRING.xmap(
                                                    UUID::fromString,
                                                    UUID::toString
                                            ),
                                            Wallet.CODEC
                                    ).fieldOf("wallets")
                                    .forGetter(EconomyState::wallets),

                            Codec.LONG
                                    .fieldOf("money_created")
                                    .forGetter(EconomyState::moneyCreated),

                            Codec.LONG
                                    .fieldOf("money_destroyed")
                                    .forGetter(EconomyState::moneyDestroyed)
                    ).apply(instance, EconomyState::new)
            );

    public static EconomyState empty() {
        return new EconomyState(
                Map.of(),
                0,
                0
        );
    }

    public Wallet getWallet(UUID playerId) {
        return wallets.getOrDefault(
                playerId,
                Wallet.empty()
        );
    }

    public EconomyState setWallet(
            UUID playerId,
            Wallet wallet
    ) {
        Map<UUID, Wallet> newWallets =
                new java.util.HashMap<>(wallets);

        newWallets.put(playerId, wallet);

        return new EconomyState(
                Map.copyOf(newWallets),
                moneyCreated,
                moneyDestroyed
        );
    }

    public EconomyState addCreated(long amount) {
        return new EconomyState(
                wallets,
                Math.addExact(moneyCreated, amount),
                moneyDestroyed
        );
    }

    public EconomyState addDestroyed(long amount) {
        return new EconomyState(
                wallets,
                moneyCreated,
                Math.addExact(moneyDestroyed, amount)
        );
    }

    public long moneySupply() {
        return moneyCreated - moneyDestroyed;
    }
}