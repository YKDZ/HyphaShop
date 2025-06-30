package cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount;

public record AmountPair(int amount, int stack) {
    public int totalAmount() {
        return amount * stack;
    }
}
