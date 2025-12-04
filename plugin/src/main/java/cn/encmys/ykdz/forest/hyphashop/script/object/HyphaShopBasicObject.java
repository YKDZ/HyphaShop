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
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@ObjectName("HyphaShopBasic")
public class HyphaShopBasicObject extends InternalObject {
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
            HyphaShopImpl.LOGGER.warn("""
                    Decimal provided to format_decimal(decimal) is not a number but %s.
                    Related context: %s
                    """.formatted(decimalValue, ctx));
            return null;
        }

        final BigDecimal decimal = decimalValue.getAsBigDecimal();

        try {
            return MessageConfig.getDecimalFormat(locale).format(decimal);
        } catch (Exception e) {
            HyphaShopImpl.LOGGER.debug("""
                    Error when formatting decimal in format_decimal(decimal).
                    %s
                    """.formatted(TextUtils.getStackTrace(e)));
            return null;
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
            HyphaShopImpl.LOGGER.debug("""
                    Date provided to format_data(data) is not a Java object but %s.
                    Related context: %s
                    """.formatted(dateValue, ctx));
            return null;
        }

        final Object dateObj = dateValue.value();

        if (!(dateObj instanceof Date)) {
            HyphaShopImpl.LOGGER.debug("""
                    Date provided to format_data(data) is not a Date instance but %s.
                    Related context: %s
                    """.formatted(dateValue, ctx));
            return null;
        }

        try {
            return MessageConfig.getDateFormat(locale).format(dateObj);
        } catch (Exception e) {
            HyphaShopImpl.LOGGER.debug("""
                    Error when formatting data in format_data(data).
                    Related context: %s
                    """.formatted(ctx));
            return null;
        }
    }

    @Static
    @Function("format_duration")
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

        if (path.isBlank()) {
            HyphaShopImpl.LOGGER.warn("""
                    Path can not be blank in lang(path). An empty component will be returned as fallback.
                    Related context: %s
                    """.formatted(ctx));
            return Component.empty();
        }

        String locale = ContextUtils.getPlayer(ctx).map(Player::locale).map(Locale::toLanguageTag).orElse("en-US");

        Script script = MessageConfig.getMessageScript(path, locale).orElse(null);

        if (script == null) {
            HyphaShopImpl.LOGGER.warn("""
                    Can not wrap message "%s" to script. An empty component will be returned as fallback.
                    Related context: %s
                    """.formatted(path, ctx));
            return Component.empty();
        }

        try {
            Value result = ScriptUtils.evaluate(
                    new VarInjector()
                            .withTarget(new Context(ctx))
                            .withRequiredVars(script)
                            .inject(),
                    script);

            return result.getAsAdventureComponent();
        } catch (Exception e) {
            HyphaShopImpl.LOGGER.error("""
                        Error when evaluate message script in lang(path). An empty component will be returned as fallback.
                        Related context: %s
                        %s
                    """.formatted(ctx, TextUtils.getStackTrace(e)));
            return Component.empty();
        }
    }

    @Static
    @Function("lang_list")
    @FunctionParas({"path", "__player"})
    public static Component[] langList(@NotNull Context ctx) {
        final String path = ctx.findMember("path").getReferredValue().getAsString();

        final String locale = ContextUtils.getPlayer(ctx).map(Player::locale).map(Locale::toLanguageTag).orElse("en-US");
        final List<Script> scripts = StringUtils.wrapToScriptWithOmit(MessageConfig.getMessageList(path, locale));

        try {
            return scripts.stream()
                    .flatMap((line) -> ScriptUtils.evaluateComponentList(new VarInjector()
                            .withTarget(new Context(ctx))
                            .withRequiredVars(line)
                            .inject(), line).stream())
                    .filter(Objects::nonNull)
                    .toArray(Component[]::new);
        } catch (Exception e) {
            HyphaShopImpl.LOGGER.error("""
                    Error when evaluate script in lang_list(path). An empty component array will be returned as fallback.
                    Related context: %s
                    %s
                    """.formatted(ctx, TextUtils.getStackTrace(e)));
            return new Component[0];
        }
    }
}