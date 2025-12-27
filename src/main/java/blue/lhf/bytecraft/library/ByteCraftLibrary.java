package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.library.plugin_hook.description.*;
import blue.lhf.bytecraft.runtime.BukkitHook;
import mx.kenzie.foundation.language.PostCompileClass;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.compiler.CompileState;

import java.io.IOException;
import java.util.Collection;

/**
 * The ByteCraft library for ByteSkript. Includes syntaxes for interacting with Bukkit as well as a hook
 * that may be declared as the plugin main class in plugin.yml to automatically load all scripts when the plugin
 * is enabled.
 * */
public class ByteCraftLibrary extends ModifiableLibrary {

    private final Collection<PostCompileClass> bukkitHook;

    public ByteCraftLibrary() throws IOException {
        super("ByteCraft Library");
        registerSyntax(CompileState.STATEMENT, new ExprServer(this));
        registerSyntax(CompileState.ROOT, new MemberPlugin(this));
        registerSyntax(CompileState.MEMBER_BODY, new EntryName(this), new EntryVersion(this));
        bukkitHook = BukkitHook.hookRuntime();
    }

    @Override
    public Collection<PostCompileClass> getRuntime() {
        return bukkitHook;
    }
}
