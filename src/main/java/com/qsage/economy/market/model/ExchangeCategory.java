package com.qsage.economy.market.model;

public enum ExchangeCategory {

    BUILDING_BLOCKS(0),
    TOOLS_AND_WEAPONS(1),
    MATERIALS(2),
    FOOD(3),
    REDSTONE(4);

    private final int order;

    ExchangeCategory(int order) {
        this.order = order;
    }

    public int order() {
        return order;
    }
}