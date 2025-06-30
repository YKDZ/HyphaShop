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

public class DecoratorUtils {
    public static @NotNull BaseItemDecorator selectDecoratorByCondition(@NotNull BaseItemDecorator staticDecorator, @NotNull Context parent, @Nullable Object... args) {
        List<ConditionalIconRecord> conditionalIcons = staticDecorator.getProperty(ItemProperty.CONDITIONAL_ICONS);
        if (conditionalIcons == null) return staticDecorator;

        List<ConditionalIconRecord> sortedIcons = new ArrayList<>(conditionalIcons);
        sortedIcons.sort((a, b) -> Integer.compare(b.priority(), a.priority()));

        return sortedIcons.stream()
                .filter(icon -> {
                    Script condition = icon.condition();
                    return ScriptUtils.evaluateBoolean(new VarInjector()
                            .withArgs(args)
                            .withTarget(new Context(parent))
                            .withRequiredVars(condition)
                            .inject(), condition);
                })
                .findFirst()
                .map(ConditionalIconRecord::decorator)
                .orElse(staticDecorator);
    }
}
