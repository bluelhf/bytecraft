package blue.lhf.bytecraft.library.commands;

import mx.kenzie.foundation.*;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.TriggerHolder;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.compiler.Pattern;
import org.byteskript.skript.compiler.CommonTypes;
import org.byteskript.skript.compiler.structure.PreVariable;
import org.byteskript.skript.compiler.structure.SectionMeta;
import org.byteskript.skript.lang.element.StandardElements;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

@Documentation(
        name = "Subcommand Member",
        description = """
                Declares a literal subcommand within a command's tree, for example the `invite` in `/party invite <player>`.

                Write subcommand sections under your command and nest them to build the shape of the command.
                Triggers inside a subcommand may access the command context and any previously-declared arguments.
                """,
        examples = {
                """
                command party (ctx):
                  trigger:
                    print "Usage: /party invite <player>"
                    return 0

                  subcommand invite:
                    trigger:
                      print "Inviting..."
                      return 1

                    string argument (player):
                      trigger:
                        print "Invited " + {player}
                        return 1
                """
        }
)
public class MemberSubcommand extends TriggerHolder {
    private static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern.compile(
            "subcommand (?<label>" + SkriptLangSpec.IDENTIFIER.pattern() + ")");

    public MemberSubcommand(final Library provider) {
        super(provider, StandardElements.MEMBER, "subcommand ...");
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        final Matcher matcher = PATTERN.matcher(thing);
        if (matcher.find() && matcher.group("label") != null) {
            return new Pattern.Match(matcher, matcher.group("label"));
        } else return null;
    }

    @Override
    public String callSiteName(final Context context, final Pattern.Match match) {
        final CommandData data = getCommandData(context.getSection());
        return MemberCommand.callSiteFor(data.currentNode());
    }

    @Override
    public Type returnType(final Context context, final Pattern.Match match) {
        return CommonTypes.INTEGER;
    }

    @Override
    public Type[] parameters(final Context context, final Matcher match) {
        assert context.getParent() == this;
        final CommandData data = getCommandData(context.getSection());
        return MemberCommand.buildParameters(data.currentNode());
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        final String label = match.meta();

        // Propagate CommandData up across subcommand sections
        final CommandData data = getCommandData(context.getSection(1));
        context.getSection(0).getData().add(data);

        final AtomicReference<WriteInstruction> deferredCall = new AtomicReference<>();
        final CommandNode.Literal node = CommandNode.literal(data.currentNode(), label, (method, builder) -> {
            deferredCall.get().accept(method, builder);
            WriteInstruction.invokeVirtual(CommonTypes.INTEGER, new Type(int.class), "intValue").accept(method, builder);
        });

        data.enterNode(node);
        final MethodErasure signature = new MethodErasure(returnType(context, match), callSiteName(context, match), parameters(context, match.matcher()));
        deferredCall.set(WriteInstruction.invokeStatic(context.getBuilder().getType(), signature));

        final PreVariable contextVariable = new PreVariable(data.getContextVariable());
        contextVariable.parameter = true;
        context.forceUnspecVariable(contextVariable);

        for (final CommandNode.Argument argument : node.arguments()) {
            final PreVariable variable = new PreVariable(argument.label());
            variable.parameter = true;
            context.forceUnspecVariable(variable);
        }

        // Must be after the command data is added to the section
        super.compile(context, match);
    }

    private @NotNull CommandData getCommandData(final SectionMeta meta) {
        return meta.getData().stream()
                .filter(CommandData.class::isInstance)
                .map(CommandData.class::cast).findFirst()
                .orElseThrow();
    }

    @Override
    public void onSectionExit(final Context context, final SectionMeta meta) {
        getCommandData(meta).exitNode();
        super.onSectionExit(context, meta);
        context.setState(CompileState.MEMBER_BODY);
    }
}
