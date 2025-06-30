package cn.encmys.ykdz.forest.hyphashop.command.sub;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.CommandUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CartCommand {
    public static CommandNode<CommandSourceStack> getCartCommand() {
        return Commands.literal("cart")
                .then(getCartOpenCommand())
                .build();
    }

    private static CommandNode<CommandSourceStack> getCartOpenCommand() {
        return Commands.literal("open")
                .requires(ctx -> ctx.getSender().hasPermission("hyphashop.command.cart.open"))
                .then(CommandUtils.playerNameArgument("cart-owner-name")
                        .then(Commands.argument("open-for", ArgumentTypes.player())
                                .executes((ctx) -> {
                                    final CommandSender sender = ctx.getSource().getSender();
                                    final String cartOwnerName = ctx.getArgument("cart-owner-name", String.class);
                                    final OfflinePlayer cartOwner = Bukkit.getOfflinePlayer(cartOwnerName);
                                    final Player openFor = ctx.getArgument("open-for", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

                                    if (!cartOwner.hasPlayedBefore()) {
                                        MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_cart_open_failure_invalidOwnerName, Map.of("cart_owner_name", cartOwnerName), sender);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if (!cartOwner.getUniqueId().equals(openFor.getUniqueId())) {
                                        if (!sender.hasPermission("hyphashop.command.history.open.other")) {
                                            return Command.SINGLE_SUCCESS;
                                        }
                                    }

                                    HyphaShop.PROFILE_FACTORY.getProfile(cartOwner).getCartGUI().open(openFor);
                                    MessageUtils.sendMessageWithPrefix(sender, MessageConfig.messages_command_cart_open_success, sender, openFor);
                                    return Command.SINGLE_SUCCESS;
                                }))
                )
                .build();
    }
}
