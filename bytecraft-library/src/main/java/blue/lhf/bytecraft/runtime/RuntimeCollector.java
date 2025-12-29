package blue.lhf.bytecraft.runtime;

import mx.kenzie.foundation.language.PostCompileClass;
import org.byteskript.skript.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;

import static java.nio.file.FileVisitResult.CONTINUE;

public class RuntimeCollector {
    public static List<Resource> collectRuntime(final ProtectionDomain domain, final String prefix) throws IOException {
        final List<Resource> runtime = new ArrayList<>();

        final CodeSource source = domain.getCodeSource();
        if (source == null) throw new IOException("Cannot initialise runtime because code source isn't available");

        final URI base = URI.create("jar:" + source.getLocation());
        try (final FileSystem jarFs = FileSystems.newFileSystem(base, Map.of())) {
            for (final Path root : jarFs.getRootDirectories()) {
                final Path effectivePrefix = root.resolve(prefix);
                Files.walkFileTree(root, new SimpleFileVisitor<>() {
                    @Override
                    public @NotNull FileVisitResult visitFileFailed(@NotNull final Path file, @NotNull final IOException exc) {
                        return CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult postVisitDirectory(@NotNull final Path dir, @Nullable final IOException exc) {
                        return CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult visitFile(@NotNull final Path file, @NotNull final BasicFileAttributes attrs) {
                        if (!file.startsWith(effectivePrefix)) return CONTINUE;
                        final byte[] content;
                        try (final InputStream in = Files.newInputStream(file)) {
                            content = in.readAllBytes();
                        } catch (final IOException e) {
                            return CONTINUE;
                        }

                        if (file.getFileName().toString().endsWith(".class")) {
                            final String name = new ClassReader(content).getClassName();
                            runtime.add(Resource.ofCompiledClass(new PostCompileClass(content, name.replace('/', '.'), name)));
                        } else runtime.add(Resource.ofBytes(file.toString(), content));

                        return CONTINUE;
                    }
                });
            }
        }

        return runtime;
    }
}
