package cn.encmys.ykdz.forest.hyphashop.script.pack;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.ContextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

@ObjectName("Command")
public class CommandObject extends InternalObject {
    @Static
    @Function("command")
    @FunctionParas({"cmd", "repeat", "delay", "p", "__player"})
    public static void command(@NotNull Context ctx) {
        String command = ContextUtils.getStringParam(ctx, "cmd").orElse(null);

        if (command == null) return;

        int repeat = Math.max(1, ContextUtils.getIntParam(ctx, "repeat").orElse(1));
        int delay = Math.max(0, ContextUtils.getIntParam(ctx, "delay").orElse(0));

        // 若不手动指定 p
        // 则视为 console
        CommandSender sender = Bukkit.getConsoleSender();

        boolean isPlayer = ContextUtils.getBooleanParam(ctx, "p").orElse(false);

        if (isPlayer) sender = ContextUtils.getPlayer(ctx).orElse(null);

        if (sender == null) return;

        CommandSender finalSender = sender;

        if (delay > 0) {
            Scheduler.runTaskLater(
                    (task) -> IntStream.range(0, repeat)
                            .forEach(i -> Scheduler.runTask((task2) -> Bukkit.dispatchCommand(finalSender, command))),
                    delay
            );
        } else {
            IntStream.range(0, repeat)
                    .forEach(i -> Scheduler.runTask((task) -> Bukkit.dispatchCommand(finalSender, command)));
        }
    }
}
