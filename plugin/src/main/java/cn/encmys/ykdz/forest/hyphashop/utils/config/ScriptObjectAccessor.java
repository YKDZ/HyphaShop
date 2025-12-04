package cn.encmys.ykdz.forest.hyphashop.utils.config;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record ScriptObjectAccessor(@NotNull ScriptObject config) implements ConfigAccessor {
    public ScriptObjectAccessor(@Nullable ScriptObject config) {
        this.config = config == null ? new ScriptObject() : config;
    }

    @Override
    public boolean isString(@NotNull String path) {
        return config.findMemberWithPathSafely(path).orElse(new Value(null)).isType(Value.Type.STRING);
    }

    @Override
    public boolean isList(@NotNull String path) {
        return config.findMemberWithPathSafely(path).orElse(new Value(null)).isType(Value.Type.ARRAY);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull String path) {
        return config.findMember(path, String.class);
    }

    @Override
    public @NotNull Optional<Component> getComponent(@NotNull String path) {
        return config.findMember(path, Component.class);
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull String path) {
        return config.findMember(path, Boolean.class);
    }

    @Override
    public @NotNull Optional<List<Boolean>> getBooleanList(@NotNull String path) {
        return config.findMember(path, ScriptArray.class)
                .map(array -> array.values().stream()
                        .map(ref -> ref.getReferredValue().getAsBoolean())
                        .toList());
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull String path) {
        return config.findMember(path, Number.class).map(Number::floatValue);
    }

    @Override
    public @NotNull Optional<List<Float>> getFloatList(@NotNull String path) {
        return config.findMember(path, ScriptArray.class)
                .map(array -> array.values().stream()
                        .map(ref -> ref.getReferredValue().getAsBigDecimal().floatValue())
                        .toList());
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull String path) {
        return config.findMember(path, Number.class).map(Number::intValue);
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull String path) {
        return config.findMember(path, Number.class).map(Number::longValue);
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull String path) {
        return config.findMember(path, Number.class).map(Number::doubleValue);
    }

    @Override
    public @NotNull Optional<Function> getFunction(@NotNull String path, @NotNull Context ctx) {
        return config.findMember(path, Function.class);
    }

    @Override
    public @NotNull Optional<List<String>> getStringList(@NotNull String path) {
        return config.findMember(path, ScriptArray.class)
                .map(array -> array.values().stream()
                        .map(ref -> ref.getReferredValue().getAsString())
                        .toList());
    }

    @Override
    public @NotNull Optional<ConfigAccessor> getConfig(@NotNull String path) {
        return config.findMember(path, ScriptObject.class).map(ScriptObjectAccessor::new);
    }

    @Override
    public @NotNull Optional<List<? extends ConfigAccessor>> getConfigList(@NotNull String path) {
        return config.findMember(path, ScriptArray.class)
                .map(array -> array.values().stream()
                        .map(ref -> ref.getReferredValue().getAsScriptObject())
                        .map(ScriptObjectAccessor::new)
                        .toList());
    }

    @Override
    public @NotNull Optional<Map<String, ? extends ConfigAccessor>> getLocalMembers() {
        final Map<String, ConfigAccessor> members = new HashMap<>();
        config.getLocalMembers().keySet().forEach(key -> members.put(key, new ScriptObjectAccessor(config.findMember(key).getReferredValue().getAsScriptObject())));
        return Optional.of(members);
    }

    @Override
    public @NotNull Optional<Object> get(@NotNull String path) {
        return Optional.ofNullable(config.findMember(path).getReferredValue().value());
    }

    @Override
    public @NotNull Set<String> getKeys() {
        return config.getLocalMembers().keySet();
    }

    @Override
    public boolean contains(@NotNull String path) {
        return config.hasLocalMember(path);
    }
}
