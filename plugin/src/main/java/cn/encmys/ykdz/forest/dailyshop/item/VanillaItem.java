package cn.encmys.ykdz.forest.dailyshop.item;

import cn.encmys.ykdz.forest.dailyshop.api.item.BaseItem;
import cn.encmys.ykdz.forest.dailyshop.api.item.enums.BaseItemType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaItem implements BaseItem {
    private final Material material;

    public VanillaItem(@NotNull Material material) {
        this.material = material;
    }

    @Override
    public ItemStack build(@Nullable Player player) {
        return new ItemStack(getMaterial());
    }

    @Override
    public String getDisplayName() {
        // TODO 使用新接口实现
//        String name = DailyShop.ITEMSLANG_API.translate(getMaterial(), Config.language);
//        if (name == null) {
//            name = "<red>Name not find";
//        }
//        return DailyShop.ADVENTURE_MANAGER.legacyToMiniMessage(name);
        return material.toString();
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack item) {
        return getMaterial() == item.getType();
    }

    @Override
    public boolean isExist() {
        return true;
    }

    @Override
    public BaseItemType getItemType() {
        return BaseItemType.VANILLA;
    }

    public Material getMaterial() {
        return material;
    }
}
