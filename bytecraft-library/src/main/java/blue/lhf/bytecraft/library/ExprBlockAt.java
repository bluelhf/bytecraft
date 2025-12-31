package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.runtime.BytecraftExtractedCalls;
import mx.kenzie.foundation.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.syntax.RelationalExpression;
import org.byteskript.skript.compiler.Context;
import org.byteskript.skript.compiler.Pattern;
import org.byteskript.skript.error.ScriptCompileError;

import static org.byteskript.skript.lang.element.StandardElements.EXPRESSION;

public class ExprBlockAt extends RelationalExpression {
    public ExprBlockAt(final ModifiableLibrary provider) {
        super(provider, EXPRESSION,
                "block at %Location%",
                "block %Integer% (block|met(er|re))[s] [to the] %Cardinal% (from|of) %Location%",
                "block %Integer% (block|met(er|re))[s] [in] %Egocentric% [of] %Location%");
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        switch (match.matchedPattern) {
            case 0 -> writeCall(context.getMethod(), findMethod(Location.class, "getBlock"), context);
            case 1 -> writeCall(context.getMethod(), findMethod(BytecraftExtractedCalls.class, "getRelativeCardinal", Object.class, Object.class, Object.class), context);
            case 2 -> writeCall(context.getMethod(), findMethod(BytecraftExtractedCalls.class, "getRelativeEgocentric", Object.class, Object.class, Object.class), context);
            case 3 -> throw new ScriptCompileError(context.lineNumber(), "Impossible state, matched pattern must be [0, 2] but got " + match.matchedPattern);
        }
    }

    @Override
    public Type getReturnType() {
        return new Type(Block.class);
    }
}
