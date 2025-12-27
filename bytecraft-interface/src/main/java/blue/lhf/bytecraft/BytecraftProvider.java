package blue.lhf.bytecraft;

import org.byteskript.skript.runtime.Skript;

/**
 * Simple interface that provides a common ground for the Bytecraft bootstrap and library to communicate.
 * Used as a service provider.
 * */
public interface BytecraftProvider {
    void register(final Skript skript);
}
