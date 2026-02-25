package blue.lhf.bytecraft.library.commands;

import blue.lhf.bytecraft.runtime.BukkitHook;
import blue.lhf.bytecraft.runtime.Enable;
import mx.kenzie.foundation.*;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.TriggerHolder;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.compiler.structure.PreVariable;
import org.byteskript.skript.compiler.structure.SectionMeta;
import org.byteskript.skript.lang.element.StandardElements;
import org.byteskript.skript.runtime.data.EventData;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;

import java.lang.invoke.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

import static blue.lhf.bytecraft.ByteCraftFlag.IN_COMMAND_MEMBER;
import static java.lang.reflect.Modifier.*;
import static org.byteskript.skript.compiler.CompileState.MEMBER_BODY;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

@Documentation(
        name = "Command Member",
        description = """
                Declares a Minecraft command to be included with the plugin. The command is enabled
                when the plugin is first enabled. Command arguments are defined using a tree structure, similar
                to how Mojang defines its own commands. Triggers within commands return numbers; 1 represents a success.
                
                Currently, using a command member causes an error when enabling the plugin. Command members are not fully
                implemented yet.
                """,
        examples = {
                """
                command foo (my_context):
                  trigger:
                    // This is sent to console.
                    print "Please specify a name!"
                    return 0
                
                  // The following is not supported yet, but gives an idea of how the command tree will work.
                  text argument (name):
                    trigger:
                      print "Hello, " + {name} + "!" // also sent to console.
                      return 1
                """
        }
)
public class MemberCommand extends TriggerHolder {
    private static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern.compile(
            "command (?<name>" + SkriptLangSpec.IDENTIFIER.pattern() + ") ?\\((?<param>" + SkriptLangSpec.IDENTIFIER.pattern() + ")\\)");

    public MemberCommand(final Library provider) {
        super(provider, StandardElements.MEMBER, "command(...)");
    }

    public static String callSiteFor(final CommandNode commandNode) {
        CommandNode current = commandNode;
        final StringBuilder builder = new StringBuilder();
        while (current != null) {
            builder.insert(0, current.label() + "$");
            current = current.parent();
        }
        return "command$callSite$" + builder.substring(0, builder.length() - 1);
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        final Matcher matcher = PATTERN.matcher(thing);
        if (matcher.find() && matcher.group("param") != null) {
            final CommandDetails details = new CommandDetails(matcher.group("name"), matcher.group("param"));
            return new Pattern.Match(matcher, details);
        } else return null;
    }

    @NotNull
    static CommandData getCommandData(final SectionMeta meta) {
        return meta.getData().stream()
                .filter(CommandData.class::isInstance)
                .map(CommandData.class::cast).findFirst()
                .orElseThrow();
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        context.addFlag(IN_COMMAND_MEMBER);
        context.setState(MEMBER_BODY);
        final CommandDetails details = match.meta();
        final PreVariable contextVariable = new PreVariable(details.contextVariable());
        contextVariable.parameter = true;
        context.forceUnspecVariable(contextVariable);

        final AtomicReference<WriteInstruction> deferredWrite = new AtomicReference<>();
        final CommandData data = new CommandData(CommandNode.literal(null,
                details.name(), (method, builder) -> deferredWrite.get().accept(method, builder)), details.contextVariable());

        context.getSection().getData().add(data);
        final MethodErasure signature = new MethodErasure(returnType(context, match), callSiteName(context, match), parameters(context, match.matcher()));
        deferredWrite.set((method, builder) -> {
            WriteInstruction.invokeStatic(context.getBuilder().getType(), signature).accept(method, builder);
            WriteInstruction.invokeVirtual(CommonTypes.INTEGER, new Type(int.class), "intValue").accept(method, builder);
        });
        super.compile(context, match);
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
        return new Type[]{new Type("com.mojang.brigadier.context.CommandContext")};
    }

    @Override
    public void onSectionExit(final Context context, final SectionMeta meta) {
        final CommandData data = meta.getData().stream().filter(CommandData.class::isInstance).map(CommandData.class::cast).findFirst().orElseThrow();
        compileSyntheticCommand(context, meta, data);

        context.removeFlag(IN_COMMAND_MEMBER);
        context.closeAllTrees();
        super.onSectionExit(context, meta);
    }

