package cn.encmys.ykdz.forest.dailyshop.item.builder;

import cn.encmys.ykdz.forest.dailyshop.api.DailyShop;
import cn.encmys.ykdz.forest.dailyshop.api.config.CartGUIConfig;
import cn.encmys.ykdz.forest.dailyshop.api.config.MessageConfig;
import cn.encmys.ykdz.forest.dailyshop.api.config.record.misc.IconRecord;
import cn.encmys.ykdz.forest.dailyshop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.dailyshop.api.item.decorator.enums.PropertyType;
import cn.encmys.ykdz.forest.dailyshop.api.profile.Profile;
import cn.encmys.ykdz.forest.dailyshop.api.profile.enums.GUIType;
import cn.encmys.ykdz.forest.dailyshop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.dailyshop.api.shop.Shop;
import cn.encmys.ykdz.forest.dailyshop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.dailyshop.api.shop.order.enums.SettlementResult;
import cn.encmys.ykdz.forest.dailyshop.api.utils.CommandUtils;
import cn.encmys.ykdz.forest.dailyshop.api.utils.LogUtils;
import cn.encmys.ykdz.forest.dailyshop.api.utils.PlayerUtils;
import cn.encmys.ykdz.forest.dailyshop.api.utils.TextUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.Item;

import java.util.*;

public class NormalIconBuilder {
    public static @NotNull Item build(@NotNull IconRecord record, @Nullable Shop shop, @Nullable Map<String, String> additionalVars, @Nullable Map<String, List<String>> additionalListVars) {
        BaseItemDecorator staticDecorator = BaseItemDecoratorBuilder.get(record);
        if (staticDecorator.getProperty(PropertyType.FEATURE_SCROLL) == null && staticDecorator.getProperty(PropertyType.FEATURE_PAGE_CHANGE) == null) {
            return buildNormalIcon(staticDecorator, record, shop, additionalVars, additionalListVars);
        } else {
            if (staticDecorator.getProperty(PropertyType.FEATURE_SCROLL) != null) {
                return buildScrollIcon(staticDecorator, record, shop, additionalVars, additionalListVars);
            } else {
                return buildPageIcon(staticDecorator, record, shop, additionalVars, additionalListVars);
            }
        }
    }

    private static @NotNull Item buildNormalIcon(@NotNull BaseItemDecorator staticDecorator, @NotNull IconRecord record, @Nullable Shop shop, @Nullable Map<String, String> additionalVars, @Nullable Map<String, List<String>> additionalListVars) {
        return Item.builder()
                .setItemProvider((player) -> {
                    BaseItemDecorator decorator = parseDecorator(record, shop, player, additionalVars);
                    return itemFromDecorator(decorator, shop, player, additionalVars, additionalListVars);
                })
                .addClickHandler((item, click) -> {
                    Player player = click.getPlayer();
                    ClickType clickType = click.getClickType();

                    Map<String, String> vars = new HashMap<>() {{
                        put("player-name", player.getName());
                        put("player-uuid", player.getUniqueId().toString());
                        put("click-type", clickType.name());
                        if (shop != null) {
                            put("shop-id", shop.getId());
                            put("shop-name", shop.getName());
                        }
                        if (additionalVars != null) {
                            putAll(additionalVars);
                        }
                    }};

                    BaseItemDecorator decorator = parseDecorator(record, shop, player, vars);
                    Map<ClickType, List<String>> commandData = decorator.getProperty(PropertyType.COMMANDS_DATA);
                    if (commandData != null) dispatchCommand(clickType, player, commandData, vars);
                    handleNormalFeatures(clickType, decorator, player, shop);
                })
                .updatePeriodically((Long) Optional.ofNullable(staticDecorator.getProperty(PropertyType.UPDATE_PERIOD)).orElse(-1L))
                .build();
    }

