package cn.encmys.ykdz.forest.hyphashop.item.builder;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.parser.BaseItemParser;
import cn.encmys.ykdz.forest.hyphashop.item.parser.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BaseItemBuilder {
    private static final @NotNull Set<BaseItemParser> parserRegistry = new HashSet<>();

    static {
        register(new ItemsAdderParser());
        register(new MMOItemsParser());
        register(new MythicMobsParser());
        register(new PotionParser());
        register(new SkullParser());
        register(new VanillaParser());
    }

    public static @NotNull Optional<BaseItem> get(@NotNull String base) {
        for (final BaseItemParser parser : parserRegistry) {
            if (parser.canParse(base)) return Optional.of(parser.parse(base));
        }
        return Optional.empty();
    }

    public static void register(@NotNull BaseItemParser parser) {
        parserRegistry.add(parser);
    }
}
