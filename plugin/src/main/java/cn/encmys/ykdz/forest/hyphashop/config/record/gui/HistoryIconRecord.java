package cn.encmys.ykdz.forest.hyphashop.config.record.gui;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import org.jetbrains.annotations.NotNull;

public record HistoryIconRecord(@NotNull BaseItemDecorator iconDecorator,
                                @NotNull Script formatOrderContentLine,
                                @NotNull BaseItemDecorator miscPlaceholderIcon) {
}
