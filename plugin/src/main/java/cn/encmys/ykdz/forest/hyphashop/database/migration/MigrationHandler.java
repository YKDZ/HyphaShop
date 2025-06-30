package cn.encmys.ykdz.forest.hyphashop.database.migration;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.enums.DBType;
import cn.encmys.ykdz.forest.hyphashop.api.database.migration.record.MigrationFile;
import cn.encmys.ykdz.forest.hyphashop.api.database.schema.DBVersionSchema;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MigrationHandler {
    protected final @NotNull String migrationDir;
    protected final @NotNull String jarResourceDir;

    public MigrationHandler(@NotNull DBType dbType) {
        final String dbName = dbType.name().toLowerCase();
        this.migrationDir = HyphaShop.INSTANCE.getDataFolder() + "/data/migrations/" + dbName;
        this.jarResourceDir = "/data/migrations/" + dbName + "/";
    }

    public boolean migrate() {
        try {
            final List<MigrationFile> migrations = scanMigrationFiles();
            int currentVersion = getCurrentVersion();
            final List<MigrationFile> pending = getPendingMigrations(migrations, currentVersion);

            if (pending.isEmpty()) {
                LogUtils.info("Database is up-to-date.");
                return true;
            }

            validateMigrationSequence(currentVersion, pending);

            for (MigrationFile migration : pending) {
                executeMigration(migration, currentVersion);
                LogUtils.info("Successfully executed migration " + migration.filename());
                currentVersion = migration.version();
            }
            return true;
        } catch (Exception e) {
            handleMigrationError(e);
            return false;
        }
    }

    protected List<MigrationFile> scanMigrationFiles() throws IOException {
        final File dir = new File(migrationDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create migration directory: " + dir.getAbsolutePath());
        }

        copyMigrationsFromJar();

        final List<MigrationFile> files = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(
                dir.toPath(),
                "V*__*.sql"
        )) {
            for (final Path entry : stream) {
                final File file = entry.toFile();
                if (file.isFile() && file.canRead()) {
                    parseMigrationFile(file.getName(), files);
                }
            }
        }

        detectVersionConflicts(files);

        return files.stream()
                .sorted(Comparator.comparingInt(MigrationFile::version))
                .collect(Collectors.toList());
    }

    protected void copyMigrationsFromJar() throws IOException {
        final URL jarDirUrl = getClass().getResource(jarResourceDir);
        if (jarDirUrl == null) return;

        try (final JarFile jarFile = ((JarURLConnection) jarDirUrl.openConnection()).getJarFile()) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (isValidMigrationEntry(entry)) {
                    saveMigrationResource(entry);
                }
            }
        }
    }

    protected boolean isValidMigrationEntry(@NotNull JarEntry entry) {
        return entry.getName().startsWith(jarResourceDir.substring(1)) &&
                entry.getName().endsWith(".sql") &&
                !entry.isDirectory();
    }

    protected void saveMigrationResource(@NotNull JarEntry entry) {
        final String resourcePath = entry.getName();
        final String filename = extractFilename(resourcePath);
        final File targetFile = new File(migrationDir, filename);

        if (!targetFile.exists()) {
            // 使用正确的资源路径，移除 jarResourceDir 的前导斜杠以避免路径错误
            final String resourceRelativePath = jarResourceDir.substring(1) + filename;
            HyphaShop.INSTANCE.saveResource(resourceRelativePath, false);
            LogUtils.info("Copied migration file: " + filename);
        }
    }

    protected String extractFilename(@NotNull String resourcePath) {
        return resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
    }

    protected void detectVersionConflicts(@NotNull List<MigrationFile> files) throws IOException {
        final Set<Integer> versions = new HashSet<>();
        for (MigrationFile f : files) {
            if (!versions.add(f.version())) {
                throw new IOException("Duplicate migration version detected: V" + f.version());
            }
        }
    }

    protected List<MigrationFile> getPendingMigrations(@NotNull List<MigrationFile> allMigrations, int currentVersion) {
        return allMigrations.stream()
                .filter(m -> m.version() > currentVersion)
                .collect(Collectors.toList());
    }

    protected void validateMigrationSequence(int currentVersion, @NotNull List<MigrationFile> pending) {
        int expectedVersion = currentVersion + 1;
        for (MigrationFile m : pending) {
            if (m.version() != expectedVersion) {
                throw new RuntimeException("Missing migration for version " + expectedVersion +
                        ", found version " + m.version());
            }
            expectedVersion++;
        }
    }

    protected void executeMigration(MigrationFile migration, int fromVersion) {
        try (final Connection connection = HyphaShop.DATABASE_FACTORY.getConnection()) {
            connection.setAutoCommit(false);
            executeSqlScript(connection, migration);
            connection.commit();
            connection.setAutoCommit(true);
            insertVersion(migration.version(), fromVersion);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute migration V" + migration.version(), e);
        }
    }

    protected void executeSqlScript(@NotNull Connection connection, @NotNull MigrationFile migration) throws Exception {
        final String sql = readSqlContent(migration.filename());
        for (final String command : splitSqlCommands(sql)) {
            try (final Statement stmt = connection.createStatement()) {
                stmt.execute(command);
            }
        }
    }

    protected String readSqlContent(@NotNull String filename) throws IOException {
        return Files.readString(Paths.get(migrationDir, filename));
    }

    protected List<String> splitSqlCommands(@NotNull String sql) {
        return Arrays.stream(sql.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    protected void handleMigrationError(@NotNull Exception e) {
        e.printStackTrace();
        LogUtils.error("Migration failed. Plugin will be disabled for data safety. Please check logs and try again.");
    }

    private void parseMigrationFile(String filename, List<MigrationFile> files) {
        final Pattern pattern = Pattern.compile("V(\\d+)__.*\\.sql");
        final Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            int version = Integer.parseInt(matcher.group(1));
            files.add(new MigrationFile(version, filename));
        }
    }

    private int getCurrentVersion() {
        try {
            final DBVersionSchema schema = HyphaShop.DATABASE_FACTORY.getDBVersionDao().queryLatestSchema();
            return schema == null ? 0 : schema.version();
        } catch (SQLException ignored) {
            return 0;
        }
    }

    private void insertVersion(int newVersion, int fromVersion) throws SQLException {
        HyphaShop.DATABASE_FACTORY.getDBVersionDao().insertSchema(
                new DBVersionSchema(newVersion, fromVersion, new Date()));
    }
}