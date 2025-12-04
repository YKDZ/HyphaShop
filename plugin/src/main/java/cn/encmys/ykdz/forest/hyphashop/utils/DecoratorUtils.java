package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.gui.record.ConditionalIconRecord;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DecoratorUtils {
    public static @NotNull BaseItemDecorator selectDecoratorByCondition(@NotNull BaseItemDecorator staticDecorator, @NotNull Context parent, @NotNull Map<String, Object> vars, @Nullable Object... args) {
        final List<ConditionalIconRecord> conditionalIcons = staticDecorator.getProperty(ItemProperty.CONDITIONAL_ICONS);
        if (conditionalIcons == null) return staticDecorator;

        final List<ConditionalIconRecord> sortedIcons = new ArrayList<>(conditionalIcons);
        sortedIcons.sort((a, b) -> Integer.compare(b.priority(), a.priority()));

        return sortedIcons.stream()
                .filter(icon -> {
                    final Script condition = icon.condition();
                    return ScriptUtils.evaluateBoolean(new VarInjector()
                            .withArgs(args)
                            .withExtraVars(vars)
                            .withTarget(new Context(parent))
                            .withRequiredVars(condition)
                            .inject(), condition);
                })
                .findFirst()
                .map(ConditionalIconRecord::decorator)
                .orElse(staticDecorator);
    }
}
