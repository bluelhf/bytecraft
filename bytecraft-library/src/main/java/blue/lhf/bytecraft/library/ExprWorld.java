package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.runtime.BytecraftExtractedCalls;
import mx.kenzie.foundation.Type;
import org.bukkit.World;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.syntax.RelationalExpression;
import org.byteskript.skript.lang.element.StandardElements;
import org.byteskript.skript.lang.handler.StandardHandlers;

import java.lang.reflect.Method;

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
