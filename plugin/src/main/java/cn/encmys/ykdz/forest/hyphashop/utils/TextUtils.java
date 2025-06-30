package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.record.ScriptOrComponentItemName;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextUtils {
    private static final @NotNull
    @Unmodifiable Map<Character, Integer> TIME_UNITS = Map.of(
            's', 20,
            'm', 60 * 20,
            'h', 60 * 60 * 20,
            'd', 24 * 60 * 60 * 20,
            'w', 7 * 24 * 60 * 60 * 20
    );
    private static final @NotNull Pattern SPACE_PATTERN = Pattern.compile("\\s+");

    public static @Range(from = 0L, to = Long.MAX_VALUE) int parseTimeStringToTicks(@Nullable String time) {
        // 处理空输入
        if (time == null || time.isEmpty()) {
            return 0;
        }

        // 去除前后空格并检查有效性
        final String trimmedTime = time.trim();
        if (trimmedTime.isEmpty()) {
            return 0;
        }

        // 分割时间表达式
        final String[] parts = SPACE_PATTERN.split(trimmedTime);
        int ticks = 0;

        for (final String part : parts) {
            // 检查基本格式
            if (part.length() < 2) {
                LogUtils.warn("Invalid time format: '" + part + "', should format like '5s'");
                return 0; // 任何部分格式错误立即返回 0
            }

            // 解析单位和数值
            final char unit = Character.toLowerCase(part.charAt(part.length() - 1));
            final String valueStr = part.substring(0, part.length() - 1);

            // 获取时间单位对应的 ticks
            Integer unitTicks = TIME_UNITS.get(unit);
            if (unitTicks == null) {
                LogUtils.warn("Invalid time unit: '" + unit + "', in experssion'" + part + "'. Use 's' as fallback.");
                unitTicks = TIME_UNITS.get('s'); // 使用秒作为 fallback
            }

            // 解析数值
            try {
                final double value = Double.parseDouble(valueStr);
                ticks += (int) Math.round(value * unitTicks);
            } catch (NumberFormatException e) {
                LogUtils.warn("Fail to parse number: '" + valueStr + "', in experssion'" + part + ". This part will use 0 as fallback.");
            }
        }

        return Math.max(ticks, 0);
    }

    public static @NotNull Component parseNameToComponent(@NotNull ScriptOrComponentItemName displayName, @NotNull Context parent, @Nullable Object... args) {
        if (displayName.isComponent()) return displayName.componentName();
        return parseNameToComponent(displayName.scriptName(), parent, Collections.emptyMap(), args);
    }

    public static @NotNull Component parseNameToComponent(@NotNull ScriptOrComponentItemName displayName, @NotNull Context parent, @NotNull Map<String, Object> vars, @Nullable Object... args) {
        if (displayName.isComponent()) return displayName.componentName();
        return parseNameToComponent(displayName.scriptName(), parent, vars, args);
    }

    public static @Nullable Component parseNameToComponent(@Nullable Script displayName, @NotNull Context parent, @NotNull Map<String, Object> vars, @Nullable Object... args) {
        if (displayName == null) return null;

        return ScriptUtils.evaluateComponent(new VarInjector()
                .withTarget(new Context(parent))
                .withRequiredVars(displayName)
                .withArgs(args)
                .withExtraVars(vars)
                .inject(), displayName);
    }

    public static @Nullable List<Component> parseLoreToComponent(@Nullable List<Script> lore, @NotNull Context parent, @Nullable Object @NotNull ... args) {
        return parseLoreToComponent(lore, parent, Collections.emptyMap(), args);
    }

    public static @Nullable List<Component> parseLoreToComponent(@Nullable List<Script> lore, @NotNull Context parent, @NotNull Map<String, Object> vars, @Nullable Object... args) {
        List<Component> result = null;
        if (lore != null) {
            result = lore.stream()
                    .flatMap((line) -> ScriptUtils.evaluateComponentList(new VarInjector()
                            .withTarget(new Context(parent))
                            .withRequiredVars(lore)
                            .withArgs(args)
                            .withExtraVars(vars)
                            .inject(), line).stream())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static @NotNull String parseRestockTimer(@NotNull Shop shop) {
        long timeRemaining = (shop.getShopStocker().getLastRestocking() + shop.getShopStocker().getAutoRestockPeriod() * 50L) - System.currentTimeMillis();
        if (timeRemaining > 0) {
            long hours = timeRemaining / (60 * 60 * 1000);
            long minutes = (timeRemaining % (60 * 60 * 1000)) / (60 * 1000);
            long seconds = (timeRemaining % (60 * 1000)) / 1000;
            return String.format(MessageConfig.format_timer, hours, minutes, seconds);
        }
        return String.format(MessageConfig.format_timer, 0, 0, 0);
    }
}
