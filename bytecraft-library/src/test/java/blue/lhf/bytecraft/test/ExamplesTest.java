package blue.lhf.bytecraft.test;

import blue.lhf.bytecraft.ByteCraftLibrary;
import org.byteskript.skript.compiler.SimpleSkriptCompiler;
import org.byteskript.skript.runtime.Skript;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Logger;

import static java.nio.file.FileVisitResult.CONTINUE;

public class ExamplesTest {
    public static final Logger LOGGER = Logger.getLogger(ExamplesTest.class.getName());
    public static void main(final String... args) throws IOException {
        new ExamplesTest().testExamplesCompile();
    }

    public void testExamplesCompile() throws IOException {
        final Skript skript = new Skript(new SimpleSkriptCompiler(new ByteCraftLibrary()));
        final Path workingDirectory = Path.of("").toAbsolutePath();
        final boolean isInSubmodule = workingDirectory.endsWith("bytecraft-library");
        final Path root = isInSubmodule ? workingDirectory.getParent() : workingDirectory;
        Files.walkFileTree(root.resolve("examples"), new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult visitFileFailed(@NotNull final Path file, @NotNull final IOException exc) {
                return CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult postVisitDirectory(@NotNull final Path dir, @Nullable final IOException exc) throws IOException {
                if (dir.getFileName().toString().equals("skript")) {
                    LOGGER.info("Compiling scripts in " + root.relativize(dir));
                    skript.compileScripts(dir.toFile());
                }
                return CONTINUE;
            }
        });
    }
}
