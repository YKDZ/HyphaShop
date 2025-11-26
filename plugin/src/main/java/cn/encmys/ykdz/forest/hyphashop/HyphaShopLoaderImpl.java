package cn.encmys.ykdz.forest.hyphashop;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class HyphaShopLoaderImpl implements PluginLoader {
    private static @NotNull MavenLibraryResolver mavenCentralArtifact() {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:7.0.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.google.code.gson:gson:2.13.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.flywaydb:flyway-core:11.14.0"), null));

        resolver.addRepository(new RemoteRepository.Builder("central", "default",
                "https://cache-redirector.jetbrains.com/repo1.maven.org/maven2").build());

        return resolver;
    }

    private static @NotNull MavenLibraryResolver invuiArtifact() {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui:2.0.0-alpha.20"), null));

        resolver.addRepository(
                new RemoteRepository.Builder("xenondevs", "default", "https://repo.xenondevs.xyz/releases").build());

        return resolver;
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        classpathBuilder.addLibrary(mavenCentralArtifact());
        classpathBuilder.addLibrary(invuiArtifact());
    }
}
