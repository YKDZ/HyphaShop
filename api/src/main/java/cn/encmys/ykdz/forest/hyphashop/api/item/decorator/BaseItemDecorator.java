package cn.encmys.ykdz.forest.hyphashop.api.item.decorator;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.record.ScriptOrComponentItemName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class BaseItemDecorator {
    protected final @NotNull BaseItem baseItem;
    protected final @NotNull Map<ItemProperty, Object> properties = new EnumMap<>(ItemProperty.class);

    public BaseItemDecorator(@NotNull BaseItem baseItem) {
        this.baseItem = baseItem;
    }

    public @NotNull BaseItemDecorator setProperty(@NotNull ItemProperty type, @Nullable Object value) {
        properties.put(type, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getProperty(@NotNull ItemProperty type) {
        Object value = properties.get(type);
        if (value == null) return null;
        else if (type.getToken().getRawType().isInstance(value)) {
            return (T) type.getToken().getRawType().cast(value);
        }
        throw new IllegalArgumentException("Invalid type for config key: " + type + ". Require " + type.getToken().getRawType() + " but given " + value);
    }

    public @NotNull ScriptOrComponentItemName getNameOrUseBaseItemName() {
        Script name = getProperty(ItemProperty.NAME);
        if (name == null) {
            return ScriptOrComponentItemName.of(baseItem.getDisplayName(this));
        }
        return ScriptOrComponentItemName.of(name);
    }

    public @NotNull BaseItem getBaseItem() {
        return baseItem;
    }

    @Override
    public String toString() {
        return "BaseItemDecorator{" +
                "baseItem=" + baseItem +
                ", properties=" + properties +
                '}';
    }
}