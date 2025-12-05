package cn.encmys.ykdz.forest.hyphashop.api.price;

import org.jetbrains.annotations.NotNull;

public interface PricePair {
    @NotNull PriceInstance getBuy();

    void setBuy(@NotNull PriceInstance buy);
    
    @NotNull PriceInstance getSell();

    void setSell(@NotNull PriceInstance sell);

}
