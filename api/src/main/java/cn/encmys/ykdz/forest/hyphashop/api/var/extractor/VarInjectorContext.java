package cn.encmys.ykdz.forest.hyphashop.api.var.extractor;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class VarInjectorContext {
    private final @NotNull List<@NotNull Object> args = new ArrayList<>();
    private @Nullable Context target;
    private @Nullable Set<@NotNull String> requiredVars;

    public boolean isReady() {
        return target != null && requiredVars != null;
    }

    public @NotNull List<Object> getArgs() {
        return args;
    }

    public void setArgs(@NotNull List<@Nullable Object> args) {
        this.args.addAll(args);
    }

    public void setArgs(@Nullable Object @NotNull ... args) {
        Arrays.stream(args).filter(Objects::nonNull).forEach(this.args::add);
    }

    public void addArg(@Nullable Object arg) {
        if (arg == null) return;
        args.add(arg);
    }

    public void setRequiredVars(@NotNull Set<@NotNull String> requiredVars) {
        this.requiredVars = requiredVars;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(@NotNull Class<T> type) {
        for (Object param : args) {
            if (type.isInstance(param)) {
                return (T) param;
            }
        }
        return null;
    }

    public boolean has(@NotNull Class<?> type) {
        return get(type) != null;
    }

    public boolean hasAll(Class<?> @NotNull ... types) {
        for (Class<?> type : types) {
            if (!has(type)) return false;
        }
        return true;
    }

    public void putVar(@NotNull String name, @NotNull Supplier<Object> value) {
        if (!isReady()) return;
        assert requiredVars != null;
        assert target != null;
        if (name.startsWith("__") || requiredVars.contains(name)) {
            target.declareMember(name, new Reference(new Value(value.get()), true, false));
        }
    }

    public @NotNull Context getTarget() {
        if (target == null) throw new IllegalStateException("Target not set");
        return target;
    }

    public void setTarget(@NotNull Context target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "VarInjectorContext{" +
                "target=" + target +
                ", requiredVars=" + requiredVars +
                ", args=" + args +
                '}';
    }
}