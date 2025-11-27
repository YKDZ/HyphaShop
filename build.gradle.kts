plugins {
    `java-library`
    id("java")
    id("com.vanniktech.maven.publish") version "0.35.0"
    id("com.gradleup.shadow") version "9.2.2"
}

allprojects {
    project.group = "cn.encmys.ykdz.forest"
    project.version = "0.4.7-Beta"

    apply<JavaPlugin>()
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "org.gradle.maven-publish")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    repositories {
        mavenLocal()
        // Prevent dependabot from pulling packages that have not been updated to Maven CDN we used
        maven("https://cache-redirector.jetbrains.com/repo1.maven.org/maven2")
        //
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://r.irepo.space/maven/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.xenondevs.xyz/releases/")
        maven("https://repo.oraxen.com/releases")
        maven("https://maven.devs.beer/")

        maven {
            url = uri("https://maven.pkg.github.com/ykdz/HyphaShop")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_KEY")
            }
        }

        maven {
            url = uri("https://maven.pkg.github.com/ykdz/HyphaScript")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_KEY")
            }
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}