    private static @NotNull Item buildScrollIcon(@NotNull BaseItemDecorator staticDecorator, @NotNull IconRecord record, @Nullable Shop shop, @Nullable Map<String, String> additionalVars, @Nullable Map<String, List<String>> additionalListVars) {
        return BoundItem.scrollGui()
                .setItemProvider((player, gui) -> {
                    BaseItemDecorator decorator = parseDecorator(record, shop, player, additionalVars);
                    Map<String, String> vars = new HashMap<>() {{
                        // 当前 scroll 从 0 开始
                        put("current-line", String.valueOf(gui.getLine() + 1));
                        // 总数从 0 开始
                        put("total-line", String.valueOf(gui.getMaxLine() + 1));
                        if (additionalVars != null) {
                            putAll(additionalVars);
                        }
                    }};
                    return itemFromDecorator(decorator, shop, player, vars, additionalListVars);
                })
                .addClickHandler((item, gui, click) -> {
                    Player player = click.getPlayer();
                    ClickType clickType = click.getClickType();

                    Map<String, String> vars = new HashMap<>() {{
                        put("player-name", player.getName());
                        put("player-uuid", player.getUniqueId().toString());
                        put("click-type", clickType.name());
                        // 当前 scroll 从 0 开始
                        put("current-line", String.valueOf(gui.getLine() + 1));
                        // 总数从 0 开始
                        put("total-line", String.valueOf(gui.getMaxLine() + 1));
                        if (shop != null) {
                            put("shop-id", shop.getId());
                            put("shop-name", shop.getName());
                        }
                        if (additionalVars != null) {
                            putAll(additionalVars);
                        }
                    }};

                    BaseItemDecorator decorator = parseDecorator(record, shop, player, vars);
                    Map<ClickType, List<String>> commandData = decorator.getProperty(PropertyType.COMMANDS_DATA);
                    if (commandData != null) dispatchCommand(clickType, player, commandData, vars);
                    handleNormalFeatures(clickType, decorator, player, shop);
                    if (clickType == decorator.getProperty(PropertyType.FEATURE_SCROLL)) {
                        Integer amount = decorator.getProperty(PropertyType.FEATURE_SCROLL_AMOUNT);
                        featuresScroll(amount == null ? 0 : amount, gui);
                    }
                })
                .updatePeriodically((Long) Optional.ofNullable(staticDecorator.getProperty(PropertyType.UPDATE_PERIOD)).orElse(-1L))
                .
                .build();
    }

    private static @NotNull Item buildPageIcon(@NotNull BaseItemDecorator staticDecorator, @NotNull IconRecord record, @Nullable Shop shop, @Nullable Map<String, String> additionalVars, @Nullable Map<String, List<String>> additionalListVars) {
        return BoundItem.pagedGui()
                .setItemProvider((player, gui) -> {
                    BaseItemDecorator decorator = parseDecorator(record, shop, player, additionalVars);
                    Map<String, String> vars = new HashMap<>() {{
                        // 当前 page 从 0 开始
                        put("current-page", String.valueOf(gui.getPage() + 1));
                        // 总数从 1 开始
                        // 若不存在 content 则为 0
                        put("total-page", String.valueOf(gui.getPageAmount() == 0 ? 1 : gui.getPageAmount()));
                        if (additionalVars != null) {
                            putAll(additionalVars);
                        }
                    }};
                    return itemFromDecorator(decorator, shop, player, vars, additionalListVars);
                })
                .addClickHandler((item, gui, click) -> {
                    Player player = click.getPlayer();
                    ClickType clickType = click.getClickType();

                    Map<String, String> vars = new HashMap<>() {{
                        put("player-name", player.getName());
                        put("player-uuid", player.getUniqueId().toString());
                        put("click-type", clickType.name());
                        // 当前 page 从 0 开始
                        put("current-page", String.valueOf(gui.getPage() + 1));
                        // 总数从 1 开始
                        // 若不存在 content 则为 0
                        put("total-page", String.valueOf(gui.getPageAmount() == 0 ? 1 : gui.getPageAmount()));
                        if (shop != null) {
                            put("shop-id", shop.getId());
                            put("shop-name", shop.getName());
                        }
                        if (additionalVars != null) {
                            putAll(additionalVars);
                        }
                    }};

                    BaseItemDecorator decorator = parseDecorator(record, shop, player, vars);
                    Map<ClickType, List<String>> commandData = decorator.getProperty(PropertyType.COMMANDS_DATA);
                    if (commandData != null) dispatchCommand(clickType, player, commandData, vars);
                    handleNormalFeatures(clickType, decorator, player, shop);
                    if (clickType == decorator.getProperty(PropertyType.FEATURE_PAGE_CHANGE)) {
                        Integer amount = decorator.getProperty(PropertyType.FEATURE_PAGE_CHANGE_AMOUNT);
                        featuresPageChange(amount == null ? 0 : amount, gui);
                    }
                })
                .updatePeriodically((Long) Optional.ofNullable(staticDecorator.getProperty(PropertyType.UPDATE_PERIOD)).orElse(-1L))
                .build();
    }

