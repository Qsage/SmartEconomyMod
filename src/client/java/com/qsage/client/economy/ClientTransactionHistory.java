package com.qsage.client.economy;

import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.transaction.network.TransactionHistoryRequestPayload;
import com.qsage.economy.transaction.network.TransactionHistorySnapshotPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ClientTransactionHistory {

    private static final List<Transaction> transactions =
            new ArrayList<>();

    private static final Set<UUID> loadedIds =
            new HashSet<>();

    private static int currentPage = -1;
    private static int requestedPage = -1;

    private static boolean hasNextPage = true;
    private static boolean loading = false;

    private ClientTransactionHistory() {
    }

    public static void open() {
        clear();
        requestPage(0);
    }

    public static void requestPage(int page) {
        if (loading || page < 0 || page > 100_000) {
            return;
        }

        if (!ClientPlayNetworking.canSend(
                TransactionHistoryRequestPayload.TYPE
        )) {
            return;
        }

        loading = true;
        requestedPage = page;

        try {
            ClientPlayNetworking.send(
                    new TransactionHistoryRequestPayload(page)
            );
        } catch (RuntimeException e) {
            loading = false;
            requestedPage = -1;
            throw e;
        }
    }

    public static void requestNextPage() {
        if (loading || !hasNextPage) {
            return;
        }

        requestPage(currentPage + 1);
    }

    public static void update(
            TransactionHistorySnapshotPayload payload
    ) {
        // Не принимаем ответы на устаревшие запросы.
        if (!loading || payload.page() != requestedPage) {
            return;
        }

        for (Transaction transaction : payload.transactions()) {
            if (loadedIds.add(transaction.id())) {
                transactions.add(transaction);
            }
        }

        currentPage = payload.page();
        hasNextPage = payload.hasNextPage();

        requestedPage = -1;
        loading = false;
    }

    public static List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    public static int getCurrentPage() {
        return currentPage;
    }

    public static boolean hasNextPage() {
        return hasNextPage;
    }

    public static boolean isLoading() {
        return loading;
    }

    public static void clear() {
        transactions.clear();
        loadedIds.clear();

        currentPage = -1;
        requestedPage = -1;

        hasNextPage = true;
        loading = false;
    }
}