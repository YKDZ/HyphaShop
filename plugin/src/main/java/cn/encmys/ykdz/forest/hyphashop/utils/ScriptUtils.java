package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.EvaluateResult;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScriptUtils {
    public static @NotNull Context extractContext(@NotNull String scriptStr) {
        final Script script = new Script(scriptStr);
        final EvaluateResult result = script.evaluate(new Context());

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when extracting context from script. Use global context as fallback value.");
            HyphaShopImpl.LOGGER.warn(result.toString());
            return InternalObjectManager.GLOBAL_OBJECT;
        }

        return script.getContext();
    }

    public static boolean evaluateBoolean(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when evaluating script. Use false as fallback value.");
            HyphaShopImpl.LOGGER.warn(result.toString());
            return false;
        }

        if (!result.value().isType(Value.Type.BOOLEAN)) {
            HyphaShopImpl.LOGGER.warn("Result of script: " + script.getScript() + " is not boolean but " + result.value().type() + ". Use false as fallback value.");
            return false;
        }

        return result.value().getAsBoolean();
    }

    public static Optional<BigDecimal> evaluateBigDecimal(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when evaluating script. Use NaN as fallback value.");
            HyphaShopImpl.LOGGER.warn(result.toString());
            return Optional.empty();
        }

        if (!result.value().isType(Value.Type.NUMBER)) {
            HyphaShopImpl.LOGGER.warn("Result of script: " + script.getScript() + " is not double but " + result.value().type() + ". Use -1 as fallback value.");
            return Optional.empty();
        }

        return Optional.of(result.value().getAsBigDecimal());
    }

    public static double evaluateDouble(@NotNull Context context, @NotNull Script script) {
        return evaluateBigDecimal(context, script).map(BigDecimal::doubleValue).orElse(Double.NaN);
    }

    public static int evaluateInt(@NotNull Context context, @NotNull Script script) {
        return evaluateBigDecimal(context, script).map(BigDecimal::intValue).orElse(Integer.MIN_VALUE);

    }

    public static @NotNull String evaluateString(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when evaluating script. Use empty string as fallback value.");
            HyphaShopImpl.LOGGER.warn(result.toString());
            return "";
        }

        return result.value().getAsString();
    }

    public static @NotNull Component evaluateComponent(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when evaluating script. Use empty string as fallback value.");
            HyphaShopImpl.LOGGER.warn(result.toString());
            return Component.empty();
        }

        return result.value().getAsAdventureComponent();
    }

    public static @NotNull @Unmodifiable List<Component> evaluateComponentList(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when evaluating script. Use empty string list as fallback value.");
            HyphaShopImpl.LOGGER.warn(result.toString());
            return Collections.emptyList();
        }

        if (result.value().isType(Value.Type.NULL)) {
            return Collections.emptyList();
        } else if (result.value().isType(Value.Type.ARRAY)) {
            return result.value().getAsArray().values().stream()
                    .map((ref) -> ref.getReferredValue().getAsAdventureComponent())
                    .collect(Collectors.toList());
        }
        return List.of(result.value().getAsAdventureComponent());
    }

    public static @NotNull @Unmodifiable List<String> evaluateStringList(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when evaluating script. Use empty string list as fallback value.");
            HyphaShopImpl.LOGGER.warn(result.toString());
            return Collections.emptyList();
        }

        if (result.value().isType(Value.Type.NULL)) {
            return Collections.emptyList();
        } else if (result.value().isType(Value.Type.ARRAY)) {
            return result.value().getAsArray().values().stream()
                    .map((ref) -> ref.getReferredValue().getAsString())
                    .collect(Collectors.toList());
        }
        return List.of(result.value().getAsString());
    }

    public static @NotNull Value evaluate(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            HyphaShopImpl.LOGGER.warn("Error when evaluating script.");
            HyphaShopImpl.LOGGER.warn(result.toString());
        }

        return result.value();
    }

    public static @NotNull Reference @NotNull [] convertArray(@NotNull Object @NotNull [] array) {
        return Arrays.stream(array)
                .map((obj) -> {
                    if (obj instanceof Reference) {
                        return (Reference) obj;
                    } else {
                        return new Reference(new Value(obj));
                    }
                })
                .toArray(Reference[]::new);
    }
}
