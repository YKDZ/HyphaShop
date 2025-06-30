package cn.encmys.ykdz.forest.hyphashop.config.record.gui;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record HistoryIconRecord(@NotNull BaseItemDecorator iconDecorator,
                                @Nullable Script formatOrderContentLine,
                                @NotNull BaseItemDecorator miscPlaceholderIcon) {
}
