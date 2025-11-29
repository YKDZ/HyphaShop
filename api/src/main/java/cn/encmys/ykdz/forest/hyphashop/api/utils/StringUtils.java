package cn.encmys.ykdz.forest.hyphashop.api.utils;

import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.script.ParserResult;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class StringUtils {
    private static final @NotNull Pattern NEWLINE_PATTERN = Pattern.compile("[\r\n]");
    private static final @NotNull Pattern TEMPLATE_VAR_PATTERN = Pattern.compile("\\$\\{[^}]*}");

    public static @NotNull ScriptObject wrapToScriptObject(@NotNull Map<String, ?> map) {
        ScriptObject object = new ScriptObject();
        map.forEach((key, value) -> object.declareMember(key, new Reference(new Value(value), true)));
        return object;
    }

    @Contract("null -> null")
    public static @Nullable Script wrapToScript(@Nullable String scriptStr) {
        if (scriptStr == null) return null;

        Script script = new Script(scriptStr);
        ParserResult result = script.parse();

        if (result.resultType() != ParserResult.Type.SUCCESS) {
            System.out.println(result);
        }

        return script;
    }

    public static @NotNull List<Script> wrapToScript(@Nullable List<String> scriptStrList) {
        if (scriptStrList == null) return new ArrayList<>();

        return scriptStrList.stream()
                .map(StringUtils::wrapToScript)
                .toList();
    }

    public static @NotNull Optional<Script> wrapToScriptWithOmit(@Nullable String scriptStr) {
        if (scriptStr == null) return Optional.empty();

        Script script = new Script(handleOmit(scriptStr));
        ParserResult result = script.parse();

        if (result.resultType() != ParserResult.Type.SUCCESS) {
            throw new RuntimeException("Script failed to parse: " + result.resultType());
        }

        return Optional.of(script);
    }

    public static @NotNull List<Script> wrapToScriptWithOmit(@Nullable List<String> scriptStrList) {
        if (scriptStrList == null) return new ArrayList<>();

        return scriptStrList.stream()
                .map(StringUtils::wrapToScriptWithOmit)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static @NotNull String handleOmit(@NotNull String scriptStr) {
        // 仅对单行内容触发省略机制
        if (!NEWLINE_PATTERN.matcher(scriptStr).find()) {
            // 含表达式插值格式的字符串允许省略反引号
            if (TEMPLATE_VAR_PATTERN.matcher(scriptStr).find()) {
                if ((!scriptStr.startsWith("`") || !scriptStr.startsWith("?`")) && !scriptStr.endsWith("`")) {
                    return "`" + scriptStr + "`";
                }
            }
            // 单行不含表达式插值的字符串即被视为字符串
            // 是为了方便书写单行字符串，减少迷惑
            // 但导致必须使用多行字符串语法在 lore 处书写脚本
            else if (!scriptStr.startsWith("\"") && !scriptStr.endsWith("\"")) {
                return "\"" + scriptStr + "\"";
            }
        }
        return scriptStr;
    }
}
