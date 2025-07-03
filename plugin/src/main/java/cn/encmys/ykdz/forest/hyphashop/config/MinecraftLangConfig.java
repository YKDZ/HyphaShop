package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Use to translate the display name of the vanilla base item in gui to specific language.
 * Inspired by <a href="https://github.com/ManyouTeam/UltimateShop">UltimateShop</a>.
 *
 * @since 0.3.1-beta
 * @deprecated Replaced by Translatable Component
 */
@Deprecated
public class MinecraftLangConfig {
    /**
     * Format like 1.21 or 1.16.5
     */
    private final static @NotNull String serverVersion = Bukkit.getVersion().split("-")[0];
    private final static @NotNull Map<String, String> config = new HashMap<>();

    public static void load() {
        final String fileDestination = HyphaShop.INSTANCE.getDataFolder() + "/lang/minecraft/" + Config.language_minecraftLang + ".json";
        try {
            final File locateFile = loadLangFile(fileDestination);
            loadMapFromLangFile(locateFile);
            LogUtils.info("Successfully loaded language file " + locateFile.getName());
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
        }
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<String, String> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    @NotNull
    private static File loadLangFile(@NotNull String fileDestination) {
        final File locateFile = new File(fileDestination);
        if (!locateFile.exists()) {
            locateFile.getParentFile().mkdirs();
            LogUtils.info("Start to load lang file " + Config.language_minecraftLang + ".json. The size of this file is around 600K.");
            if (downloadLangFileFromMcAssets(fileDestination) || downloadLangFileFromOfficial(fileDestination)) {
                return locateFile;
            } else {
                LogUtils.info("Failed to download lang file " + Config.language_minecraftLang + ".json. Use en_us.json (Version " + serverVersion + ") as fallback. Please check your network connection.");
                return useFallbackLang();
            }
        }
        return locateFile;
    }

    @Contract(" -> new")
    private static @NotNull File useFallbackLang() {
        HyphaShop.INSTANCE.saveResource("lang/minecraft/en_us.json", true);
        return new File(HyphaShop.INSTANCE.getDataFolder() + "/lang/minecraft/en_us.json");
    }

    private static boolean downloadLangFileFromMcAssets(@NotNull String fileDestination) {
        LogUtils.info("Try to download lang file " + Config.language_minecraftLang + ".json through mcasset.cloud.");
        try {
            downloadFile(new URI("https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/" + serverVersion + "/assets/minecraft/lang/" + Config.language_minecraftLang + ".json").toURL(), fileDestination);
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }

    private static boolean downloadLangFileFromOfficial(@NotNull String fileDestination) {
        LogUtils.info("Try to download lang file " + Config.language_minecraftLang + ".json through official api.");
        try {
            final JsonObject versions = fetchJson(new URI("https://launchermeta.mojang.com/mc/game/version_manifest.json").toURL());
            if (versions == null) return false;

            final String latestVersion = getLatestVersion(versions);
            if (latestVersion == null) return false;

            final String metaURLString = getMetaURLString(versions, latestVersion);
            final JsonObject meta = fetchJson(new URI(metaURLString).toURL());
            if (meta == null) return false;

            final JsonObject assetIndex = meta.getAsJsonObject("assetIndex");
            if (assetIndex == null || !assetIndex.has("url")) return false;

            final JsonObject assets = fetchJson(new URI(assetIndex.get("url").getAsString()).toURL());
            if (assets == null) return false;

            final String target = "minecraft/lang/" + Config.language_minecraftLang + ".json";

            final JsonObject targetAsset = getTargetAsset(assets, target);
            if (targetAsset == null) return false;

            final String hash = targetAsset.get("hash").getAsString();
            downloadFile(new URI("https://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash).toURL(), fileDestination);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @NotNull
    public static String translate(@NotNull Material material, @NotNull String sub) {
        return config.getOrDefault("item.minecraft." + material.name().toLowerCase() + sub, config.getOrDefault("block.minecraft." + material.name().toLowerCase() + sub, "<red>Name not found!>"));
    }

    private static String getLatestVersion(@NotNull JsonObject versions) {
        final JsonObject latest = versions.getAsJsonObject("latest");
        if (latest == null || !latest.has("release")) return null;
        return latest.get("release").getAsString();
    }

    @NotNull
    private static String getMetaURLString(@NotNull JsonObject versions, @NotNull String latestVersion) {
        final JsonArray versionArray = versions.getAsJsonArray("versions");
        if (versionArray == null) return "";
        String metaURLString = "https://piston-meta.mojang.com/v1/packages/177e49d3233cb6eac42f0495c0a48e719870c2ae/" + latestVersion + ".json";
        for (final JsonElement el : versionArray) {
            JsonObject versionObj = el.getAsJsonObject();
            if (versionObj != null && versionObj.has("id") && versionObj.get("id").getAsString().equals(serverVersion)) {
                if (versionObj.has("url")) {
                    metaURLString = versionObj.get("url").getAsString();
                    break;
                }
            }
        }
        return metaURLString;
    }

    @Nullable
    private static JsonObject getTargetAsset(@NotNull JsonObject assets, @NotNull String target) {
        final JsonObject objects = assets.getAsJsonObject("objects");
        if (objects == null || !objects.has(target)) return null;
        return objects.getAsJsonObject(target);
    }

    private static void loadMapFromLangFile(@NotNull File file) throws IOException {
        final Gson gson = new Gson();
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        try (final FileReader reader = new FileReader(file)) {
            config.clear();
            final Map<String, String> loadedConfig = gson.fromJson(reader, type);
            if (loadedConfig != null) {
                config.putAll(loadedConfig);
            }
        }
    }

    private static JsonObject fetchJson(@NotNull URL url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            final StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            return JsonParser.parseString(response.toString()).getAsJsonObject();
        } finally {
            connection.disconnect();
        }
    }

    private static void downloadFile(@NotNull URL url, String fileDestination) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10 * 1000); // 10 秒超时
        connection.setReadTimeout(10 * 1000); // 10 秒超时

        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download file: " + responseCode);
        }

        // 若目录不存在则创建
        final File file = new File(fileDestination);
        final File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        try (final InputStream inputStream = connection.getInputStream();
             final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             final FileOutputStream fileOutputStream = new FileOutputStream(file)) {

            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            connection.disconnect();
        }
    }
}
