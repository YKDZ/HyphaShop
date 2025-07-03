package cn.encmys.ykdz.forest.hyphashop.script.pack;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphashop.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ObjectName("Player")
public class PlayerObject extends InternalObject {
    @Static
    @Function("sound")
    @FunctionParas({"sound", "volume", "pitch", "__player"})
    public static void sound(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null) return;

        Sound sound;

        try {
            sound = Registry.SOUND_EVENT.get(Key.key(ContextUtils.getStringParam(ctx, "sound").orElse("")));
        } catch (Exception ignored) {
            return;
        }

        if (sound == null) sound = Sound.BLOCK_ANVIL_BREAK;

        float volume = ContextUtils.getFloatParam(ctx, "volume").orElse(1f);
        float pitch = ContextUtils.getFloatParam(ctx, "pitch").orElse(1f);

        player.playSound(player, sound, volume, pitch);
    }

    @Static
    @Function("message")
    @FunctionParas({"msg", "__player"})
    public static void message(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null) return;

        String msg = ContextUtils.getStringParam(ctx, "msg").orElse(null);

        if (msg == null) return;

        HyphaAdventureUtils.sendPlayerMessage(player, msg);
    }
}
