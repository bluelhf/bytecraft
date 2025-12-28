package blue.lhf.bytecraft.library;

import blue.lhf.bytecraft.runtime.BukkitHook;
import mx.kenzie.foundation.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.SimpleExpression;
import org.byteskript.skript.compiler.Context;
import org.byteskript.skript.compiler.Pattern.Match;
import org.byteskript.skript.lang.element.StandardElements;

@Documentation(
        name = "Current Plugin",
        description = """
                Returns the plugin object for the plugin represented by this script.
                """,
        examples = {
                """
                broadcast "Hello from " + getName() of plugin + "."
                """
        }
)
public class ExprPlugin extends SimpleExpression {
    private final Type pluginType;

    public ExprPlugin(final ModifiableLibrary registrar) {
        super(registrar, StandardElements.EXPRESSION, "[the] [current[ly running]] plugin");
        this.pluginType = registrar.registerType(JavaPlugin.class);
    }

    @Override
    public Type getReturnType() {
        return pluginType;
    }

    @Override
    public void compile(final Context context, final Match match) {
        context.getMethod().writeCode(
                WriteInstruction.loadClassConstant(BukkitHook.COMPILED_HOOK_TYPE),
                WriteInstruction.invokeStatic(Type.of("org/bukkit/plugin/java/JavaPlugin"),
                        new MethodErasure(pluginType, "getPlugin", Type.of(Class.class)))
        );
    }
}
