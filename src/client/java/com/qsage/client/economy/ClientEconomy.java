package com.qsage.client.economy;

public final class ClientEconomy {
    private static long available;
    private static long locked;

    private ClientEconomy() {
    }

    public static long getAvailable() {
        return available;
    }

    public static long getLocked() {
        return locked;
    }

    public static long getTotal(){
        return Math.addExact(
                available,
                locked
        );
    }

    public static void setBalance(
            long newAvailable,
            long newLocked)
    {
        if (newAvailable < 0) {
            throw new IllegalArgumentException(
                    "Available balance cannot be negative"
            );
        }

        if (newLocked < 0) {
            throw new IllegalArgumentException(
                    "Locked balance cannot be negative"
            );
        }
        available = newAvailable;
        locked = newLocked;
    }

    public  static void clear(){
        available = 0;
        locked = 0;
    }
}
