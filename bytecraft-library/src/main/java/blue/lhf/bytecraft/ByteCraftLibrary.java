package blue.lhf.bytecraft;

import blue.lhf.bytecraft.library.ExprPlugin;
import blue.lhf.bytecraft.library.ExprServer;
import blue.lhf.bytecraft.library.events.EventEnable;
import blue.lhf.bytecraft.library.plugin_hook.description.*;
import blue.lhf.bytecraft.runtime.*;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.resource.Resource;
import org.byteskript.skript.compiler.CompileState;
import org.byteskript.skript.runtime.Skript;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * The ByteCraft library for ByteSkript. Includes syntaxes for interacting with Bukkit as well as a hook
 * that may be declared as the plugin main class in plugin.yml to automatically load all scripts when the plugin
 * is enabled.
 * */
public class ByteCraftLibrary extends ModifiableLibrary implements BytecraftProvider {

    private final Collection<Resource> runtime = new HashSet<>();

    public ByteCraftLibrary() throws IOException {
        super("ByteCraft Library");
        registerSyntax(CompileState.STATEMENT, new ExprServer(this), new ExprPlugin(this));
        registerSyntax(CompileState.ROOT, new MemberPlugin(this), new EventEnable());
        registerSyntax(CompileState.MEMBER_BODY,
                new EntryName(this), new EntryVersion(this),
                new EntryApiVersion(this), new EntryDescription(this));

        runtime.addAll(RuntimeCollector.collectRuntime(
                ByteCraftLibrary.class.getProtectionDomain(),
                "blue/lhf/bytecraft/runtime/"));

        runtime.addAll(RuntimeCollector.collectRuntime(ByteCraftLibrary.class.getProtectionDomain(), "bytecraft.class"));
    }

    @Override
    public Collection<Resource> getRuntime() {
        return runtime;
    }

    @Override
    public void register(final Skript skript) {
        skript.registerLibrary(this);
    }
}
