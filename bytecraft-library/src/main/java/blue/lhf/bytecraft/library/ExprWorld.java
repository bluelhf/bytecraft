package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.runtime.BytecraftExtractedCalls;
import mx.kenzie.foundation.Type;
import org.bukkit.World;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.RelationalExpression;
import org.byteskript.skript.lang.element.StandardElements;
import org.byteskript.skript.lang.handler.StandardHandlers;

import java.lang.reflect.Method;

@Documentation(
        name = "World by Name",
        description = """
                Finds a world based on its name, unique id, or namespaced key. The name of the overworld
                in normal Minecraft is 'world'. The nether is 'world_nether' and the end is 'world_the_end'.
                """,
        examples = """
                set {nether} to world "world_nether"
                set {nether_block} to block at 40, 30, -62 in {nether}
                """
)
public class ExprWorld extends RelationalExpression {
    public ExprWorld(final Library provider) {
        super(provider, StandardElements.EXPRESSION, "world %Object%");
        final Method handler = findMethod(BytecraftExtractedCalls.class, "findWorld", Object.class);
        setHandler(StandardHandlers.FIND, handler);
        setHandler(StandardHandlers.GET, handler);
    }

    @Override
    public Type getReturnType() {
        return new Type(World.class);
    }
}
