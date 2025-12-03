package cn.encmys.ykdz.forest.hyphashop.script.object;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

@ObjectName("HyphaShopBasic")
public class HyphaShopBasicObject extends InternalObject {
    @Static
    @Function("term")
    @FunctionParas({"enum_name", "__player"})
    public static String term(@NotNull Context ctx) {
        final Value enumId = ctx.findMember("enum_name").getReferredValue();
        final String locale = ContextUtils.getPlayer(ctx)
                .map(Player::locale)
                .map(Locale::toLanguageTag)
                .orElse("en-US");

        if (!enumId.isType(Value.Type.STRING)) {
            if (Config.debug) {
                HyphaShopImpl.LOGGER.warn("HyphaShopBasic.term: enum_name is not a string");
                return "<red>ERROR: enum_name must be a string";
            } else {
                return null;
            }
        }
        final String enumIdStr = (String) enumId.value();

        final List<Supplier<String>> converters = Arrays.asList(
                () -> MessageConfig.getMessageString(MessageConfig.getTermPath(OrderType.valueOf(enumIdStr)), locale, "Term does not exists. Check console for more details"),
                () -> MessageConfig.getMessageString(MessageConfig.getTermPath(ShoppingMode.valueOf(enumIdStr)), locale, "Term does not exists. Check console for more details"));

        for (Supplier<String> converter : converters) {
            try {
                return converter.get();
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (Config.debug) {
            HyphaShopImpl.LOGGER.warn("HyphaShopBasic.term: invalid enum value: " + enumIdStr);
            return "<red>ERROR: invalid enum value: " + enumIdStr;
        } else {
            return null;
        }
    }

    @Static
    @Function("format_decimal")
    @FunctionParas({"decimal", "__player"})
    public static String formatDecimal(@NotNull Context ctx) {
        final Value decimalValue = ctx.findMember("decimal").getReferredValue();
        final String locale = ContextUtils.getPlayer(ctx)
                .map(Player::locale)
                .map(Locale::toLanguageTag)
                .orElse("en-US");

        if (!decimalValue.isType(Value.Type.NUMBER)) {
            if (Config.debug) {
                HyphaShopImpl.LOGGER.warn("HyphaShopBasic.formatDecimal: decimal is not a number");
                return "<red>ERROR: decimal must be a number";
            } else {
                return null;
            }
        }

        final BigDecimal decimal = decimalValue.getAsBigDecimal();

        try {
            return MessageConfig.getDecimalFormat(locale).format(decimal);
        } catch (Exception e) {
            if (Config.debug) {
                HyphaShopImpl.LOGGER.warn("HyphaShopBasic.formatDecimal: error formatting decimal");
                return "<red>ERROR: decimal formatting error";
            } else {
                return null;
            }
        }
    }

    @Static
    @Function("format_date")
    @FunctionParas({"date", "__player"})
    public static String formatDate(@NotNull Context ctx) {
        final Value dateValue = ctx.findMember("date").getReferredValue();

        final String locale = ContextUtils.getPlayer(ctx)
                .map(Player::locale)
                .map(Locale::toLanguageTag)
                .orElse("en-US");

        if (!dateValue.isType(Value.Type.JAVA_OBJECT)) {
            if (Config.debug) {
                HyphaShopImpl.LOGGER.warn("HyphaShopBasic.formatDate: date is not a Java object");
                return "<red>ERROR: date must be a Java object";
            } else {
                return null;
            }
        }

        final Object dateObj = dateValue.value();

        if (!(dateObj instanceof Date)) {
            if (Config.debug) {
                HyphaShopImpl.LOGGER.warn("HyphaShopBasic.formatDate: date is not a Date instance");
                return "<red>ERROR: date must be a Date instance";
            } else {
                return null;
            }
        }

        try {
            return MessageConfig.getDateFormat(locale).format(dateObj);
        } catch (Exception e) {
            if (Config.debug) {
                HyphaShopImpl.LOGGER.warn("HyphaShopBasic.formatDate: error formatting date");
                return "<red>ERROR: date formatting error";
            } else {
                return null;
            }
        }
    }

    @Static
    @Function("formatDuration")
    @FunctionParas({"duration", "__player"})
    public static String formatDuration(@NotNull Context ctx) {
        final long duration = ctx.findMember("duration").getReferredValue().getAsBigDecimal().longValue();
        final String locale = ContextUtils.getPlayer(ctx).map(Player::locale).map(Locale::toLanguageTag).orElse("en-US");

        return TextUtils.formatDuration(Duration.ofMillis(duration), locale);
    }

    @Static
    @Function("lang")
    @FunctionParas({"path", "__player"})
    public static Component lang(@NotNull Context ctx) {
        final String path = ctx.findMember("path").getReferredValue().getAsString();

        if (path.isBlank())
            return Component.text("lang(path)'s path can not be blank");

        String locale = ContextUtils.getPlayer(ctx).map(Player::locale).map(Locale::toLanguageTag).orElse("en-US");

        Script script = MessageConfig.getMessageScript(path, locale).orElse(new Script("Message does not exists. Check console for more details"));

        Value result = ScriptUtils.evaluate(
                new VarInjector()
                        .withTarget(new Context(ctx))
                        .withRequiredVars(script)
                        .inject(),
                script);

        return result.getAsAdventureComponent();
    }

    @Static
    @Function("langList")
    @FunctionParas({"path", "__player"})
    public static Component[] langList(@NotNull Context ctx) {
        final String path = ctx.findMember("path").getReferredValue().getAsString();

        final String locale = ContextUtils.getPlayer(ctx).map(Player::locale).map(Locale::toLanguageTag).orElse("en-US");
        final List<Script> scripts = StringUtils.wrapToScriptWithOmit(MessageConfig.getMessageList(path, locale));

        return scripts.stream()
                .flatMap((line) -> ScriptUtils.evaluateComponentList(new VarInjector()
                        .withTarget(new Context(ctx))
                        .withRequiredVars(line)
                        .inject(), line).stream())
                .filter(Objects::nonNull)
                .toArray(Component[]::new);
    }
}