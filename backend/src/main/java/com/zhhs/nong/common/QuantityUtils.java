package com.zhhs.nong.common;

public final class QuantityUtils {

    private QuantityUtils() {
    }

    public static int clampToAvailableStock(int qty, int stock) {
        if (qty < 1) {
            return 1;
        }
        if (stock > 0) {
            return Math.min(qty, stock);
        }
        return qty;
    }
}
