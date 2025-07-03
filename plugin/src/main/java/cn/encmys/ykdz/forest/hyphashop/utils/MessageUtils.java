package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.SettlementResult;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    private static final @NotNull Map<String, Object> extraVars = new HashMap<>() {{
        put("no_prefix", "");
    }};

    public static void sendMessage(@NotNull CommandSender sender, @NotNull Script message, @Nullable Object @NotNull ... args) {
        sendMessage(sender, message, new HashMap<>(), args);
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull Script message, @NotNull Map<String, Object> vars, @Nullable Object @NotNull ... args) {
        if (message.getScript().isBlank() || message.getScript().equals("``") || message.getScript().equals("\"\""))
            return;

        Scheduler.runAsyncTask((task) -> {
            final Context context = new VarInjector()
                    .withTarget(new Context())
                    .withRequiredVars(message)
                    .withExtraVars(vars)
                    .withExtraVars(extraVars)
                    .withArgs(args)
                    .inject();
            final Component msg = ScriptUtils.evaluateComponent(context, message);
            HyphaAdventureUtils.sendMessage(sender, msg);
        });
    }

    public static void sendMessageWithPrefix(@NotNull CommandSender sender, @NotNull Script message, @Nullable Object @NotNull ... args) {
        sendMessageWithPrefix(sender, message, new HashMap<>(), args);
    }

    public static void sendMessageWithPrefix(@NotNull CommandSender sender, @NotNull Script message, @NotNull Map<String, Object> vars, @Nullable Object @NotNull ... args) {
        if (message.getScript().isBlank() || message.getScript().equals("``") || message.getScript().equals("\"\""))
            return;

        Scheduler.runAsyncTask((task) -> {
            final Context context = new VarInjector()
                    .withTarget(new Context())
                    .withRequiredVars(message)
                    .withExtraVars(vars)
                    .withExtraVars(extraVars)
                    .withArgs(args)
                    .inject();
            final Component msg = ScriptUtils.evaluateComponent(context, message);
            if (message.getLexicalScope() != null
                    && message.getLexicalScope().flattenToSet().contains("no_prefix")) {
                HyphaAdventureUtils.sendMessage(sender, msg);
            } else {
                String prefix = ScriptUtils.evaluateString(context, MessageConfig.messages_prefix);
                HyphaAdventureUtils.sendMessage(sender, HyphaAdventureUtils.getComponentFromMiniMessage((prefix == null ? "" : prefix)).append(msg));
            }
        });
    }

    public static void handleSettleCartMessage(@NotNull Player player, @NotNull SettlementResult result) {
        final Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        sendMessageWithPrefix(player, MessageConfig.getSettleResultMessage(ShoppingMode.CART, profile.getCart().getOrder().getType(), result.type()), new HashMap<>() {{
            if (profile.getCart().getOrder().getType() == OrderType.SELL_TO) {
                put("cost", result.price());
            } else {
                put("earned", result.price());
            }
        }}, player);
    }
}
