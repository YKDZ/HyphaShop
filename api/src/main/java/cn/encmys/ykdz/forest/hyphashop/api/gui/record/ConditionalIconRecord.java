package cn.encmys.ykdz.forest.hyphashop.api.gui.record;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import org.jetbrains.annotations.NotNull;

public record ConditionalIconRecord(@NotNull Script condition, int priority, @NotNull BaseItemDecorator decorator) {
}
