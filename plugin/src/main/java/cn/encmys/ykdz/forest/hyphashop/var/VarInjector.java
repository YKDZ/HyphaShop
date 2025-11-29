package cn.encmys.ykdz.forest.hyphashop.var;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.var.extractor.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class VarInjector {
    private static final @NotNull List<@NotNull VarExtractor> EXTRACTORS = Arrays.asList(
            new SettlementLogExtractor(),
            new ProductExtractor(),
            new ShopExtractor(),
            new ProductShopExtractor(),
            new SenderExtractor(),
            new OfflinePlayerExtractor(),
            new PlayerShopExtractor(),
            new ProductPlayerExtractor(),
            new ShopOrderExtractor(),
            new PlayerShopShopOrderExtractor(),
            new IconExtractor(),
            new ClickExtractor(),
            new GUIExtractor(),
            new BundleProductExtractor(),
            new ItemStackExtractor()
    );

    private final @NotNull VarInjectorContext ctx = new VarInjectorContext();
    private final @NotNull Map<String, Object> extraVars = new HashMap<>();

    public @NotNull VarInjector withTarget(@NotNull Context target) {
        ctx.setTarget(target);
        return this;
    }

    public @NotNull VarInjector withRequiredVars(@NotNull Script script) {
        ctx.setRequiredVars(script.getLexicalScope() == null ? Collections.emptySet() : script.getLexicalScope().flattenToSet());
        return this;
    }

    public @NotNull VarInjector withRequiredVars(@NotNull List<Script> scripts) {
        ctx.setRequiredVars(scripts.stream()
                .map(script -> {
                    if (script.getLexicalScope() == null) throw new IllegalArgumentException("Lexical scope is null!");
                    return script.getLexicalScope().flattenToSet();
                })
                .flatMap(Set::stream)
                .collect(Collectors.toSet()));
        return this;
    }

    public @NotNull VarInjector withArgs(@NotNull List<@Nullable Object> args) {
        ctx.setArgs(args);
        return this;
    }

    public @NotNull VarInjector withArgs(@Nullable Object @NotNull ... args) {
        ctx.setArgs(args);
        return this;
    }

    public @NotNull VarInjector withArg(@Nullable Object arg) {
        ctx.addArg(arg);
        return this;
    }

    public @NotNull VarInjector withExtraVars(@NotNull Map<String, Object> extraVars) {
        this.extraVars.putAll(extraVars);
        return this;
    }

    public @NotNull Context inject() {
        if (!ctx.isReady()) throw new IllegalStateException("Context is not ready");

        // 用于调试的标记
        if (Config.debug)
            ctx.getTarget().declareMember("__var_injector_context__", new Reference(new Value(ctx.toString()), true));

        injectExtraVars();

        if (ctx.getArgs().isEmpty() || ctx.getArgs().stream().allMatch(Objects::isNull)) return ctx.getTarget();

        EXTRACTORS.forEach(extractor -> extractor.extract(ctx));

        return ctx.getTarget();
    }

    private void injectExtraVars() {
        extraVars.forEach((name, value) -> {
            if (value != null && value.getClass().isArray()) {
                ctx.getTarget().declareMember(name, new Reference(new Value(ScriptUtils.convertArray((Object[]) value)), true));
            } else {
                ctx.getTarget().declareMember(name, new Reference(new Value(value), true));
            }
        });
    }
}
