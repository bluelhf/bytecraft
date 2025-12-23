package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.runtime.BukkitHook;
import mx.kenzie.foundation.language.PostCompileClass;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.compiler.CompileState;

import java.io.IOException;
import java.util.Collection;

public class ByteCraftLibrary extends ModifiableLibrary {

    private final Collection<PostCompileClass> bukkitHook;

    public ByteCraftLibrary() throws IOException {
        super("ByteCraft Library");
        registerSyntax(CompileState.STATEMENT, new ExprServer(this));
        bukkitHook = BukkitHook.hookRuntime();
    }

    @Override
    public Collection<PostCompileClass> getRuntime() {
        return bukkitHook;
    }
}
