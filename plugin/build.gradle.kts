dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")

    compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")

    compileOnly("dev.lone:api-itemsadder:4.0.10")

    compileOnly("org.jetbrains:annotations:26.0.2-1")
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")

    compileOnly("xyz.xenondevs.invui:invui:2.0.0-alpha.20")

    implementation("org.bstats:bstats-bukkit:3.1.0")

    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("org.xerial:sqlite-jdbc:3.50.3.0")

    compileOnly("com.google.code.gson:gson:2.13.2")

    compileOnly("com.zaxxer:HikariCP:7.0.2")

    implementation("cn.encmys.ykdz.forest:hyphautils:0.1.0-beta")

    implementation("cn.encmys.ykdz.forest:hyphascript:0.1.0-beta")

    compileOnly("io.lumine:Mythic-Dist:5.10.1") {
        exclude(group = "com.mojang")
    }

    compileOnly("net.kyori:adventure-api:4.25.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")

    compileOnly("org.flywaydb:flyway-core:11.14.0")

    implementation(project(":api"))
}

tasks {
    shadowJar {
        archiveFileName.set(rootProject.name + "-" + project.version + ".jar")
        relocate("org.bstats", "${project.group}.hyphashop.libraries.bstats")
    }
}