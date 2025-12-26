package blue.lhf.bytecraft.runtime;

import mx.kenzie.foundation.*;
import mx.kenzie.foundation.language.PostCompileClass;
import org.byteskript.skript.app.ScriptRunner;
import org.byteskript.skript.app.SimpleThrottleController;
import org.byteskript.skript.error.ScriptRuntimeError;
import org.byteskript.skript.runtime.Skript;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;

import java.io.*;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.CodeSource;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for building a Bukkit plugin entrypoint.
 **/
public class BukkitHook {
    private BukkitHook() {}

    public static Collection<PostCompileClass> hookRuntime() throws IOException {
        return Set.of(readClassBytes(Bootstrap.class), buildHook());
    }

    private static PostCompileClass readClassBytes(final Class<?> clazz) throws IOException {
        final String filepath = clazz.getName().replace('.', '/') + ".class";
        try (final InputStream byteStream = clazz.getClassLoader().getResourceAsStream(filepath)) {
            if (byteStream == null) throw new NoSuchFileException(filepath, null, "The class loader for " + clazz + " did not have a resource " + filepath);
            final Type type = new Type(clazz);
            return new PostCompileClass(byteStream.readAllBytes(), type.getTypeName(), type.internalName());
        }
    }

    private static PostCompileClass buildHook() {
        final Label tryLabel = new Label();
        final Label tryEndLabel = new Label();
        final Label catchLabel = new Label();
        final ClassBuilder builder = new ClassBuilder(new Type(BukkitHook.class.getName() + "$CompiledHook"))
                .setSuperclass(new Type("org.bukkit.plugin.java.JavaPlugin"));
        builder
                .addMethod("<init>")
                .writeCode(
                        WriteInstruction.loadThis(),
                        WriteInstruction.invokeSpecial(new Type("org.bukkit.plugin.java.JavaPlugin")),
                        WriteInstruction.returnEmpty()
                ).finish()
                .addMethod("onEnable").setReturnType(void.class)
                .writeCode(
                        WriteInstruction.tryCatch(new Type(Exception.class), tryLabel, tryEndLabel, catchLabel),
                        WriteInstruction.label(tryLabel),

                        WriteInstruction.invokeStatic(new Type(Bootstrap.class), new Type(void.class), "run"),

                        WriteInstruction.jump(tryEndLabel),
                        WriteInstruction.label(catchLabel),
                        WriteInstruction.storeObject(1),
                        WriteInstruction.loadThis(),
                        WriteInstruction.invokeVirtual(builder.getType(), new MethodErasure(Logger.class, "getLogger")),
                        WriteInstruction.getStaticField(new Type(Level.class), new FieldErasure(new Type("java.util.logging.Level"), "SEVERE")),
                        WriteInstruction.push("Failed to run script"),
                        WriteInstruction.loadObject(1),
                        WriteInstruction.invokeVirtual(new Type(Logger.class), new MethodErasure(void.class, "log", Level.class, String.class, Throwable.class)),
                        WriteInstruction.label(tryEndLabel),
                        WriteInstruction.returnEmpty()
                ).finish();

        return new PostCompileClass(builder.compile(), builder.getName(), builder.getInternalName());
    }

    //FIXME: Make ByteSkript's ScriptRunner load scripts using the right class loader (Skript#loadScript)
    /**
     * A bootstrap class that loads all scripts in the current JAR. Code is adapted from {@link ScriptRunner}, which
     * can't be used because it loads classes with the current class loader instead of using the Skript class loader,
     * which causes issues.
     * */
    public static class Bootstrap {
        public static void run() throws Exception {
            final Skript skript = new Skript(null);
            final CodeSource src = Bootstrap.class.getProtectionDomain().getCodeSource();
            if (src == null) {
                throw new ScriptRuntimeError("Unable to access source.");
            }

            final URL jar = src.getLocation();

            try (final ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (!entry.isDirectory()) {
                        final String name = entry.getName();
                        if (name.endsWith(".class") && name.startsWith("skript/")) {
                            try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                                zip.transferTo(out);
                                final byte[] bytes = out.toByteArray();
                                final ClassReader reader = new ClassReader(bytes);
                                skript.loadScript(bytes, reader.getClassName().replace('/', '.'));
                            }
                        }
                    }
                }
            }

            (new SimpleThrottleController(skript)).run();
        }
    }
}
