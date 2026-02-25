package blue.lhf.bytecraft.library.commands;

import blue.lhf.bytecraft.library.util.LambdaSection;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import mx.kenzie.foundation.MethodErasure;
import mx.kenzie.foundation.Type;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.error.ScriptCompileError;
import org.byteskript.skript.lang.element.StandardElements;

import java.util.regex.Matcher;

@Documentation(
        name = "Command Section",
        description = """
            Creates a [Brigadier](https://github.com/Mojang/brigadier) command executing some code when run.
            This command can be passed as a parameter to the `executes()` method of a Brigadier command builder.
            
            Command sections accept one parameter, the command context. This parameter is stored in a variable, the name
            of which is given in parentheses similar to how ByteSkript functions are defined. Only one parameter
            may be specified.
            
            Commands should return an integer describing their result. A value of 1 denotes success.
            For more details, see the [Custom Plugin example](https://github.com/bluelhf/bytecraft/blob/8a092d265b8283b56f3f76fe82a05f08d383ad7d/examples/plugin/skript/index.bsk)
            on the bytecraft repository.
            """,
        examples = {
                """
                set {my_command} to a new command (my_context):
                    // Sends "Hello, world!" to the executor of the command.
                    run sendMessage("Hello, world!") of getSender() of getSource() of {my_context}
                    return 1
                """
        }
)
public class ExprCommandSection extends LambdaSection {
    private static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern.compile(
            "a? new command ?\\((?<param>" + SkriptLangSpec.IDENTIFIER.pattern() + ")\\)");

    public ExprCommandSection(final Library provider) {
        super(provider, StandardElements.EXPRESSION, "[a] new command (...)");
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        final Matcher matcher = PATTERN.matcher(thing);
        if (matcher.find() && matcher.group("param") != null) {
            return new Pattern.Match(matcher, matcher.group("param"));
        } else return null;
    }

    @Override
    public Type getReturnType() {
        return new Type(Command.class);
    }

    @Override
    public MethodErasure getMethodSignature(final Context context, final Pattern.Match match) {
        return new MethodErasure(new Type(int.class), "run", new Type(CommandContext.class));
    }

    @Override
    public MethodErasure getRuntimeMethodSignature(final Context context, final Pattern.Match match) {
        return new MethodErasure(new Type(Integer.class), "run", new Type(CommandContext.class));
    }

    @Override
    public String getVariableNameForParameter(final Context context, final Pattern.Match match, final int parameterIndex) {
        if (parameterIndex != 0) throw new ScriptCompileError(context.lineNumber(),
                "Command section should have one parameter (#0), but parameter #" + parameterIndex + " was requested.");
        return match.meta();
    }
}