    private void compileSyntheticCommand(final Context context, final SectionMeta meta, final CommandData data) {
        context.addInnerClass(new Type(MethodHandles.Lookup.class), PUBLIC | STATIC | FINAL);
        final int lambdaIndex = context.getLambdaIndex();
        context.increaseLambdaIndex();

        final String lambdaName = "lambda$L" + lambdaIndex;
        final MethodBuilder lambdaBody = context.getBuilder().addMethod(lambdaName)
                .setModifiers(Modifier.PUBLIC | Modifier.STATIC)
                .setReturnType(new Type(void.class));

        // zero captured locals
        lambdaBody.addParameter(new Type("io.papermc.paper.plugin.lifecycle.event.LifecycleEvent")); // generic parameter upper bound for Event

        lambdaBody.writeCode(
                WriteInstruction.loadObject(0), // load Event parameter
                WriteInstruction.cast(new Type("io.papermc.paper.plugin.lifecycle.event.registrar.RegistrarEvent")), // cast to registrar event
                WriteInstruction.invokeInterface(new Type("io.papermc.paper.plugin.lifecycle.event.registrar.RegistrarEvent"),
                        new Type("io.papermc.paper.plugin.lifecycle.event.registrar.Registrar"), "registrar"),

                data.getRoot().build(context.getBuilder()),
                WriteInstruction.cast(new Type("com.mojang.brigadier.builder.LiteralArgumentBuilder")),
                WriteInstruction.invokeVirtual(new Type("com.mojang.brigadier.builder.LiteralArgumentBuilder"), new Type("com.mojang.brigadier.tree.LiteralCommandNode"), "build"),
                WriteInstruction.invokeInterface(new Type("io.papermc.paper.command.brigadier.Commands"), new Type("java.util.Set"), "register", new Type("com.mojang.brigadier.tree.LiteralCommandNode")),
                WriteInstruction.returnEmpty()
        );

        final MethodErasure lambdaSignature = lambdaBody.getErasure();
        final MethodErasure factorySignature = new MethodErasure(Type.of("io/papermc/paper/plugin/lifecycle/event/handler/LifecycleEventHandler"), "run");
        final MethodErasure methodSignature = new MethodErasure(new Type(void.class), "run", new Type("io.papermc.paper.plugin.lifecycle.event.LifecycleEvent"));

        final MethodErasure metafactorySignature = new MethodErasure(findMethod(LambdaMetafactory.class, "metafactory",
                MethodHandles.Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class));

        final String containingClassName = lambdaBody.finish().getInternalName();

        context.getBuilder().addMethod("registerCommand$" + data.getRoot().label())
                .addModifiers(PUBLIC, STATIC).setReturnType(new Type(void.class))
                .addAnnotation(EventData.class).setVisible(true)
                .addValue("name", name())
                .addValue("event", Enable.class.getName())
                .addValue("async", false).finish()
                .writeCode(
                        WriteInstruction.loadClassConstant(BukkitHook.COMPILED_HOOK_TYPE),
                        WriteInstruction.invokeStatic(Type.of("org/bukkit/plugin/java/JavaPlugin"),
                                new MethodErasure(Type.of("org/bukkit/plugin/java/JavaPlugin"), "getPlugin", Type.of(Class.class))),

                        WriteInstruction.invokeVirtual(Type.of("org/bukkit/plugin/java/JavaPlugin"),
                                new MethodErasure(Type.of("io/papermc/paper/plugin/lifecycle/event/LifecycleEventManager"), "getLifecycleManager")),

                        WriteInstruction.getStaticField(Type.of("io/papermc/paper/plugin/lifecycle/event/types/LifecycleEvents"),
                                Type.of("io/papermc/paper/plugin/lifecycle/event/types/LifecycleEventType$Prioritizable"), "COMMANDS")
                ).writeCode((method, visitor) -> visitor.visitInvokeDynamicInsn(
                        factorySignature.name(), factorySignature.getDescriptor(),
                        new Handle(H_INVOKESTATIC,
                                new Type(LambdaMetafactory.class).internalName(),
                                metafactorySignature.name(),
                                metafactorySignature.getDescriptor(),
                                false
                        ),
                        org.objectweb.asm.Type.getMethodType(methodSignature.getDescriptor()),
                        new Handle(H_INVOKESTATIC, containingClassName, lambdaSignature.name(), lambdaSignature.getDescriptor(), false),
                        org.objectweb.asm.Type.getMethodType(methodSignature.getDescriptor())
                )).writeCode(
                        WriteInstruction.invokeInterface(Type.of("io/papermc/paper/plugin/lifecycle/event/LifecycleEventManager"), new Type(void.class), "registerEventHandler",
                                Type.of("io/papermc/paper/plugin/lifecycle/event/types/LifecycleEventType"),
                                Type.of("io/papermc/paper/plugin/lifecycle/event/handler/LifecycleEventHandler"))
                ).writeCode(WriteInstruction.returnEmpty());
    }

    public static Type[] buildParameters(final CommandNode node) {
        final List<Type> parameters = new ArrayList<>();
        parameters.add(new Type("com.mojang.brigadier.context.CommandContext"));
        for (final CommandNode.Argument argument : node.arguments())
            parameters.add(argument.argumentClass());

        return parameters.toArray(Type[]::new);
    }

    public static Type[] buildParameters(final CommandNode parent, final Type argument) {
        final List<Type> parameters = new ArrayList<>();
        parameters.add(new Type("com.mojang.brigadier.context.CommandContext"));
        for (final CommandNode.Argument sub : parent.arguments())
            parameters.add(sub.argumentClass());
        parameters.add(argument);
        return parameters.toArray(Type[]::new);
    }

    /**
     * Details about the registration of a command, such as its name and the label associated with its context.
     * @param name The name of the command, e.g. "foo" for <code>/foo</code>.
     * @param contextVariable The name of the command's context label, e.g. "my_context" for a command defined as <code>command foo (my_context):</code>
     * */
    private record CommandDetails(String name, String contextVariable) {
    }
}
