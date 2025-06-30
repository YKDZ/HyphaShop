package cn.encmys.ykdz.forest.hyphashop.gui.factory;

import cn.encmys.ykdz.forest.hyphashop.api.gui.GUI;
import cn.encmys.ykdz.forest.hyphashop.api.gui.factory.NormalGUIFactory;
import cn.encmys.ykdz.forest.hyphashop.config.NormalGUIConfig;
import cn.encmys.ykdz.forest.hyphashop.gui.NormalGUI;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NormalGUIFactoryImpl implements NormalGUIFactory {
    private final static @NotNull Map<String, GUI> normalGUIs = new HashMap<>();

    public NormalGUIFactoryImpl() {
        load();
    }

    public void load() {
        NormalGUIConfig.getConfigs()
                .forEach(config -> config.getKeys()
                        .forEach(id -> register(id, new NormalGUI(config.getConfig(id).orElse(new ConfigurationSectionAccessor(new YamlConfiguration()))))));
    }

    public void unload() {
        normalGUIs.clear();
    }

    @Override
    public @NotNull GUI getGUI(@NotNull String name) {
        return normalGUIs.get(name);
    }

    @Override
    public void register(@NotNull String id, @NotNull GUI gui) {
        normalGUIs.put(id, gui);
    }

    @Override
    public boolean hasGUI(@NotNull String id) {
        return normalGUIs.containsKey(id);
    }

    @Override
    public @NotNull @Unmodifiable Map<String, GUI> getGUIs() {
        return Collections.unmodifiableMap(normalGUIs);
    }
}
