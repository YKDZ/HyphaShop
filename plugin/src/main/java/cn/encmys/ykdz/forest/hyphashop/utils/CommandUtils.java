package cn.encmys.ykdz.forest.hyphashop.utils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandUtils {
    public static @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> playerNameArgument(@NotNull String argName) {
        return Commands.argument(argName, StringArgumentType.string())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.startsWith(builder.getRemainingLowerCase()))
                            .forEach(builder::suggest);
                    return builder.buildFuture();
                });
    }
}
