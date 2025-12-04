package cn.encmys.ykdz.forest.hyphashop.command.sub;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GUICommand {
    public static CommandNode<CommandSourceStack> getGUICommand() {
        return Commands.literal("gui")
                .then(getGUIOpenCommand())
                .build();
    }

    private static CommandNode<CommandSourceStack> getGUIOpenCommand() {
        return Commands.literal("open")
                .requires(ctx -> ctx.getSender().hasPermission("hyphashop.command.gui.open"))
                .then(Commands.argument("gui", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            HyphaShop.NORMAL_GUI_FACTORY.getGUIs().keySet().stream()
                                    .filter(id -> id.startsWith(builder.getRemainingLowerCase()))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes((ctx) -> {
                                    final CommandSender sender = ctx.getSource().getSender();
                                    final String id = ctx.getArgument("gui", String.class);
                                    final Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                    if (!sender.hasPermission("hyphashop.gui.open." + id)) {
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if (!HyphaShop.NORMAL_GUI_FACTORY.hasGUI(id)) {
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Scheduler.runAsyncTask((task) -> HyphaShop.NORMAL_GUI_FACTORY.getGUI(id).open(target));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
    }
}
