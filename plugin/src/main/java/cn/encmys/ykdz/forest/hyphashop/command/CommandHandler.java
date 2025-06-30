package cn.encmys.ykdz.forest.hyphashop.command;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.command.sub.CartCommand;
import cn.encmys.ykdz.forest.hyphashop.command.sub.GUICommand;
import cn.encmys.ykdz.forest.hyphashop.command.sub.OrderHistoryCommand;
import cn.encmys.ykdz.forest.hyphashop.command.sub.ShopCommand;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public class CommandHandler {
    public static LiteralCommandNode<CommandSourceStack> load() {
        return Commands.literal("hyphashop")
                .then(getReloadCommand())
                .then(getSaveCommand())
                .then(CartCommand.getCartCommand())
                .then(OrderHistoryCommand.getHistoryCommand())
                .then(ShopCommand.getShopCommand())
                .then(GUICommand.getGUICommand())
                .build();
    }

    private static CommandNode<CommandSourceStack> getReloadCommand() {
        return Commands.literal("reload")
                .executes((ctx) -> {
                    final CommandSender sender = ctx.getSource().getSender();
                    HyphaShop.INSTANCE.reload();
                    MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_reload_success, sender);
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    private static CommandNode<CommandSourceStack> getSaveCommand() {
        return Commands.literal("save")
                .executes((ctx) -> {
                    final CommandSender sender = ctx.getSource().getSender();
                    HyphaShop.PROFILE_FACTORY.save();
                    HyphaShop.PRODUCT_FACTORY.save();
                    HyphaShop.SHOP_FACTORY.save();
                    MessageUtils.sendMessageWithPrefix(ctx.getSource().getSender(), MessageConfig.messages_command_save_success, sender);
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
