package cn.encmys.ykdz.forest.hyphashop.command.sub;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
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

public class OrderHistoryCommand {
    public static CommandNode<CommandSourceStack> getHistoryCommand() {
        return Commands.literal("history")
                .then(getHistoryOpenCommand())
                .build();
    }

    private static CommandNode<CommandSourceStack> getHistoryOpenCommand() {
        return Commands.literal("open")
                .requires(ctx -> ctx.getSender().hasPermission("hyphashop.command.history.open"))
                .then(CommandUtils.playerNameArgument("history-owner-name")
                        .then(Commands.argument("open-for", ArgumentTypes.player())
                                .executes((ctx) -> {
                                    final CommandSender sender = ctx.getSource().getSender();
                                    final String historyOwnerName = ctx.getArgument("history-owner-name", String.class);
                                    final OfflinePlayer historyOwner = Bukkit.getOfflinePlayer(historyOwnerName);
                                    final Player openFor = ctx
                                            .getArgument("open-for", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).getFirst();

                                    if (!historyOwner.hasPlayedBefore()) {
                                        StringUtils.wrapToScriptWithOmit(MessageConfig.getMessage("messages.command.history.open.failure.invalid-owner-name", ((Player) sender).locale()))
                                                .ifPresent(msg -> MessageUtils.sendMessageWithPrefix(sender, msg, Map.of("history_owner_name", historyOwnerName), sender));

                                        return Command.SINGLE_SUCCESS;
                                    }

                                    HyphaShop.PROFILE_FACTORY.getProfile(historyOwner).getOrderHistoryGUI()
                                            .open(openFor);
                                    StringUtils.wrapToScriptWithOmit(MessageConfig.getMessage("messages.command.history.open.success", ((Player) openFor).locale()))
                                            .ifPresent(msg -> MessageUtils.sendMessageWithPrefix(sender, msg, openFor));

                                    return Command.SINGLE_SUCCESS;
                                })))
                .build();
    }
}
