package cn.encmys.ykdz.forest.hyphashop.command.sub;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand {
    public static CommandNode<CommandSourceStack> getShopCommand() {
        return Commands.literal("shop")
                .then(getShopOpenCommand())
                .then(getShopRestockCommand())
                .build();
    }

    private static CommandNode<CommandSourceStack> getShopOpenCommand() {
        return Commands.literal("open")
                .requires(ctx -> ctx.getSender().hasPermission("hyphashop.command.shop.open"))
                .then(Commands.argument("shop", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            HyphaShop.SHOP_FACTORY.getShops().keySet().stream()
                                    .filter(shopId -> shopId.startsWith(builder.getRemainingLowerCase()))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes((ctx) -> {
                                    final CommandSender sender = ctx.getSource().getSender();
                                    final String shopId = ctx.getArgument("shop", String.class);
                                    final Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                    final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
                                    if (target == null) {
                                        MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_shop_open_failure_invalidPlayer, sender, shop);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if (!sender.hasPermission("hyphashop.shop.open." + shopId)) {
                                        MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_noPermission, sender, shop);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if (shop == null) {
                                        MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_shop_open_failure_invalidShop, sender);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_shop_open_success, sender, shop);
                                    Scheduler.runAsyncTask((task) -> shop.getShopGUI().open(target));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
    }

    private static CommandNode<CommandSourceStack> getShopRestockCommand() {
        return Commands.literal("restock")
                .requires(ctx -> ctx.getSender().hasPermission("hyphashop.command.shop.restock"))
                .then(Commands.argument("shop", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            HyphaShop.SHOP_FACTORY.getShops().keySet().stream()
                                    .filter(shopId -> shopId.startsWith(builder.getRemainingLowerCase()))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes((ctx) -> {
                            final CommandSender sender = ctx.getSource().getSender();
                            final String shopId = ctx.getArgument("shop", String.class);
                            final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
                            if (shop == null) {
                                MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_shop_restock_failure_invalidShop, sender);
                                return Command.SINGLE_SUCCESS;
                            }
                            shop.getShopStocker().stock();
                            MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_shop_restock_success, sender, shop);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
    }
}
