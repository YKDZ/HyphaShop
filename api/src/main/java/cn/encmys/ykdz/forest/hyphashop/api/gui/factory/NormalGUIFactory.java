package cn.encmys.ykdz.forest.hyphashop.api.gui.factory;

import cn.encmys.ykdz.forest.hyphashop.api.gui.GUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public interface NormalGUIFactory {
    void unload();

    @NotNull GUI getGUI(@NotNull String name);

    void register(@NotNull String id, @NotNull GUI gui);

    boolean hasGUI(@NotNull String id);

    @NotNull
    @Unmodifiable
    Map<String, GUI> getGUIs();
}
