package cn.encmys.ykdz.forest.dailyshop.profile.factory;

import cn.encmys.ykdz.forest.dailyshop.api.DailyShop;
import cn.encmys.ykdz.forest.dailyshop.api.profile.Profile;
import cn.encmys.ykdz.forest.dailyshop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.dailyshop.api.profile.factory.ProfileFactory;
import cn.encmys.ykdz.forest.dailyshop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.dailyshop.profile.ProfileImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileFactoryImpl implements ProfileFactory {
    private final static Map<UUID, Profile> profiles = new ConcurrentHashMap<>();

    @Override
    public Profile buildProfile(Player player) {
        Profile profile = new ProfileImpl(player);
        profiles.put(player.getUniqueId(), profile);
        return profile;
    }

    @NotNull
    public Profile getProfile(Player player) {
        Profile profile = profiles.get(player.getUniqueId());
        if (profile == null) {
            return buildProfile(player);
        }
        return profile;
    }

    @Override
    public Map<UUID, Profile> getProfiles() {
        return Collections.unmodifiableMap(profiles);
    }

    @Override
    public void removeProfile(Player player) {
        if (player.isOnline()) {
            return;
        }
        profiles.remove(player.getUniqueId());
    }

    @Override
    public void save() {
        List<Profile> data = new ArrayList<>();
        for (Profile profile : profiles.values()) {
            if (!profile.getCart().getOrders().isEmpty() ||
                    profile.getCart().getMode() != OrderType.SELL_TO ||
                    profile.getShoppingModes().containsValue(ShoppingMode.CART)
            ) {
                data.add(profile);
            }
        }
        DailyShop.DATABASE.saveProfileData(data);
    }

    @Override
    public void save(UUID playerUUID) {
        Profile profile = profiles.get(playerUUID);
        if (profile != null) {
            Bukkit.getScheduler().runTaskAsynchronously(
                    DailyShop.INSTANCE,
                    () -> DailyShop.DATABASE.saveProfileData(new ArrayList<>() {{
                        add(profile);
                    }})
            );
        }
    }

    @Override
    public void unload() {
        save();
        profiles.clear();
    }
}
