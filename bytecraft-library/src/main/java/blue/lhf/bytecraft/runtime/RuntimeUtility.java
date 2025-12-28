package blue.lhf.bytecraft.runtime;

import mx.kenzie.foundation.Type;
import mx.kenzie.foundation.language.PostCompileClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

public class RuntimeUtility {
    private RuntimeUtility() {}

    public static PostCompileClass readClassBytes(final Class<?> clazz) throws IOException {
        final String filepath = clazz.getName().replace('.', '/') + ".class";
        try (final InputStream byteStream = clazz.getClassLoader().getResourceAsStream(filepath)) {
            if (byteStream == null) throw new NoSuchFileException(filepath, null, "The class loader for " + clazz + " did not have a resource " + filepath);
            final Type type = new Type(clazz);
            return new PostCompileClass(byteStream.readAllBytes(), type.getTypeName(), type.internalName());
        }
    }
}
