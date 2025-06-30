package cn.encmys.ykdz.forest.hyphashop.command.sub;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ProductCommand {
    public static CommandNode<CommandSourceStack> getProductCommand() {
        return Commands.literal("product")
                .build();
    }
}
