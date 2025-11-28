package cn.encmys.ykdz.forest.hyphashop.script.object;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@ObjectName("HyphaShopBasic")
public class HyphaShopBasicObject extends InternalObject {
    @Static
    @Function("term")
    @FunctionParas({"enum_name"})
    public static String term(@NotNull Context ctx) {
        final Value enumId = ctx.findMember("enum_name").getReferredValue();
        if (!enumId.isType(Value.Type.STRING)) {
            if (Config.debug) {
                LogUtils.warn("HyphaShopBasic.term: enum_name is not a string");
                return "<red>ERROR: enum_name must be a string";
            } else {
                return null;
            }
        }
        final String enumIdStr = (String) enumId.getValue();

        final List<Supplier<String>> converters = Arrays.asList(
                () -> MessageConfig.getTerm(OrderType.valueOf(enumIdStr)),
                () -> MessageConfig.getTerm(ShoppingMode.valueOf(enumIdStr))
        );

        for (Supplier<String> converter : converters) {
            try {
                return converter.get();
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (Config.debug) {
            LogUtils.warn("HyphaShopBasic.term: invalid enum value: " + enumIdStr);
            return "<red>ERROR: invalid enum value: " + enumIdStr;
        } else {
            return null;
        }
    }

    @Static
    @Function("format_decimal")
    @FunctionParas({"decimal"})
    public static String formatDecimal(@NotNull Context ctx) {
        final Value decimalValue = ctx.findMember("decimal").getReferredValue();

        if (!decimalValue.isType(Value.Type.NUMBER)) {
            if (Config.debug) {
                LogUtils.warn("HyphaShopBasic.formatDecimal: decimal is not a number");
                return "<red>ERROR: decimal must be a number";
            } else {
                return null;
            }
        }

        final BigDecimal decimal = decimalValue.getAsBigDecimal();

        try {
            return MessageConfig.format_decimal.format(decimal);
        } catch (Exception e) {
            if (Config.debug) {
                LogUtils.warn("HyphaShopBasic.formatDecimal: error formatting decimal");
                return "<red>ERROR: decimal formatting error";
            } else {
                return null;
            }
        }
    }

    @Static
    @Function("format_date")
    @FunctionParas({"date"})
    public static String formatDate(@NotNull Context ctx) {
        final Value dateValue = ctx.findMember("date").getReferredValue();

        if (!dateValue.isType(Value.Type.JAVA_OBJECT)) {
            if (Config.debug) {
                LogUtils.warn("HyphaShopBasic.formatDate: date is not a Java object");
                return "<red>ERROR: date must be a Java object";
            } else {
                return null;
            }
        }

        final Object dateObj = dateValue.getValue();

        if (!(dateObj instanceof Date)) {
            if (Config.debug) {
                LogUtils.warn("HyphaShopBasic.formatDate: date is not a Date instance");
                return "<red>ERROR: date must be a Date instance";
            } else {
                return null;
            }
        }

        try {
            return MessageConfig.format_date.format(dateObj);
        } catch (Exception e) {
            if (Config.debug) {
                LogUtils.warn("HyphaShopBasic.formatDate: error formatting date");
                return "<red>ERROR: date formatting error";
            } else {
                return null;
            }
        }
    }
}