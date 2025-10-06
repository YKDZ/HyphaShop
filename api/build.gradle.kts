java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.9-pre4-R0.1-SNAPSHOT")

    compileOnly("org.bstats:bstats-bukkit:3.1.0")

    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("dev.lone:api-itemsadder:4.0.10")

    compileOnly("xyz.xenondevs.invui:invui:2.0.0-alpha.19")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("org.jetbrains:annotations:26.0.2-1")
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")

    compileOnly("com.google.code.gson:gson:2.13.1")

    compileOnly("cn.encmys.ykdz.forest:hyphascript:0.1.0-beta")

    compileOnly("org.flywaydb:flyway-core:11.13.2")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set(rootProject.name + "-api-" + project.version + ".jar")
    }
}