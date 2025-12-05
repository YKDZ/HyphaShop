package cn.encmys.ykdz.forest.hyphashop.price;

import cn.encmys.ykdz.forest.hyphashop.api.price.PriceInstance;
import cn.encmys.ykdz.forest.hyphashop.api.price.PricePair;
import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;

public class PricePairImpl implements PricePair {
    @Expose
    private PriceInstance buy;
    @Expose
    private PriceInstance sell;

    public PricePairImpl(@NotNull PriceInstance buy, @NotNull PriceInstance sell) {
        this.buy = buy;
        this.sell = sell;
    }

    @Override
    public @NotNull PriceInstance getBuy() {
        return buy;
    }

    @Override
    public void setBuy(@NotNull PriceInstance buy) {
        this.buy = buy;
    }

    @Override
    public @NotNull PriceInstance getSell() {
        return sell;
    }

    @Override
    public void setSell(@NotNull PriceInstance sell) {
        this.sell = sell;
    }
}
