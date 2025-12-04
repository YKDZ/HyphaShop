package cn.encmys.ykdz.forest.hyphashop.api.utils;

import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.script.ParserResult;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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

    @Contract("null -> null; !null -> !null")
    public static @Nullable Script wrapToScript(@Nullable String scriptStr) {
        if (scriptStr == null)
            return null;

        Script script = new Script(scriptStr);
        ParserResult result = script.parse();

        if (result.resultType() != ParserResult.Type.SUCCESS) {
            throw new RuntimeException(result.toString());
        }

        return script;
    }

    public static @NotNull @Unmodifiable List<Script> wrapToScript(@Nullable List<String> scriptStrList) {
        if (scriptStrList == null)
            return List.of();

        return scriptStrList.stream()
                .map(StringUtils::wrapToScript)
                .toList();
    }

    /**
     * 只有 <code>title</code>、<code>name</code>、<code>message</code>
     * 等语义的字段的包装才应该使用此方法
     */
    public static @NotNull Optional<Script> wrapToScriptWithOmit(@Nullable String scriptStr) {
        if (scriptStr == null)
            return Optional.empty();

        Script script = new Script(handleOmit(scriptStr));
        ParserResult result = script.parse();

        if (result.resultType() != ParserResult.Type.SUCCESS) {
            throw new RuntimeException(result.toString());
        }

        return Optional.of(script);
    }

    /**
     * 只有 <code>lore</code> 等语义的字段的包装才应该使用此方法
     */
    public static @NotNull @Unmodifiable List<@NotNull Script> wrapToScriptWithOmit(
            @Nullable List<String> scriptStrList) {
        if (scriptStrList == null)
            return List.of();

        return scriptStrList.stream()
                .map(StringUtils::wrapToScriptWithOmit)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 只有同时满足<br/>
     * 1. 单行<br/>
     * 2. 不以 <code>s:</code> 开头<br/>
     * 两个条件的字符串才会触发省略机制。<br/>
     * 省略机制行为如下：<br/>
     * 1. 包含合法的表达式插值 <code>${xxx}</code> 语法，且不以 <code>`</code> 或 <code>?`</code>
     * 开头、<code>`</code> 结尾的字符串会被自动补充开头与结尾的 <code>`</code><br/>
     * 2. 若不满足 <code>1</code> 中的条件，且不以 <code>"</code> 开头、<code>"</code>
     * 结尾的字符串会被自动补充开头与结尾的 <code>"</code>
     */
    private static @NotNull String handleOmit(@NotNull String str) {
        if (str.startsWith("s:"))
            return str.substring(2);

        // 仅对不以 s: 开头的单行文本触发省略机制
        if (!NEWLINE_PATTERN.matcher(str).find()) {
            // 含表达式插值格式的字符串允许省略反引号
            if (TEMPLATE_VAR_PATTERN.matcher(str).find()) {
                if (!(str.startsWith("`") || str.startsWith("?`")) && !str.endsWith("`")) {
                    return "`" + str + "`";
                }
            }
            // 单行不含表达式插值的字符串即被视为字符串
            else if (!str.startsWith("\"") && !str.endsWith("\"")) {
                return "\"" + str + "\"";
            }
        }

        return str;
    }
}
