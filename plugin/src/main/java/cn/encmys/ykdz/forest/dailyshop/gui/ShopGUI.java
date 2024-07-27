package cn.encmys.ykdz.forest.dailyshop.gui;

import cn.encmys.ykdz.forest.dailyshop.api.DailyShop;
import cn.encmys.ykdz.forest.dailyshop.api.config.ShopConfig;
import cn.encmys.ykdz.forest.dailyshop.api.config.record.shop.IconRecord;
import cn.encmys.ykdz.forest.dailyshop.api.config.record.shop.ShopGUIRecord;
import cn.encmys.ykdz.forest.dailyshop.api.gui.ShopRelatedGUI;
import cn.encmys.ykdz.forest.dailyshop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.dailyshop.api.product.Product;
import cn.encmys.ykdz.forest.dailyshop.api.shop.Shop;
import cn.encmys.ykdz.forest.dailyshop.api.utils.LogUtils;
import cn.encmys.ykdz.forest.dailyshop.builder.NormalIconBuilder;
import cn.encmys.ykdz.forest.dailyshop.builder.ProductIconBuilder;
import cn.encmys.ykdz.forest.dailyshop.hook.PlaceholderAPIHook;
import cn.encmys.ykdz.forest.dailyshop.item.decorator.BaseItemDecoratorImpl;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI extends ShopRelatedGUI {
    public ShopGUI(Shop shop) {
        super(shop);
    }

    @Override
    public ScrollGui.@NotNull Builder<Item> buildGUIBuilder(@Nullable Player player) {
        String shopId = shop.getId();
        List<String> listedProduct = shop.getShopStocker().getListedProducts();
        ShopGUIRecord record = ShopConfig.getShopGUIRecord(shopId);

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
                guiBuilder.addIngredient(iconRecord.key(), buildNormalIcon(iconRecord));
            }
        }

        // 商品图标
        for (String productId : listedProduct) {
            Product product = DailyShop.PRODUCT_FACTORY.getProduct(productId);
            if (product == null) {
                continue;
            }
            guiBuilder.addContent(ProductIconBuilder.build(product.getIconDecorator(), player, shopId, product));
        }

        return guiBuilder;
    }

    @Override
    public void open(@NotNull Player player) {
        String title = PlaceholderAPIHook.isHooked() ? PlaceholderAPI.setPlaceholders(player, ShopConfig.getShopGUITitle(shop.getId())) : ShopConfig.getShopGUITitle(shop.getId());
        Window window = Window.single()
                .setGui(buildGUIBuilder(player))
                .setTitle(title)
                .setCloseHandlers(new ArrayList<>() {{
                    add(() -> getWindows().remove(player.getUniqueId()));
                }})
                .build(player);

        window.open();

        getWindows().put(player.getUniqueId(), window);
    }

    @Override
    public Item buildNormalIcon(IconRecord record) {
        BaseItemDecorator decorator = BaseItemDecoratorImpl.get(record, true);
        if (decorator == null) {
            LogUtils.warn("Icon shop-gui.icons." + record + " in shop " + shop.getId() + " has invalid base setting. Please check it.");
            return null;
        }
        return NormalIconBuilder.build(decorator, this);
    }
}
