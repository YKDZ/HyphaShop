dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")

    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")

    compileOnly("dev.lone:api-itemsadder:4.0.10")

    compileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")

    implementation("xyz.xenondevs.invui:invui:2.0.0-alpha.15")

    implementation("org.bstats:bstats-bukkit:3.1.0")

    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("org.xerial:sqlite-jdbc:3.50.1.0")

    implementation("com.google.code.gson:gson:2.13.1")

    implementation("com.zaxxer:HikariCP:6.3.0")

    implementation("cn.encmys:HyphaScript:0.1.0-Beta")

    implementation("cn.encmys:HyphaUtils:0.1.0-Beta")

    compileOnly("io.lumine:Mythic-Dist:5.9.1") {
        exclude(group = "com.mojang")
    }

    compileOnly("net.kyori:adventure-api:4.22.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.22.0")

    implementation(project(":api"))
}

tasks {
    shadowJar {
        archiveFileName.set(rootProject.name + "-" + project.version + ".jar")
        relocate("org.bstats", "${project.group}.hyphashop.libraries.bstats")
    }
}