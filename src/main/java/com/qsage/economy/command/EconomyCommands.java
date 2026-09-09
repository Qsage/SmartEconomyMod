package com.qsage.economy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.qsage.SmartEconomy;
import com.qsage.economy.money.MoneySink;
import com.qsage.economy.money.MoneySource;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import net.minecraft.network.chat.Component;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class EconomyCommands {

    private EconomyCommands() {
    }

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    registerBalance(dispatcher);
                    registerPay(dispatcher);
                    registerEconomy(dispatcher);
                }
        );
    }

    // =========================================================
    // /balance
    // /bal
    // =========================================================

    private static void registerBalance(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {

        dispatcher.register(
                Commands.literal("balance")
                        .executes(EconomyCommands::balance)
        );

        dispatcher.register(
                Commands.literal("bal")
                        .executes(EconomyCommands::balance)
        );
    }

    private static int balance(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource().getPlayerOrException();

        long available =
                SmartEconomy.economy()
                        .getAvailable(player.getUUID());

        long locked =
                SmartEconomy.economy()
                        .getLocked(player.getUUID());

        long total =
                SmartEconomy.economy()
                        .getTotal(player.getUUID());

        player.sendSystemMessage(
                Component.literal(
                        "Balance: "
                                + available
                                + " | Locked: "
                                + locked
                                + " | Total: "
                                + total
                )
        );

        return 1;
    }

    // =========================================================
    // /pay <player> <amount>
    // =========================================================

    private static void registerPay(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {

        dispatcher.register(
                Commands.literal("pay")
                        .then(
                                Commands.argument(
                                                "player",
                                                EntityArgument.player()
                                        )
                                        .then(
                                                Commands.argument(
                                                                "amount",
                                                                LongArgumentType.longArg(1)
                                                        )
                                                        .executes(
                                                                EconomyCommands::pay
                                                        )
                                        )
                        )
        );
    }

    private static int pay(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer sender =
                context.getSource()
                        .getPlayerOrException();

        ServerPlayer target =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        long amount =
                LongArgumentType.getLong(
                        context,
                        "amount"
                );

        try {

            SmartEconomy.economy().transfer(
                    sender.getUUID(),
                    target.getUUID(),
                    amount,
                    "Player payment"
            );

            sender.sendSystemMessage(
                    Component.literal(
                            "You sent "
                                    + amount
                                    + " to "
                                    + target.getGameProfile().name()
                    )
            );

            target.sendSystemMessage(
                    Component.literal(
                            "You received "
                                    + amount
                                    + " from "
                                    + sender.getGameProfile().name()
                    )
            );

            return 1;

        } catch (IllegalStateException e) {

            sender.sendSystemMessage(
                    Component.literal(
                            "Insufficient funds."
                    )
            );

            return 0;
        }
    }

    // =========================================================
    // /economy
    // =========================================================

    private static void registerEconomy(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {

        dispatcher.register(
                Commands.literal("economy")
                        .requires(
                                source ->
                                        source.permissions().hasPermission(Permissions.COMMANDS_OWNER)
                        )

                        // /economy info
                        .then(
                                Commands.literal("info")
                                        .executes(
                                                EconomyCommands::info
                                        )
                        )

                        // /economy give <player> <amount>
                        .then(
                                Commands.literal("give")
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                LongArgumentType.longArg(1)
                                                                        )
                                                                        .executes(
                                                                                EconomyCommands::give
                                                                        )
                                                        )
                                        )
                        )

                        // /economy take <player> <amount>
                        .then(
                                Commands.literal("take")
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                LongArgumentType.longArg(1)
                                                                        )
                                                                        .executes(
                                                                                EconomyCommands::take
                                                                        )
                                                        )
                                        )
                        )

                        // /economy player <player>
                        .then(
                                Commands.literal("player")
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .executes(
                                                                EconomyCommands::player
                                                        )
                                        )
                        )
        );
    }

    // =========================================================
    // /economy give
    // =========================================================

    private static int give(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer target =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        long amount =
                LongArgumentType.getLong(
                        context,
                        "amount"
                );

        SmartEconomy.economy().deposit(
                target.getUUID(),
                amount,
                MoneySource.ADMIN,
                "Admin command"
        );

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Added "
                                + amount
                                + " to "
                                + target.getGameProfile().name()
                ),
                true
        );

        target.sendSystemMessage(
                Component.literal(
                        "You received "
                                + amount
                                + " coins."
                )
        );

        return 1;
    }

    // =========================================================
    // /economy take
    // =========================================================

    private static int take(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer target =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        long amount =
                LongArgumentType.getLong(
                        context,
                        "amount"
                );

        try {

            SmartEconomy.economy().withdraw(
                    target.getUUID(),
                    amount,
                    MoneySink.ADMIN,
                    "Admin command"
            );

            context.getSource().sendSuccess(
                    () -> Component.literal(
                            "Removed "
                                    + amount
                                    + " from "
                                    + target.getGameProfile().name()
                    ),
                    true
            );

            target.sendSystemMessage(
                    Component.literal(
                            amount
                                    + " coins were removed from your balance."
                    )
            );

            return 1;

        } catch (IllegalStateException e) {

            context.getSource().sendFailure(
                    Component.literal(
                            "Player does not have enough funds."
                    )
            );

            return 0;
        }
    }

    // =========================================================
    // /economy info
    // =========================================================

    private static int info(
            CommandContext<CommandSourceStack> context
    ) {

        long created =
                SmartEconomy.economy()
                        .getMoneyCreated();

        long destroyed =
                SmartEconomy.economy()
                        .getMoneyDestroyed();

        long supply =
                SmartEconomy.economy()
                        .getMoneySupply();

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "=== Smart Economy ===\n"
                                + "Created: " + created + "\n"
                                + "Destroyed: " + destroyed + "\n"
                                + "Supply: " + supply
                ),
                false
        );

        return 1;
    }

    // =========================================================
    // /economy player <player>
    // =========================================================

    private static int player(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer target =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        long available =
                SmartEconomy.economy()
                        .getAvailable(target.getUUID());

        long locked =
                SmartEconomy.economy()
                        .getLocked(target.getUUID());

        long total =
                SmartEconomy.economy()
                        .getTotal(target.getUUID());

        context.getSource().sendSuccess(
                () -> Component.literal(
                        target.getGameProfile().name()
                                + "\n"
                                + "Available: " + available
                                + "\n"
                                + "Locked: " + locked
                                + "\n"
                                + "Total: " + total
                ),
                false
        );

        return 1;
    }
}