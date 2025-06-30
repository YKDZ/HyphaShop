package cn.encmys.ykdz.forest.hyphashop.api.item.decorator.record;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ScriptOrComponentItemName(@Nullable Script scriptName, @Nullable Component componentName) {
    public static @NotNull ScriptOrComponentItemName of(@NotNull Script scriptName) {
        return new ScriptOrComponentItemName(scriptName, null);
    }

    public static @NotNull ScriptOrComponentItemName of(@NotNull Component componentName) {
        return new ScriptOrComponentItemName(null, componentName);
    }

    public @NotNull Script scriptName() {
        if (!isScript())
            return Objects.requireNonNull(StringUtils.wrapToScriptWithOmit(PlainTextComponentSerializer.plainText().serialize(componentName)));
        return scriptName;
    }

    public @NotNull Component componentName() {
        if (!isComponent()) throw new IllegalStateException("Name is not a component.");
        return componentName;
    }

    public boolean isComponent() {
        return componentName != null;
    }

    public boolean isScript() {
        return scriptName != null;
    }
}
