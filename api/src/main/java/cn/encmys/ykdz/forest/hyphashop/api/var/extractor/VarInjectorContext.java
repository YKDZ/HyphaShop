package cn.encmys.ykdz.forest.hyphashop.api.var.extractor;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class VarInjectorContext {
    private final @NotNull List<@Nullable Object> args = new ArrayList<>();
    private @Nullable Context target;
    private @Nullable Set<@NotNull String> requiredVars;

    public boolean isReady() {
        return target != null && requiredVars != null;
    }

    public @NotNull List<@Nullable Object> getArgs() {
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
    public <T> @NotNull Optional<T> get(@NotNull Class<T> type) {
        if (!isReady()) return Optional.empty();
        assert target != null;

        for (Object param : args) {
            // 手动提供的对象优先
            if (type.isInstance(param)) return Optional.of((T) param);
        }

        // 从目标上下文查找
        return (Optional<T>) target.getLocalMembers().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("__") && !entry.getKey().endsWith("__"))
                .filter(entry -> (type.isInstance(entry.getValue().getReferredValue().getValue())))
                .map(entry -> entry.getValue().getReferredValue().getValue())
                .findFirst();
    }

    public boolean has(@NotNull Class<?> type) {
        return get(type).isPresent();
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
        // __ 开头的变量默认被注入
        if (name.startsWith("__") || requiredVars.contains(name)) {
            target.declareMember(name, new Reference(new Value(value.get()), true));
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