package blue.lhf.bytecraft.library;

import mx.kenzie.foundation.*;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.syntax.SimpleExpression;
import org.byteskript.skript.compiler.Context;
import org.byteskript.skript.compiler.Pattern.Match;
import org.byteskript.skript.lang.element.StandardElements;

public class ExprServer extends SimpleExpression {
    private final Type serverType;

    public ExprServer(final ModifiableLibrary registrar) {
        super(registrar, StandardElements.EXPRESSION, "[the] [current[ly running]] [bukkit] server");
        this.serverType = registrar.registerType("org.bukkit.Server");
    }

    @Override
    public Type getReturnType() {
        return serverType;
    }

    @Override
    public void compile(final Context context, final Match match) {
        context.getMethod().writeCode(WriteInstruction.invokeStatic(Type.of("org/bukkit/Bukkit"),
                new MethodErasure(serverType, "getServer")));
    }
}
