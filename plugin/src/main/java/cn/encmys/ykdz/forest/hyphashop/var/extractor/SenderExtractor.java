package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SenderExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        final CommandSender sender = ctx.get(CommandSender.class);
        if (sender == null) return;

        ctx.putVar("__command_sender", () -> sender);
        ctx.putVar("command_sender_name", () -> PlainTextComponentSerializer.plainText().serialize(sender.name()));
    }
}
