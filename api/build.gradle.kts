plugins {
    id("com.vanniktech.maven.publish") version "0.35.0"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")

    compileOnly("org.bstats:bstats-bukkit:3.1.0")

    compileOnly("me.clip:placeholderapi:2.11.7")

    compileOnly("dev.lone:api-itemsadder:4.0.10")

    compileOnly("xyz.xenondevs.invui:invui:2.0.0-alpha.21")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("org.jetbrains:annotations:26.0.2-1")
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")

    compileOnly("com.google.code.gson:gson:2.13.2")

    compileOnly("cn.encmys.ykdz.forest:hyphascript:0.2.1-beta")

    compileOnly("org.flywaydb:flyway-core:11.19.0")
}

tasks.withType<Jar>().configureEach {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("cn.encmys", "hyphashop-api", project.version.toString())

    pom {
        name.set(rootProject.name + " API")
        description.set("API from Minecraft Paper plugin HyphaShop")
        url.set("https://github.com/YKDZ/HyphaShop/")
        developers {
            developer {
                id.set("ykdz")
                name.set("YKDZ")
                url.set("https://github.com/YKDZ/")
            }
        }
        scm {
            url.set("https://github.com/YKDZ/HyphaShop/")
            connection.set("scm:git:git://github.com/YKDZ/HyphaShop.git")
            developerConnection.set("scm:git:ssh://git@github.com/YKDZ/HyphaShop.git")
        }
        licenses {
            license {
                name.set("GNU General Public License v3.0")
                url.set("https://github.com/YKDZ/cat/blob/main/LICENSE")
                distribution.set("https://github.com/YKDZ/cat/blob/main/LICENSE")
            }
        }
    }
}