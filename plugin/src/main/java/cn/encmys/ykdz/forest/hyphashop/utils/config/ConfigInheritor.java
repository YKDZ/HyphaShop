package cn.encmys.ykdz.forest.hyphashop.utils.config;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ConfigInheritor implements ConfigAccessor {
    private static final @NotNull List<@NotNull String> nonInheritableKeys = List.of("icons");

    private final @NotNull ConfigAccessor parent;
    private final @NotNull ConfigAccessor config;

    public ConfigInheritor(@NotNull ConfigAccessor parent, @NotNull ConfigAccessor config) {
        this.parent = parent;
        this.config = config;
    }

    @Override
    public boolean isString(@NotNull String path) {
        return config.isString(path) || parent.isString(path);
    }

    @Override
    public boolean isList(@NotNull String path) {
        return config.isList(path) || parent.isList(path);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getString(path);
        return config.getString(path)
                .or(() -> parent.getString(path));
    }

    @Override
    public @NotNull Optional<Component> getComponent(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getComponent(path);
        return config.getComponent(path)
                .or(() -> parent.getComponent(path));
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getBoolean(path);
        return config.getBoolean(path)
                .or(() -> parent.getBoolean(path));
    }

    @Override
    public @NotNull Optional<List<Boolean>> getBooleanList(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getBooleanList(path);
        return config.getBooleanList(path)
                .or(() -> parent.getBooleanList(path));
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getFloat(path);
        return config.getFloat(path)
                .or(() -> parent.getFloat(path));
    }

    @Override
    public @NotNull Optional<List<Float>> getFloatList(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getFloatList(path);
        return config.getFloatList(path)
                .or(() -> parent.getFloatList(path));
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getInt(path);
        return config.getInt(path)
                .or(() -> parent.getInt(path));
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getLong(path);
        return config.getLong(path)
                .or(() -> parent.getLong(path));
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getDouble(path);
        return config.getDouble(path)
                .or(() -> parent.getDouble(path));
    }

    @Override
    public @NotNull Optional<Function> getFunction(@NotNull String path, @NotNull Context ctx) {
        if (nonInheritableKeys.contains(path)) return config.getFunction(path, ctx);
        return config.getFunction(path, ctx)
                .or(() -> parent.getFunction(path, ctx));
    }

    @Override
    public @NotNull Optional<List<String>> getStringList(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getStringList(path);
        return config.getStringList(path)
                .or(() -> parent.getStringList(path));
    }

    @Override
    public @NotNull Optional<ConfigAccessor> getConfig(@NotNull String path) {
        if (nonInheritableKeys.contains(path)) return config.getConfig(path);
        return config.getConfig(path)
                .or(() -> parent.getConfig(path));
    }

    @Override
    public @NotNull Optional<List<? extends ConfigAccessor>> getConfigList(@NotNull String path) {
        return config.getConfigList(path);
    }

    @Override
    public @NotNull Optional<Map<String, ? extends ConfigAccessor>> getLocalMembers() {
        return config.getLocalMembers();
    }

    @Override
    public @NotNull Optional<Object> get(@NotNull String path) {
        if (config.getComponent(path).isPresent()) return config.get(path);
        return config.get(path)
                .or(() -> parent.get(path));
    }

    @Override
    public @NotNull Set<@NotNull String> getKeys() {
        return config.getKeys();
    }

    @Override
    public boolean contains(@NotNull String path) {
        return config.contains(path) || parent.contains(path);
    }

    @Override
    public String toString() {
        return "ConfigInheritor{" +
                "parent=" + parent +
                ", config=" + config +
                '}';
    }
}
