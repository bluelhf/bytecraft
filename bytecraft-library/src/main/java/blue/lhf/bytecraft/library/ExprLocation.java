package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.runtime.BytecraftExtractedCalls;
import mx.kenzie.foundation.Type;
import org.bukkit.Location;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.syntax.RelationalExpression;
import org.byteskript.skript.lang.element.StandardElements;
import org.byteskript.skript.lang.handler.StandardHandlers;

import java.lang.reflect.Method;

public class ExprLocation extends RelationalExpression {
    public ExprLocation(final Library provider) {
        super(provider, StandardElements.EXPRESSION, "[location] [at] %Number%, %Number%, %Number% in %World%");
        final Method handler = findMethod(BytecraftExtractedCalls.class, "findLocation", Object.class, Object.class, Object.class, Object.class);
        setHandler(StandardHandlers.FIND, handler);
        setHandler(StandardHandlers.GET, handler);
    }

    @Override
    public Type getReturnType() {
        return new Type(Location.class);
    }
}
