package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.runtime.BytecraftExtractedCalls;
import mx.kenzie.foundation.Type;
import org.bukkit.Location;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.RelationalExpression;
import org.byteskript.skript.compiler.Context;
import org.byteskript.skript.compiler.Pattern;
import org.byteskript.skript.lang.element.StandardElements;
import org.byteskript.skript.lang.handler.StandardHandlers;

import java.lang.reflect.Method;

@Documentation(
        name = "Location At",
        description = """
            Finds a location in a world based on the X, Y, and Z-coordinates. In normal Minecraft, the overworld is
            a world called 'world'.
            """,
        examples = {
                """
                set {location} to the location at 62, 92, -43 in world "world"
                """
        }
)
public class ExprLocation extends RelationalExpression {
    public ExprLocation(final Library provider) {
        super(provider, StandardElements.EXPRESSION, "[the] [location] [at] %Number%, %Number%, %Number% in %World%");
        final Method handler = findMethod(BytecraftExtractedCalls.class, "findLocation", Object.class, Object.class, Object.class, Object.class);
        setHandler(StandardHandlers.FIND, handler);
        setHandler(StandardHandlers.GET, handler);
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        if (!thing.contains(", ") || !thing.contains("in")) return null;
        return super.match(thing, context);
    }

    @Override
    public Type getReturnType() {
        return new Type(Location.class);
    }
}
