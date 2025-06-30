package cn.encmys.ykdz.forest.hyphashop.item;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaSkullUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkullItem extends VanillaItem implements BaseItem {
    private final @NotNull String data;

    public SkullItem(@NotNull String data) {
        super(Material.PLAYER_HEAD);
        this.data = data;
    }

    @Override
    public @NotNull ItemStack build(@Nullable Player player) {
        return HyphaSkullUtils.getSkullFromData(getData());
    }

    public @NotNull String getData() {
        return data;
    }
}
