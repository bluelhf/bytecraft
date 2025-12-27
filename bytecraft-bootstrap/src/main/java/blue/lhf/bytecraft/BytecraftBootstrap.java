package blue.lhf.bytecraft;

import org.byteskript.skript.runtime.Skript;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class BytecraftBootstrap {

    static {
        try {
            LogManager.getLogManager().readConfiguration(BytecraftBootstrap.class.getResourceAsStream("/logging.properties"));
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Logger LOGGER = Logger.getLogger(BytecraftBootstrap.class.getName());

    public static void load(final Skript skript) throws URISyntaxException, IOException {
        final CodeSource source = BytecraftBootstrap.class.getProtectionDomain().getCodeSource();
        if (source == null) throw new IllegalStateException("Cannot find bootstrap code source");
        final URL sourceUrl = source.getLocation();

        LOGGER.info("Loading ByteCraft libraries...");

        final Path tempDir = Files.createTempDirectory("bytecraft");
        final List<URL> collectedUrls = new ArrayList<>();
        try (final FileSystem zip = FileSystems.newFileSystem(new URI("jar:" + sourceUrl.toURI()), Map.of())) {
            final PathMatcher matcher = zip.getPathMatcher("glob:/lib/**.jar");
            for (final Path root : zip.getRootDirectories()) {
                try (final Stream<Path> files = Files.walk(root).filter(matcher::matches)) {
                    for (final Path path : (Iterable<Path>) (files::iterator)) {
                        Path targetPath = tempDir;
                        for (final Path component : path) {
                            targetPath = targetPath.resolve(component.toString());
                        }
                        Files.createDirectories(targetPath.getParent());
                        Files.copy(path, targetPath);
                        collectedUrls.add(targetPath.toUri().toURL());
                    }
                }
            }
        }

        final URLClassLoader classLoader = new URLClassLoader(collectedUrls.toArray(new URL[0]), BytecraftBootstrap.class.getClassLoader());
        ServiceLoader.load(BytecraftProvider.class, classLoader).forEach(provider -> provider.register(skript));
        LOGGER.info("Successfully loaded ByteCraft and " + collectedUrls.size() + " libraries!");
        LOGGER.info("Welcome to ByteCraft.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                classLoader.close();
                Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                    @Override
                    public @NotNull FileVisitResult visitFile(@NotNull final Path file, @NotNull final BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult visitFileFailed(@NotNull final Path file, @NotNull final IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult postVisitDirectory(@NotNull final Path dir, @Nullable final IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (final IOException ignored) {
            }
        }));
    }
}
