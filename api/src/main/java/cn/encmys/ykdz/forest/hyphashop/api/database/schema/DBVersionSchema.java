package cn.encmys.ykdz.forest.hyphashop.api.database.schema;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public record DBVersionSchema(int version, int migrationFromVersion, @NotNull Date latestMigrationTime) {
}
