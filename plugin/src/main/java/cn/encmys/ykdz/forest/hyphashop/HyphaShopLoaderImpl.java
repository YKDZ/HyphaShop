package cn.encmys.ykdz.forest.hyphashop;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class HyphaShopLoaderImpl implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        classpathBuilder.addLibrary(mavenCentralArtifact());
        classpathBuilder.addLibrary(invuiArtifact());
    }

    private static @NotNull MavenLibraryResolver mavenCentralArtifact() {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:6.3.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.google.code.gson:gson:2.13.1"), null));
        resolver.addDependency(new  Dependency(new DefaultArtifact("org.flywaydb:flyway-core:11.10.3"), null));

        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        return resolver;
    }

    private static @NotNull MavenLibraryResolver invuiArtifact() {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui:2.0.0-alpha.15"), null));

        resolver.addRepository(new RemoteRepository.Builder("xenondevs", "default", "https://repo.xenondevs.xyz/releases").build());

        return resolver;
    }
}
