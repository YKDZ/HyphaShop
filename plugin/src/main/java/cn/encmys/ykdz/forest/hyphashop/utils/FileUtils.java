package cn.encmys.ykdz.forest.hyphashop.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {
    public static @NotNull List<@NotNull File> loadYmlFiles(@NotNull String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("路径不是目录: " + directoryPath);
        }

        try (var stream = Files.list(dir)) {
            return stream
                    .filter(path -> path.toString().endsWith(".yml"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }
}
