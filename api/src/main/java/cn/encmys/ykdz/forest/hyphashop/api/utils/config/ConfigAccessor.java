package cn.encmys.ykdz.forest.hyphashop.api.utils.config;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ConfigAccessor {
    boolean isString(@NotNull String path);

    boolean isList(@NotNull String path);

    @NotNull Optional<String> getString(@NotNull String path);

    @NotNull Optional<Component> getComponent(@NotNull String path);

    @NotNull Optional<Boolean> getBoolean(@NotNull String path);

    @NotNull Optional<List<Boolean>> getBooleanList(@NotNull String path);

    @NotNull Optional<Float> getFloat(@NotNull String path);

    @NotNull Optional<List<Float>> getFloatList(@NotNull String path);

    @NotNull Optional<Integer> getInt(@NotNull String path);

    @NotNull Optional<Long> getLong(@NotNull String path);

    @NotNull Optional<Double> getDouble(@NotNull String path);

    @NotNull Optional<Function> getFunction(@NotNull String path, @NotNull Context ctx);

    @NotNull Optional<List<String>> getStringList(@NotNull String path);

    @NotNull Optional<ConfigAccessor> getConfig(@NotNull String path);

    @NotNull Optional<List<? extends ConfigAccessor>> getConfigList(@NotNull String path);

    @NotNull Optional<Map<String, ? extends ConfigAccessor>> getLocalMembers();

    @NotNull Optional<Object> get(@NotNull String path);

    @NotNull Set<String> getKeys();

    boolean contains(@NotNull String path);

    boolean selfContains(@NotNull String path);
}
