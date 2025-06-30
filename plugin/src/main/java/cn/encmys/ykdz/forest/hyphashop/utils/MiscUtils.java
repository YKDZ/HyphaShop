package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionableKey;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MiscUtils {
    public static void processActions(
            @NotNull ActionableKey key,
            @Nullable ActionsConfig actions,
            @NotNull Context parent,
            @NotNull Map<String, Object> extraVars,
            @Nullable Object... args
    ) {
        if (actions == null || !actions.hasAction(key)) return;
        final List<Script> actionList = actions.getActions(key);

        Scheduler.runAsyncTask((task) -> {
            CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
            for (Script action : actionList) {
                chain = chain.thenComposeAsync(
                        unused -> evaluateScriptAsync(new VarInjector()
                                .withTarget(new Context(parent))
                                .withRequiredVars(action)
                                .withArgs(args)
                                .withExtraVars(extraVars)
                                .inject(), action)
                );
            }
        });
    }

    public static @NotNull List<Value> processActionsWithResult(
            @NotNull ActionableKey key,
            @Nullable ActionsConfig actions,
            @NotNull Context parent,
            @NotNull Map<String, Object> extraVars,
            @Nullable Object... args
    ) {
        if (actions == null || !actions.hasAction(key)) return Collections.emptyList();
        if (actions.isEmpty()) return Collections.emptyList();

        return actions.getActions(key).stream().map(script -> ScriptUtils.evaluate(new VarInjector()
                        .withTarget(new Context(parent))
                        .withRequiredVars(script)
                        .withArgs(args)
                        .withExtraVars(extraVars)
                        .inject(), script))
                .toList();
    }

    @Contract("_, _ -> new")
    private static @NotNull CompletableFuture<Void> evaluateScriptAsync(@NotNull Context ctx, @NotNull Script script) {
        return CompletableFuture.runAsync(
                () -> ScriptUtils.evaluate(ctx, script)
        );
    }

    public static @NotNull Stream<Map.Entry<Integer, Integer>> generatePositionStream(@NotNull Reference @NotNull [] refArray, char targetChar) {
        return IntStream.range(0, refArray.length)
                .boxed()
                .flatMap(x -> {
                    final String row = refArray[x].getReferredValue().getAsString();
                    final List<Integer> yPositions = new ArrayList<>();

                    IntStream.range(0, row.length()).forEach(i -> {
                        final char c = row.charAt(i);
                        if (c != ' ' && c == targetChar) {
                            yPositions.add(i);
                        }
                    });

                    return yPositions.stream()
                            .map(y -> new AbstractMap.SimpleEntry<>(x, y));
                });
    }
}
