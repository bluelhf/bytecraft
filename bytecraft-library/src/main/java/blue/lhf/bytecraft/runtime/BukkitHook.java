package blue.lhf.bytecraft.runtime;

import mx.kenzie.foundation.*;
import mx.kenzie.foundation.language.PostCompileClass;
import org.bukkit.plugin.java.JavaPlugin;
import org.byteskript.skript.app.ScriptRunner;
import org.byteskript.skript.app.SimpleThrottleController;
import org.byteskript.skript.error.ScriptRuntimeError;
import org.byteskript.skript.runtime.Skript;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.CodeSource;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for building a Bukkit plugin entrypoint.
 **/
public class BukkitHook {

    public static final Type COMPILED_HOOK_TYPE = new Type(BootstrapPlugin.class);

    public static class BootstrapPlugin extends JavaPlugin {
        @Override
        public void onEnable() {
            try {
                Bootstrap.run();
            } catch (final Exception e) {
                getLogger().log(Level.SEVERE, "Failed to run script", e);
            }
        }
    }

    private BukkitHook() {}

    public static Collection<PostCompileClass> hookRuntime() throws IOException {
        return Set.of(readClassBytes(Bootstrap.class), readClassBytes(BootstrapPlugin.class));
    }

    private static PostCompileClass readClassBytes(final Class<?> clazz) throws IOException {
        final String filepath = clazz.getName().replace('.', '/') + ".class";
        try (final InputStream byteStream = clazz.getClassLoader().getResourceAsStream(filepath)) {
            if (byteStream == null) throw new NoSuchFileException(filepath, null, "The class loader for " + clazz + " did not have a resource " + filepath);
            final Type type = new Type(clazz);
            return new PostCompileClass(byteStream.readAllBytes(), type.getTypeName(), type.internalName());
        }
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
