package cn.encmys.ykdz.forest.hyphashop.api.utils;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {
    public static void saveMigrationFile(@NotNull String jarDirPath) {
        if (!jarDirPath.endsWith("/")) {
            jarDirPath += "/";
        }
        if (jarDirPath.startsWith("/")) {
            jarDirPath = jarDirPath.substring(1);
        }

        final String searchPath = jarDirPath;

        try {
            final ClassLoader classLoader = HyphaShop.class.getClassLoader();
            final URL resourceUrl = classLoader.getResource(searchPath);

            if (resourceUrl == null) {
                throw new IOException("JAR directory not found: " + searchPath);
            }

            final String jarUriString = resourceUrl.toString().split("!")[0];
            if (!jarUriString.startsWith("jar:")) {
                throw new IOException("Unexpected resource URL: " + resourceUrl);
            }

            final URI jarUri = URI.create(jarUriString.substring("jar:".length()));

            final Path jarFilePath = Paths.get(jarUri);

            try (JarFile jarFile = new JarFile(jarFilePath.toFile())) {
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.startsWith(searchPath)
                            && !entryName.equals(searchPath)
                            && !entry.isDirectory()) {
                        saveResourceIfNotExists(entryName);
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to extract JAR resources from: " + jarDirPath, e);
        }
    }

    public static void saveResourceIfNotExists(@NotNull String resourcePath) {
        final File file = new File(HyphaShop.INSTANCE.getDataFolder(), resourcePath);

        if (file.exists()) {
            return;
        }

        try (InputStream in = HyphaShop.INSTANCE.getResource(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            file.getParentFile().mkdirs();
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save resource " + resourcePath, e);
        }
    }
}