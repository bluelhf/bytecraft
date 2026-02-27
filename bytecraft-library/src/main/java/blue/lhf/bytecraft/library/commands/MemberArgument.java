package blue.lhf.bytecraft.library.commands;

import blue.lhf.bytecraft.ByteCraftFlag;
import mx.kenzie.foundation.*;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.TriggerHolder;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.compiler.structure.PreVariable;
import org.byteskript.skript.compiler.structure.SectionMeta;
import org.byteskript.skript.lang.element.StandardElements;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Documentation(
        name = "Argument Member",
        description = """
                Add a parameter to your command.
                
                Write argument sections under your command and nest them to build the shape of the command.
                Each argument has a trigger, which is executed when a command matching that argument is ran.
                
                Inside the trigger you can use the command context (the variable you named in the 'command' line)
                and the values of any arguments you've already declared, e.g. {name}.
                """,
        examples = {
                """
                command greet (ctx):
                  trigger:
                    print "Please specify a name!"
                    return 0
                
                  string argument (name):
                    trigger:
                      print "Hello, " + {name} + "!"
                      return 1
                """,
                """
                command math (ctx):
                  int argument (a):
                    int argument (b):
                      trigger:
                        print {a} + {b}
                        return 1
                """
        }
)
public class MemberArgument extends TriggerHolder {
    enum MemberType {
        INTEGER(java.util.regex.Pattern.compile("int(?:eger)?")),
        STRING(java.util.regex.Pattern.compile("string|text")),
        PLAYER(java.util.regex.Pattern.compile("player"));

        private final java.util.regex.Pattern pattern;

        MemberType(final java.util.regex.Pattern pattern) {
            this.pattern = pattern;
        }
    }

    private record ArgumentDetails(MemberType type, String param) {}

    private static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern.compile(
            "(?<type>" + SkriptLangSpec.IDENTIFIER.pattern() + ") argument ?\\((?<param>" + SkriptLangSpec.IDENTIFIER.pattern() + ")\\)");

    public MemberArgument(final Library provider) {
        super(provider, StandardElements.MEMBER, "(int/string/...) argument (...)");
    }

    @Override
    public String callSiteName(final Context context, final Pattern.Match match) {
        final CommandData data = MemberCommand.getCommandData(context.getSection());
        return MemberCommand.callSiteFor(data.currentNode());
    }

    @Override
    public Type returnType(final Context context, final Pattern.Match match) {
        return CommonTypes.INTEGER;
    }

    @Override
    public Type[] parameters(final Context context, final Matcher match) {
        assert context.getParent() == this;
        final CommandData data = MemberCommand.getCommandData(context.getSection());
        return MemberCommand.buildParameters(data.currentNode());
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        final Matcher matcher = PATTERN.matcher(thing);
        if (matcher.find() && matcher.group("type") != null && matcher.group("param") != null) {
            final ArgumentDetails meta = constructMeta(context, matcher);
            if (meta == null) return null;

            return new Pattern.Match(matcher, meta);
        } else return null;
    }

    private ArgumentDetails constructMeta(final Context context, final Matcher matcher) {
        MemberType type = null;
        for (final MemberType typeCandidate : MemberType.values()) {
            if (typeCandidate.pattern.asMatchPredicate().test(matcher.group("type"))) {
                type = typeCandidate;
                break;
            }
        }
        if (type == null) {
            context.getError().addHint(this, "'" + matcher.group("type").toLowerCase() + "' is not a valid argument type, it should be one of " +
                    Arrays.stream(MemberType.values()).map(MemberType::name).map(String::toLowerCase).collect(Collectors.joining(", ")));
            return null;
        }

        if (!context.hasFlag(ByteCraftFlag.IN_COMMAND_MEMBER)) {
            context.getError().addHint(this, "Arguments must be placed within a command declaration.");
        }

        return new ArgumentDetails(type, matcher.group("param"));
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        final ArgumentDetails details = match.meta();

        // Propagate CommandData up across argument sections
        // (a Tree would work, but they are closed prematurely by TriggerSection#onSectionExit)
        final CommandData data = MemberCommand.getCommandData(context.getSection(1));
        context.getSection(0).getData().add(data);

        final AtomicReference<WriteInstruction> deferredCall = new AtomicReference<>();

        final WriteInstruction call = (method, builder) -> {
            deferredCall.get().accept(method, builder);
            WriteInstruction.invokeVirtual(CommonTypes.INTEGER, new Type(int.class), "intValue").accept(method, builder);
        };

        final CommandNode.Argument node = switch (details.type()) {
            case INTEGER -> CommandNode.integerArgument(data.currentNode(), details.param(), call);
            case STRING -> CommandNode.stringArgument(data.currentNode(), details.param(), call);
            case PLAYER -> CommandNode.playerArgument(data.currentNode(), details.param(), call);
        };

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

    @Override
    public void onSectionExit(final Context context, final SectionMeta meta) {
        MemberCommand.getCommandData(meta).exitNode();
        super.onSectionExit(context, meta);
        context.setState(CompileState.MEMBER_BODY);
    }
}