    private static @NotNull BaseItemDecorator parseDecorator(@NotNull IconRecord iconRecord, @Nullable Shop shop, @NotNull Player player, @Nullable Map<String, String> additionalVars) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        Map<String, String> vars = new HashMap<>() {{
            if (shop != null) {
                put("shopping-mode-id", profile.getShoppingMode(shop.getId()).name());
                put("shop-id", shop.getId());
            }
            put("player-uuid", player.getUniqueId().toString());
            put("player-name", player.getName());
            if (additionalVars != null) {
                putAll(additionalVars);
            }
        }};
        // 尝试找到满足条件的第一个子图标
        // 否则使用默认图标
        // TODO 条件判断
        IconRecord targetIconRecord = iconRecord;
//        for (Map.Entry<String, IconRecord> entry : iconRecord.conditionIcons().entrySet()) {
//            if (JSUtils.evaluateBooleanFormula(entry.getKey(), vars, player)) {
//                targetIconRecord = entry.getValue();
//                break;
//            }
//        }
        return BaseItemDecoratorBuilder.get(targetIconRecord);
    }

    private static @NotNull xyz.xenondevs.invui.item.ItemBuilder itemFromDecorator(@NotNull BaseItemDecorator decorator, @Nullable Shop shop, @NotNull Player player, @Nullable Map<String, String> additionalVars, @Nullable Map<String, List<String>> additionalListVars) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        Map<String, String> vars = new HashMap<>() {{
            put("player-name", player.getName());
            put("player-uuid", player.getUniqueId().toString());
            if (shop != null) {
                put("shop-id", shop.getId());
                put("shop-name", shop.getName());
            }
            put("cart-total-price", profile.getCart().getMode() == OrderType.SELL_TO ?
                    MessageConfig.format_decimal.format(profile.getCart().getTotalPrice()) :
                    MessageConfig.placeholderAPI_cartTotalPrice_notSellToMode
            );
            // TODO stack 处理
//            if (profile.getCurrentStackPickerGUI() != null) {
//                put("stack", String.valueOf(((StackPickerGUI) profile.getCurrentStackPickerGUI()).getStack()));
//            }
            if (additionalVars != null) {
                putAll(additionalVars);
            }
        }};
        Integer amount = decorator.getProperty(PropertyType.AMOUNT);
        return new xyz.xenondevs.invui.item.ItemBuilder(
                new cn.encmys.ykdz.forest.dailyshop.api.utils.ItemBuilder(decorator.getBaseItem().build(player))
                        .setCustomModelData(decorator.getProperty(PropertyType.CUSTOM_MODEL_DATA))
                        .setItemFlags(decorator.getProperty(PropertyType.ITEM_FLAGS))
                        .setLore(TextUtils.decorateTextToComponent(new ArrayList<>() {{
                            addAll(decorator.getProperty(PropertyType.LORE));
                        }}, player, vars, additionalListVars))
                        .setDisplayName(TextUtils.decorateTextToComponent(decorator.getProperty(PropertyType.NAME), player, vars))
                        .setBannerPatterns(decorator.getProperty(PropertyType.BANNER_PATTERNS))
                        .setFireworkEffects(decorator.getProperty(PropertyType.FIREWORK_EFFECTS))
                        .setEnchantments(decorator.getProperty(PropertyType.ENCHANTMENTS))
                        // TODO 解析数量表达式
                        .build(amount == null ? 1 : amount)
        );
    }

    private static void handleNormalFeatures(@NotNull ClickType clickType, @NotNull BaseItemDecorator decorator, @NotNull Player player, @Nullable Shop shop) {
        if (clickType == decorator.getProperty(PropertyType.FEATURE_BACK_TO_SHOP) && shop != null) {
            backToShop(shop, player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_SWITCH_SHOPPING_MODE) && shop != null) {
            switchShoppingMode(shop, player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_OPEN_CART)) {
            openCart(player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_SETTLE_CART)) {
            settleCart(player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_SWITCH_CART_MODE)) {
            switchCartMode(player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_CLEAN_CART)) {
            cleanCart(player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_CLEAR_CART)) {
            clearCart(player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_LOAD_MORE_LOG)) {
            loadMoreLog(player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_OPEN_SHOP)) {
            String shopId = decorator.getProperty(PropertyType.FEATURE_OPEN_SHOP_TARGET);
            Shop shopToOpen = DailyShop.SHOP_FACTORY.getShop(shopId);
            if (shopToOpen == null) {
                LogUtils.warn("Try to open shop " + shopId + " but shop do not exist.");
                return;
            }
            openShop(shopToOpen, player);
        }
        if (clickType == decorator.getProperty(PropertyType.FEATURE_OPEN_ORDER_HISTORY)) {
            openOrderHistory(player);
        }
    }

    private static void dispatchCommand(@NotNull ClickType clickType, @NotNull Player player, @NotNull Map<ClickType, List<String>> commands, @NotNull Map<String, String> vars) {
        switch (clickType) {
            case LEFT ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.LEFT, new ArrayList<>()), vars);
            case RIGHT ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.RIGHT, new ArrayList<>()), vars);
            case SHIFT_LEFT ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.SHIFT_LEFT, new ArrayList<>()), vars);
            case SHIFT_RIGHT ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.SHIFT_RIGHT, new ArrayList<>()), vars);
            case DROP ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.DROP, new ArrayList<>()), vars);
            case DOUBLE_CLICK ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.DOUBLE_CLICK, new ArrayList<>()), vars);
            case MIDDLE ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.MIDDLE, new ArrayList<>()), vars);
            case CONTROL_DROP ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.CONTROL_DROP, new ArrayList<>()), vars);
            case SWAP_OFFHAND ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.SWAP_OFFHAND, new ArrayList<>()), vars);
            case NUMBER_KEY ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.NUMBER_KEY, new ArrayList<>()), vars);
            case WINDOW_BORDER_LEFT ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.WINDOW_BORDER_LEFT, new ArrayList<>()), vars);
            case WINDOW_BORDER_RIGHT ->
                    CommandUtils.dispatchCommands(player, commands.getOrDefault(ClickType.WINDOW_BORDER_RIGHT, new ArrayList<>()), vars);
            default -> {
            }
        }
    }

    private static void settleCart(Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        // 结算后购物车被清空的情况下无法获取总价
        // 故需要提前缓存
        double totalPrice = profile.getCart().getTotalPrice();
        Map<String, SettlementResult> result = profile.getCart().settle();
        // 根据结果集进行文字和音效提示
        // 购物车为空
        if (result.isEmpty()) {
            PlayerUtils.playSound(CartGUIConfig.getSoundRecord("settle-cart.failure"), player);
            PlayerUtils.sendMessage(MessageConfig.getCartSettleMessage(profile.getCart().getMode(), SettlementResult.EMPTY), player, new HashMap<>() {{
                put("mode", MessageConfig.getTerm(profile.getCart().getMode()));
            }});
            return;
        }
        // 因各种原因失败的 ShopOrder
        for (Map.Entry<String, SettlementResult> entry : result.entrySet()) {
            String shopId = entry.getKey();
            Shop shop = DailyShop.SHOP_FACTORY.getShop(shopId);
            if (shop == null) {
                continue;
            }
            SettlementResult settlementResult = entry.getValue();
            if (settlementResult != SettlementResult.SUCCESS) {
                // 仅提示错误
                PlayerUtils.sendMessage(MessageConfig.getCartSettleMessage(profile.getCart().getMode(), settlementResult), player, new HashMap<>() {{
                    put("shop-name", shop.getName());
                    put("shop-id", shop.getId());
                    put("mode", MessageConfig.getTerm(profile.getCart().getMode()));
                    if (profile.getCart().getMode() == OrderType.SELL_TO) {
                        put("cost", MessageConfig.format_decimal.format(totalPrice));
                    } else {
                        put("earn", MessageConfig.format_decimal.format(totalPrice));
                    }
                }});
            }
        }
        // 全部成功
        if (result.values().stream().allMatch(r -> r == SettlementResult.SUCCESS)) {
            PlayerUtils.playSound(CartGUIConfig.getSoundRecord("settle-cart.success"), player);
            PlayerUtils.sendMessage(MessageConfig.getCartSettleMessage(profile.getCart().getMode(), SettlementResult.SUCCESS), player, new HashMap<>() {{
                put("mode", MessageConfig.getTerm(profile.getCart().getMode()));
                if (profile.getCart().getMode() == OrderType.SELL_TO) {
                    put("cost", MessageConfig.format_decimal.format(totalPrice));
                } else {
                    put("earn", MessageConfig.format_decimal.format(totalPrice));
                }
            }});
        }
        // 部分成功
        else if (result.containsValue(SettlementResult.SUCCESS)) {
            PlayerUtils.playSound(CartGUIConfig.getSoundRecord("settle-cart.success"), player);
            PlayerUtils.sendMessage(MessageConfig.getCartSettleMessage(profile.getCart().getMode(), SettlementResult.PARTIAL_SUCCESS), player, new HashMap<>() {{
                put("mode", MessageConfig.getTerm(profile.getCart().getMode()));
                if (profile.getCart().getMode() == OrderType.SELL_TO) {
                    put("cost", MessageConfig.format_decimal.format(totalPrice - profile.getCart().getTotalPrice()));
                } else {
                    put("earn", MessageConfig.format_decimal.format(totalPrice - profile.getCart().getTotalPrice()));
                }
            }});
        }
        // 失败音效
        else {
            PlayerUtils.playSound(CartGUIConfig.getSoundRecord("settle-cart.failure"), player);
        }
        profile.getCartGUI().loadContent(player);
    }

    private static void loadMoreLog(@NotNull Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        if (profile.getViewingGuiType() == GUIType.ORDER_HISTORY) {
            profile.getOrderHistoryGUI().loadContent(player);
        }
    }

    private static void featuresScroll(int featuresScrollAmount, @NotNull ScrollGui<?> gui) {
        gui.scroll(featuresScrollAmount);
    }

    private static void featuresPageChange(int featuresPageChangeAmount, @NotNull PagedGui<?> gui) {
        gui.setPage(gui.getPage() + featuresPageChangeAmount);
    }

    private static void backToShop(@NotNull Shop shop, Player player) {
        shop.getShopGUI().open(player);
    }

    private static void switchShoppingMode(@NotNull Shop shop, Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        profile.setShoppingMode(shop.getId(),
                profile.getShoppingMode(shop.getId()) == ShoppingMode.DIRECT ? ShoppingMode.CART : ShoppingMode.DIRECT);
        PlayerUtils.sendMessage(MessageConfig.getShopOverrideableString(shop.getId(), "messages.action.shop.switch-shopping-mode.success"), player, new HashMap<>() {{
            put("shop-name", shop.getName());
            put("player-name", PlainTextComponentSerializer.plainText().serialize(player.displayName()));
            put("mode", MessageConfig.getTerm(profile.getShoppingMode(shop.getId())));
        }});
    }

    private static void openCart(Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        profile.getCartGUI().open();
        PlayerUtils.sendMessage(MessageConfig.messages_action_cart_openCart_success, player, new HashMap<>() {{
            put("player-name", player.getDisplayName());
        }});
        PlayerUtils.playSound(CartGUIConfig.getSoundRecord("open-cart.success"), player);
    }

    private static void switchCartMode(Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        profile.getCart().setMode(
                switch (profile.getCart().getMode()) {
                    case SELL_TO -> OrderType.BUY_FROM;
                    case BUY_FROM -> OrderType.BUY_ALL_FROM;
                    case BUY_ALL_FROM -> OrderType.SELL_TO;
                }
        );
        PlayerUtils.sendMessage(MessageConfig.messages_action_cart_switchCartMode_success, player, new HashMap<>() {{
            put("player-name", player.getDisplayName());
            put("mode", MessageConfig.getTerm(profile.getCart().getMode()));
        }});
        PlayerUtils.playSound(CartGUIConfig.getSoundRecord("switch-cart-mode.success"), player);
    }

    private static void cleanCart(Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        profile.getCart().clean();
        profile.getCartGUI().loadContent(player);
        PlayerUtils.sendMessage(MessageConfig.messages_action_cart_cleanCart_success, player, new HashMap<>() {{
            put("player-name", player.getDisplayName());
        }});
        PlayerUtils.playSound(CartGUIConfig.getSoundRecord("clean-cart.success"), player);
    }

    private static void clearCart(Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        profile.getCart().clear();
        profile.getCartGUI().loadContent(player);
        PlayerUtils.sendMessage(MessageConfig.messages_action_cart_clearCart_success, player, new HashMap<>() {{
            put("player-name", player.getDisplayName());
        }});
        PlayerUtils.playSound(CartGUIConfig.getSoundRecord("clear-cart.success"), player);
    }

    private static void openShop(@NotNull Shop shop, Player player) {
        shop.getShopGUI().open(player);
    }

    private static void openOrderHistory(Player player) {
        Profile profile = DailyShop.PROFILE_FACTORY.getProfile(player);
        profile.getOrderHistoryGUI().open();
    }
}
