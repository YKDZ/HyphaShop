package cn.encmys.ykdz.forest.hyphashop.utils.config;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record ConfigurationSectionAccessor(@NotNull ConfigurationSection config) implements ConfigAccessor {
    public ConfigurationSectionAccessor(@Nullable ConfigurationSection config) {
        this.config = config == null ? new YamlConfiguration() : config;
    }

    @Override
    public boolean isString(@NotNull String path) {
        return config.isString(path);
    }

    @Override
    public boolean isList(@NotNull String path) {
        return config.isList(path);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull String path) {
        return Optional.ofNullable(config.getString(path));
    }

    @Override
    public @NotNull Optional<Component> getComponent(@NotNull String path) {
        return Optional.ofNullable(config.getComponent(path, MiniMessage.miniMessage()));
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? config.getBoolean(path) : null);
    }

    @Override
    public @NotNull Optional<List<Boolean>> getBooleanList(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? !config.getBooleanList(path).isEmpty() ? config.getBooleanList(path) : null : null);
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? (float) config.getDouble(path) : null);
    }

    @Override
    public @NotNull Optional<List<Float>> getFloatList(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? !config.getFloatList(path).isEmpty() ? config.getFloatList(path) : null : null);
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? config.getInt(path) : null);
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? config.getLong(path) : null);
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? config.getDouble(path) : null);
    }

    @Override
    public @NotNull Optional<Function> getFunction(@NotNull String path, @NotNull Context ctx) {
        return Optional.ofNullable(config.contains(path) ? ScriptUtils.evaluate(ctx, new Script(config.getString(path, ""))).getAsFunction() : null);
    }

    @Override
    public @NotNull Optional<List<String>> getStringList(@NotNull String path) {
        return Optional.ofNullable(config.contains(path) ? !config.getStringList(path).isEmpty() ? config.getStringList(path) : null : null);
    }

    @Override
    public @NotNull Optional<ConfigAccessor> getConfig(@NotNull String path) {
        final ConfigurationSection childConfig = config.getConfigurationSection(path);
        return Optional.ofNullable(childConfig != null ? new ConfigurationSectionAccessor(childConfig) : null);
    }

    @Override
    public @NotNull Optional<List<? extends ConfigAccessor>> getConfigList(@NotNull String path) {
        final List<ConfigAccessor> childConfigs = config.getMapList(path).stream()
                .map(map -> {
                    ConfigurationSection childConfig = new YamlConfiguration();
                    HyphaConfigUtils.loadMapIntoConfiguration(childConfig, map, "");
                    return new ConfigurationSectionAccessor(childConfig);
                })
                .collect(Collectors.toList());
        return Optional.of(childConfigs);
    }

    @Override
    public @NotNull Optional<Map<String, ? extends ConfigAccessor>> getLocalMembers() {
        final Map<String, ConfigAccessor> members = new HashMap<>();
        config.getKeys(false).forEach(key -> {
            ConfigurationSection childConfig = config.getConfigurationSection(key);
            if (childConfig == null) return;
            members.put(key, new ConfigurationSectionAccessor(childConfig));
        });
        return Optional.of(members);
    }

    @Override
    public @NotNull Optional<Object> get(@NotNull String path) {
        return Optional.ofNullable(config.get(path));
    }

    @Override
    public @NotNull Set<@NotNull String> getKeys() {
        return config.getKeys(false);
    }

    @Override
    public boolean contains(@NotNull String path) {
        return config.contains(path);
    }
}
