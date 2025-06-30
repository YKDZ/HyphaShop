package cn.encmys.ykdz.forest.hyphashop.listener;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderListener implements Listener {
    @EventHandler
    public void onItemsAdderLoad(@NotNull ItemsAdderLoadDataEvent e) {
        HyphaShop.INSTANCE.init();
    }
}
