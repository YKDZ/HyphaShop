package cn.encmys.ykdz.forest.hyphashop.api.database.schema;

import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public record ProfileSchema(@NotNull UUID ownerUUID,
                            @NotNull Map<String, ShoppingMode> shoppingModes,
                            @NotNull ShopOrder cartOrder) {
    @Contract("_ -> new")
    public static @NotNull ProfileSchema of(@NotNull Profile profile) {
        return new ProfileSchema(profile.getOwner().getUniqueId(), profile.getShoppingModes(), profile.getCart().getOrder());
    }
}
