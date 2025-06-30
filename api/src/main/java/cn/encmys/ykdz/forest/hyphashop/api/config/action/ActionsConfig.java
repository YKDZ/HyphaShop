package cn.encmys.ykdz.forest.hyphashop.api.config.action;

import cn.encmys.ykdz.forest.hypharepo.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionEvent;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionableKey;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

public record ActionsConfig(@NotNull Map<@NotNull ActionableKey, @NotNull List<@NotNull Script>> actions) {
    public static @NotNull ActionsConfig of(@NotNull ConfigAccessor config) {
        final Map<ActionableKey, List<Script>> actions = new HashMap<>();
        config.getKeys().forEach(key -> {
            ActionableKey actionKey;
            try {
                actionKey = ActionEvent.fromConfigKey(key);
            } catch (Exception e) {
                try {
                    actionKey = ActionClickType.fromConfigKey(key);
                } catch (Exception e2) {
                    LogUtils.warn(e.getMessage());
                    return;
                }
            }
            actions.put(actionKey, config.getStringList(key).orElse(new ArrayList<>()).stream()
                    .map(StringUtils::wrapToScript)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        });
        return new ActionsConfig(actions);
    }

    public @NotNull @Unmodifiable List<Script> getActions(@NotNull ActionableKey event) {
        return Collections.unmodifiableList(actions.get(event));
    }

    public boolean hasAction(@NotNull ActionableKey event) {
        return actions.containsKey(event);
    }

    public void inherit(@NotNull ActionsConfig other) {
        other.actions.forEach((action, list) -> {
            if (this.hasAction(action)) {
                this.actions.get(action).addAll(list);
            } else {
                this.actions.put(action, list);
            }
        });
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }
}
