package blue.lhf.bytecraft;

import blue.lhf.bytecraft.library.ByteCraftLibrary;
import org.byteskript.skript.runtime.Skript;

/**
 * The entry point for the ByteCraft library for ByteSkript. Simply registers {@link ByteCraftLibrary} as a library.
 * */
public class ByteCraft {
    /**
     * Entry point for ByteSkript to load ByteCraft. Called by ByteSkript when the resulting .jar
     *      1. Has this class as its Main-Class (defined in pom.xml)
     *      2. Is in the libraries/ directory relative to ByteSkript's working directory
     * */
    public static void load(final Skript skript) throws Exception {
        skript.registerLibrary(new ByteCraftLibrary());
    }
}
