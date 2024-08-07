package cn.encmys.ykdz.forest.dailyshop.gui;

import cn.encmys.ykdz.forest.dailyshop.api.DailyShop;
import cn.encmys.ykdz.forest.dailyshop.api.config.ShopConfig;
import cn.encmys.ykdz.forest.dailyshop.api.config.record.shop.HistoryGUIRecord;
import cn.encmys.ykdz.forest.dailyshop.api.config.record.shop.IconRecord;
import cn.encmys.ykdz.forest.dailyshop.api.gui.ShopRelatedGUI;
import cn.encmys.ykdz.forest.dailyshop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.dailyshop.api.shop.Shop;
import cn.encmys.ykdz.forest.dailyshop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.dailyshop.api.shop.cashier.log.enums.SettlementLogType;
import cn.encmys.ykdz.forest.dailyshop.api.utils.LogUtils;
import cn.encmys.ykdz.forest.dailyshop.api.utils.TextUtils;
import cn.encmys.ykdz.forest.dailyshop.item.builder.NormalIconBuilder;
import cn.encmys.ykdz.forest.dailyshop.item.decorator.BaseItemDecoratorImpl;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HistoryGUI extends ShopRelatedGUI {
    public HistoryGUI(Shop shop) {
        super(shop);
    }

    @Override
    public void open(Player player) {
        HistoryGUIRecord record = ShopConfig.getHistoryGUIRecord(shop.getId());

        List<SettlementLog> logs;
        try {
            logs = DailyShop.DATABASE.queryLogs(shop.getId(), player.getUniqueId(), null, 365, 100, SettlementLogType.BUY_ALL_FROM, SettlementLogType.BUY_FROM, SettlementLogType.SELL_TO).get();
        } catch (InterruptedException | ExecutionException e) {
            LogUtils.warn("Error querying logs for " + shop.getId() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
        Gui gui = buildGUI(player);

//        for (SettlementLog log : logs) {
//            gui.addContent(SettlementLogUtils.toHistoryGuiItem(shop, log, player));
//        }

        Window window = Window.single()
                .setGui(gui)
                .setViewer(player)
                .setTitle(TextUtils.decorateText(record.title(), player, new HashMap<>() {{
                    put("shop-name", shop.getName());
                    put("shop-id", shop.getId());
                    put("player-name", player.getName());
                    put("player-uuid", player.getUniqueId().toString());
                }}))
                .setCloseHandlers(new ArrayList<>() {{
                    add(() -> getWindows().remove(player.getUniqueId()));
                }})
                .build();

        getWindows().put(player.getUniqueId(), window);
        window.open();
    }

    @Override
    public Gui buildGUI(Player player) {
        String shopId = shop.getId();
        HistoryGUIRecord record = ShopConfig.getHistoryGUIRecord(shopId);

        ScrollGui.Builder<Item> guiBuilder = ScrollGui.items()
                .setStructure(record.layout().toArray(new String[0]));

        if (record.scrollMode().isHorizontal()) {
            guiBuilder.addIngredient(markerIdentifier, Markers.CONTENT_LIST_SLOT_HORIZONTAL);
        } else {
            guiBuilder.addIngredient(markerIdentifier, Markers.CONTENT_LIST_SLOT_VERTICAL);
        }

        // 普通图标
        if (record.icons() != null) {
            for (IconRecord iconRecord : record.icons()) {
                guiBuilder.addIngredient(iconRecord.key(), buildNormalIcon(iconRecord, player));
            }
        }

        return guiBuilder.build();
    }

    @Override
    public Item buildNormalIcon(IconRecord record, Player player) {
        BaseItemDecorator decorator = BaseItemDecoratorImpl.get(record, true);
        if (decorator == null) {
            LogUtils.warn("Icon history-gui.icons." + record + " in shop " + shop.getId() + " has invalid base setting. Please check it.");
            return null;
        }
        return NormalIconBuilder.build(decorator, shop, player);
    }
}
