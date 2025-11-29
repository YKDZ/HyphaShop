package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.EvaluateResult;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptUtils {
    public static @NotNull Context extractContext(@NotNull String scriptStr) {
        final Script script = new Script(scriptStr);
        final EvaluateResult result = script.evaluate(new Context());

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when extracting context from script. Use global context as fallback value.");
            LogUtils.warn(result.toString());
            return InternalObjectManager.GLOBAL_OBJECT;
        }

        return script.getContext();
    }

    public static boolean evaluateBoolean(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script. Use false as fallback value.");
            LogUtils.warn(result.toString());
            return false;
        }

        if (!result.value().isType(Value.Type.BOOLEAN)) {
            LogUtils.warn("Result of script: " + script.getScript() + " is not boolean but " + result.value().getType() + ". Use false as fallback value.");
            return false;
        }

        return result.value().getAsBoolean();
    }

    public static double evaluateDouble(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script. Use NaN as fallback value.");
            LogUtils.warn(result.toString());
            return Double.NaN;
        }

        if (!result.value().isType(Value.Type.NUMBER)) {
            LogUtils.warn("Result of script: " + script.getScript() + " is not double but " + result.value().getType() + ". Use -1 as fallback value.");
            return Double.NaN;
        }

        return result.value().getAsBigDecimal().doubleValue();
    }

    public static int evaluateInt(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script. Use Integer.MIN_VALUE as fallback value.");
            LogUtils.warn(result.toString());
            return Integer.MIN_VALUE;
        }

        if (!result.value().isType(Value.Type.NUMBER)) {
            LogUtils.warn("Result of script: " + script.getScript() + " is not int but " + result.value().getType() + ". Use -1 as fallback value.");
            return Integer.MIN_VALUE;
        }

        return result.value().getAsBigDecimal().intValue();
    }

    public static @NotNull String evaluateString(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script. Use empty string as fallback value.");
            LogUtils.warn(result.toString());
            return "";
        }

        return result.value().getAsString();
    }

    public static @NotNull Component evaluateComponent(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script. Use empty string as fallback value.");
            LogUtils.warn(result.toString());
            return Component.empty();
        }

        return result.value().getAsAdventureComponent();
    }

    public static @NotNull List<Component> evaluateComponentList(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script. Use empty string list as fallback value.");
            LogUtils.warn(result.toString());
            return Collections.emptyList();
        }

        if (!result.value().isType(Value.Type.ADVENTURE_COMPONENT, Value.Type.STRING, Value.Type.ARRAY, Value.Type.NULL)) {
            LogUtils.warn("Result of script: " + script.getScript() + " is not string or string array but " + result.value().getType() + ". Use empty string list as fallback value.");
            return Collections.emptyList();
        }

        if (result.value().isType(Value.Type.NULL)) {
            return Collections.emptyList();
        } else if (result.value().isType(Value.Type.ADVENTURE_COMPONENT)) {
            return List.of(result.value().getAsAdventureComponent());
        } else if (result.value().isType(Value.Type.ARRAY)) {
            return Arrays.stream(result.value().getAsArray())
                    .map((ref) -> {
                        if (ref.getReferredValue().isType(Value.Type.ADVENTURE_COMPONENT)) {
                            return ref.getReferredValue().getAsAdventureComponent();
                        } else {
                            return HyphaAdventureUtils.getComponentFromMiniMessage(ref.getReferredValue().getAsString());
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of(HyphaAdventureUtils.getComponentFromMiniMessage(result.value().getAsString()));
        }
    }

    public static @NotNull List<String> evaluateStringList(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script. Use empty string list as fallback value.");
            LogUtils.warn(result.toString());
            return Collections.emptyList();
        }

        if (!result.value().isType(Value.Type.STRING, Value.Type.ARRAY, Value.Type.NULL)) {
            LogUtils.warn("Result of script: " + script.getScript() + " is not string or string array but " + result.value().getType() + ". Use empty string list as fallback value.");
            return Collections.emptyList();
        }

        if (result.value().isType(Value.Type.STRING)) {
            return List.of(result.value().getAsString());
        } else if (result.value().isType(Value.Type.NULL)) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(result.value().getAsArray())
                    .map((ref) -> ref.getReferredValue().getAsString())
                    .collect(Collectors.toList());
        }
    }

    public static @NotNull Value evaluate(@NotNull Context context, @NotNull Script script) {
        final EvaluateResult result = script.evaluate(context);

        if (result.type() != EvaluateResult.Type.SUCCESS) {
            LogUtils.warn("Error when evaluating script.");
            LogUtils.warn(result.toString());
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

    public static @NotNull ScriptObject convertToScriptObject(@NotNull Map<String, Reference> map) {
        final ScriptObject result = new ScriptObject();
        map.forEach(result::declareMember);
        return result;
    }
}
