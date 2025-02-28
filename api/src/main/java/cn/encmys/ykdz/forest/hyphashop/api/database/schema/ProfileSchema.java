package cn.encmys.ykdz.forest.hyphashop.api.database.schema;

import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public record ProfileSchema(@NotNull UUID ownerUUID, @NotNull Map<String, ShoppingMode> shoppingModes) {
    public static ProfileSchema of(Profile profile) {
        return new ProfileSchema(profile.getOwner().getUniqueId(), profile.getShoppingModes());
    }
}
