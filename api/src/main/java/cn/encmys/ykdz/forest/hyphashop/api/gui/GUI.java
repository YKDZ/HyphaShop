package cn.encmys.ykdz.forest.hyphashop.api.gui;

import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GUI {
    void closeAll();

    void open(@NotNull Player player);

    void updateContents(@NotNull Player player);

    void updateContentsForAllViewers();

    @NotNull List<String> getStructure();

    @NotNull ActionsConfig getActions();
}
