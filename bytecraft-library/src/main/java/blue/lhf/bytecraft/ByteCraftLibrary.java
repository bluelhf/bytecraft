package blue.lhf.bytecraft;

import blue.lhf.bytecraft.library.*;
import blue.lhf.bytecraft.library.commands.*;
import blue.lhf.bytecraft.library.directions.CardinalLiteral;
import blue.lhf.bytecraft.library.directions.EgocentricLiteral;
import blue.lhf.bytecraft.library.events.EventEnable;
import blue.lhf.bytecraft.library.plugin_hook.description.*;
import blue.lhf.bytecraft.runtime.Egocentric;
import blue.lhf.bytecraft.runtime.RuntimeCollector;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
        registerSyntax(CompileState.STATEMENT,
                new ExprServer(this),
                new ExprPlugin(this),
                new EgocentricLiteral(),
                new CardinalLiteral(),
                new ExprBlockAt(this),
                new ExprLocation(this),
                new ExprWorld(this),
                new ExprCommandSection(this));

        registerSyntax(CompileState.ROOT, new MemberPlugin(this), new EventEnable(), new MemberCommand(this));

        registerSyntax(CompileState.MEMBER_BODY,
                new EntryName(this),
                new EntryVersion(this),
                new EntryApiVersion(this),
                new EntryDescription(this),
                new MemberArgument(this),
                new MemberSubcommand(this));

        registerTypes(
                Location.class, Block.class, CommandSourceStack.class,
                World.class, BlockFace.class, Egocentric.class);

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
