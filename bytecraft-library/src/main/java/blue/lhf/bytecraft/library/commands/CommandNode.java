package blue.lhf.bytecraft.library.commands;


import com.google.common.base.Objects;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mx.kenzie.foundation.*;
import org.byteskript.skript.compiler.CommonTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;

import java.lang.invoke.*;
import java.util.*;

import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

/**
 * Represents a node in ByteCraft's internal model of a command tree. A command node is otherwise immutable, except
 * for its set of child nodes, which is mutable. The root node does not have a parent, but all other nodes do.
 * <p>
 * Nodes may be converted to a {@link WriteInstruction} that builds the node using {@link CommandNode#build(ClassBuilder)}.
 * */
public sealed interface CommandNode permits CommandNode.Argument, CommandNode.Literal {
    /**
     * The parent of this node.
     * @return A {@link CommandNode} that is the parent of this node.
     * */
    CommandNode parent();
    /**
     * The children of this node, as a mutable set.
     * @return A mutable {@link Set} of all the children of this node.
     * */
    Set<CommandNode> children();
    /**
     * The label for this node. For arguments, this will be the variable name for the argument value. For literals,
     * this will be the literal text in the command.
     * @return The label
     * */
    String label();

    /**
     * Adds a child to this node's children. Shorthand for calling {@link Set#add(Object)} on {@link CommandNode#children()}
     * @param child The node to add.
     * */
    default void addChild(final CommandNode child) {
        children().add(child);
    }

    /**
     * Collects the chain of {@link Argument} nodes from the root to this node (inclusive),
     * preserving their evaluation order.
     * @return An array of arguments in the order they appear in the command.
     */
    default Argument[] arguments() {
        final List<Argument> arguments = new ArrayList<>();
        CommandNode current = this;
        while (current != null) {
            if (current instanceof final Argument argument)
                arguments.add(argument);

            current = current.parent();
        }

        return arguments.reversed().toArray(new Argument[0]);
    }

    /**
     * A segment of code that consumes a {@link CommandContext} from the operand stack, executes the body
     * of the node, and pushes onto the stack an integer such as {@link Command#SINGLE_SUCCESS} representing the result.
     * <p>
     * This code is then called any time the command represented by this node is called.
     * @return A {@link WriteInstruction} that writes the executor.
     * */
    WriteInstruction executor();

    /**
     * A segment of code that pushes onto the operand stack an {@link ArgumentBuilder} (such as
     * {@link LiteralArgumentBuilder} or {@link RequiredArgumentBuilder}) describing this command node.
     * <p>
     * The code generates an argument builder which is <i>unprepared</i>, i.e. it has no children and is not connected
     * to an executor.
     * @see CommandNode#build(ClassBuilder)
     * @see CommandNode#executor()
     * */
    WriteInstruction nodeBuilder();

    /**
     * A segment of code that pushes onto the operand stack an {@link ArgumentBuilder} describing this command node.
     * <p>
     * The code generates an argument builder which is <i>prepared</i>, i.e. it has children, all of which are themselves
     * prepared, and it has been linked to an executor. Calling {@link ArgumentBuilder#build()} on the node returns a
     * brigadier {@link com.mojang.brigadier.tree.CommandNode} that is ready to be registered as (part of) a command.
     * */
    default WriteInstruction build(final ClassBuilder context) {
        final Type builderType = new Type("com.mojang.brigadier.builder.ArgumentBuilder");
        final WriteInstruction executor = executor();

        final MethodErasure targetErasure = new MethodErasure(new Type(int.class), "run", new Type("com.mojang.brigadier.context.CommandContext"));
        final MethodBuilder lambda =
                context.addMethod("lambda$F" + System.identityHashCode(executor))
                        .addModifiers(PUBLIC, STATIC).setReturnType(int.class)
                        .addParameter(new Type("com.mojang.brigadier.context.CommandContext"))
                        .writeCode(WriteInstruction.loadObject(0))
                        .writeCode(executor).writeCode(WriteInstruction.returnSmall());

        final WriteInstruction[] compiledChildren = children().stream()
                .map(node -> node.build(context)).toArray(WriteInstruction[]::new);

        return (method, builder) -> {
            nodeBuilder().accept(method, builder);

            final MethodErasure metafactorySignature = new MethodErasure(CallSite.class, "metafactory",
                    MethodHandles.Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class);

            final MethodErasure factoryErasure = new MethodErasure(new Type("com.mojang.brigadier.Command"), targetErasure.name());

            builder.visitInvokeDynamicInsn(
                    factoryErasure.name(), factoryErasure.getDescriptor(),
                    new Handle(H_INVOKESTATIC,
                            new Type(LambdaMetafactory.class).internalName(),
                            metafactorySignature.name(),
                            metafactorySignature.getDescriptor(),
                            false
                    ),
                    org.objectweb.asm.Type.getMethodType(targetErasure.getDescriptor()),
                    new Handle(H_INVOKESTATIC, context.getInternalName(), lambda.getErasure().name(), lambda.getErasure().getDescriptor(), false),
                    org.objectweb.asm.Type.getMethodType(targetErasure.getDescriptor())
            );

            WriteInstruction.invokeVirtual(builderType, builderType, "executes", new Type("com.mojang.brigadier.Command")).accept(method, builder);
            for (final WriteInstruction compiledChild : compiledChildren) {
                compiledChild.accept(method, builder);
                WriteInstruction.invokeVirtual(builderType, builderType, "then", builderType).accept(method, builder);
            }
        };
    }

    /**
     * Creates a root node that represents a literal piece of text in the command, i.e. <code>/foo</code>.
     * @param name     The literal label for the root (e.g. <code>foo</code> in <code>/foo</code>).
     * @param executor The code that should be executed when the command represented by this node is executed.
     * @return The created root {@link Literal} node.
     */
    static Literal literal(final String name, final WriteInstruction executor) {
        return literal(null, name, executor);
    }

    /**
     * Creates a node that represents a literal piece of text in the command, i.e. the 'invite' in <code>/party invite Notch</code>.
     * @param parent The node that precedes this node in the command tree. For <code>/party invite</code>, if this node is <code>invite</code>, then its parent should be <code>party</code>.
     * @param text The text of this node, such as <code>invite</code>.
     * @param executor The code that should be executed when the command represented by this node is executed (such as <code>/party invite</code> with no arguments).
     * */
    static Literal literal(final CommandNode parent, final String text, final WriteInstruction executor) {
        return new Literal(parent, new HashSet<>(), text, executor);
    }

    /**
     * Creates a node that represents a string argument in the command, e.g. the player name in
     * <code>/party invite Notch</code>.
     * @param parent The node that precedes this node in the command tree.
     * @param name   The argument's variable name used to retrieve its value from the {@link CommandContext}.
     * @param executor The code that should be executed when the command represented by this node is executed
     *                 (e.g. when all arguments up to and including this one are provided).
     * @return A new {@link StringArgument} node.
     */
    static StringArgument stringArgument(final CommandNode parent, final String name, final WriteInstruction executor) {
        return new StringArgument(parent, new HashSet<>(), name, executor);
    }

    /**
     * Creates a node that represents an integer argument in the command, e.g. the amount in
     * <code>/give diamonds 64</code>.
     * @param parent The node that precedes this node in the command tree.
     * @param name   The argument's variable name used to retrieve its value from the {@link CommandContext}.
     * @param executor The code that should be executed when the command represented by this node is executed
     *                 (e.g. when all arguments up to and including this one are provided).
     * @return A new {@link IntegerArgument} node.
     */
    static IntegerArgument integerArgument(final CommandNode parent, final String name, final WriteInstruction executor) {
        return new IntegerArgument(parent, new HashSet<>(), name, executor);
    }

    /**
     * Represents a parameterised command node that captures a value from the input (e.g. a string or integer).
     * Implementations are responsible for describing their Brigadier argument type and for loading their value
     * from the {@link CommandContext} when the node executes.
     */
    sealed interface Argument extends CommandNode permits StringArgument, IntegerArgument {
        @Override @NotNull
        CommandNode parent();

        /**
         * Emits code that pulls this argument's value from the provided {@link CommandContext} at runtime.
         * The generated code loads the argument name and expected class, then calls
         * {@code CommandContext#getArgument(name, clazz)} and leaves the raw value on the stack.
         * @return A {@link WriteInstruction} that loads the argument value from the context.
         */
        default WriteInstruction loader() {
            return (method, builder) -> {
                WriteInstruction.loadConstant(label()).accept(method, builder);
                WriteInstruction.loadClassConstant(argumentClass()).accept(method, builder);
                WriteInstruction.invokeVirtual(Type.of("com/mojang/brigadier/context/CommandContext"),
                        CommonTypes.OBJECT, "getArgument", CommonTypes.STRING, CommonTypes.CLASS).accept(method, builder);
            };
        }

        /**
         * Emits code that prepares all previously declared arguments for this node, then invokes
         * {@link #executorWithArguments()} to run the user-provided body. The generated code:
         * - Duplicates the {@link CommandContext} reference for each argument in order from root to this node.
         * - Loads and casts each argument value using {@link #loader()} and {@link #argumentClass()}.
         * - Discards the extra context copy and executes the target body.
         * @return A {@link WriteInstruction} that executes this node with its argument values on the stack.
         */
        @Override
        default WriteInstruction executor() {
            return (method, visitor) -> {
                WriteInstruction.duplicate().accept(method, visitor);
                for (final Argument argument : arguments()) {
                    WriteInstruction.duplicate().accept(method, visitor);
                    argument.loader().accept(method, visitor);
                    WriteInstruction.cast(argument.argumentClass()).accept(method, visitor);
                    WriteInstruction.swap().accept(method, visitor);
                }
                WriteInstruction.pop().accept(method, visitor);
                executorWithArguments().accept(method, visitor);
            };
        }

        @Override
        default WriteInstruction nodeBuilder() {
            return (method, builder) -> {
                WriteInstruction.loadConstant(label()).accept(method, builder);
                argumentTypeResolver().accept(method, builder);
                WriteInstruction.invokeStatic(
                        true,
                        new Type("io.papermc.paper.command.brigadier.Commands"),
                        new Type("com.mojang.brigadier.builder.RequiredArgumentBuilder"),
                        "argument", CommonTypes.STRING, new Type("com.mojang.brigadier.arguments.ArgumentType")
                ).accept(method, builder);
            };
        }

        /**
         * The user-provided body for this node that assumes all argument values have already
         * been loaded onto the operand stack in declaration order. Implementations should
         * consume the values they need and leave an int result (e.g. {@link Command#SINGLE_SUCCESS}).
         * @return A {@link WriteInstruction} that runs the command logic for this node.
         */
        WriteInstruction executorWithArguments();

        /**
         * The runtime type of this argument's value used for casting and retrieval from
         * the {@link CommandContext}. For example, {@link CommonTypes#STRING} or {@code new Type(Integer.class)}.
         * @return The {@link Type} representing the argument's Java class.
         */
        Type argumentClass();

        /**
         * Emits code that yields the Brigadier {@code ArgumentType} instance for this argument
         * (e.g. {@code StringArgumentType.string()} or {@code IntegerArgumentType.integer()}).
         * The produced value is passed to Paper's {@code Commands.argument} factory.
         * @return A {@link WriteInstruction} that loads the Brigadier argument type.
         */
        WriteInstruction argumentTypeResolver();
    }

    /**
     * A literal node that matches a fixed token in the command (e.g. "invite").
     * @param parent   the node that precedes this one; may be {@code null} for roots
     * @param children the mutable set of child nodes
     * @param label    the literal text to match
     * @param target   the body to execute when this node is the terminal of the invoked command; may be {@code null}
     */
    record Literal(CommandNode parent, Set<CommandNode> children, String label, @Nullable WriteInstruction target) implements CommandNode {
        @Override
        public WriteInstruction executor() {
            return target;
        }

        @Override
        public WriteInstruction nodeBuilder() {
            return (method, builder) -> {
                WriteInstruction.loadConstant(label()).accept(method, builder);
                WriteInstruction.invokeStatic(
                        true,
                        new Type("io.papermc.paper.command.brigadier.Commands"),
                        new Type("com.mojang.brigadier.builder.LiteralArgumentBuilder"),
                        "literal", CommonTypes.STRING
                ).accept(method, builder);
            };
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(children, label, target);
        }

        @Override
        public @NotNull String toString() {
            return "Literal(" + label + "[" + children + "])";
        }
    }

    /**
     * A command node that captures a string value from the input (e.g. a player name).
     * @param parent the node that precedes this one in the command tree
     * @param children the mutable set of child nodes
     * @param label the argument's variable name used to retrieve its value from the {@link CommandContext}
     * @param executorWithArguments the body to execute when this node is the terminal of the invoked command; it
     *                              expects all prior arguments (including this one) to be on the operand stack
     */
    record StringArgument(@NotNull CommandNode parent, Set<CommandNode> children, String label, WriteInstruction executorWithArguments) implements Argument {
        @Override
        public Type argumentClass() {
            return CommonTypes.STRING;
        }

        @Override
        public WriteInstruction argumentTypeResolver() {
            return WriteInstruction.invokeStatic(
                    new Type("com.mojang.brigadier.arguments.StringArgumentType"),
                    new Type("com.mojang.brigadier.arguments.StringArgumentType"), "string");
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(children, label, executorWithArguments);
        }

        @Override
        public @NotNull String toString() {
            return "StringArgument(" + label + "[" + children + "] -> " + System.identityHashCode(executorWithArguments) + ")";
        }
    }

    /**
     * A command node that captures an integer value from the input (e.g. an amount).
     * @param parent the node that precedes this one in the command tree
     * @param children the mutable set of child nodes
     * @param label the argument's variable name used to retrieve its value from the {@link CommandContext}
     * @param executorWithArguments the body to execute when this node is the terminal of the invoked command; it
     *                              expects all prior arguments (including this one) to be on the operand stack
     */
    record IntegerArgument(@NotNull CommandNode parent, Set<CommandNode> children, String label, WriteInstruction executorWithArguments) implements Argument {
        @Override
        public Type argumentClass() {
            return new Type(Integer.class);
        }

        @Override
        public WriteInstruction argumentTypeResolver() {
            return WriteInstruction.invokeStatic(new Type("com.mojang.brigadier.arguments.IntegerArgumentType"), new Type("com.mojang.brigadier.arguments.IntegerArgumentType"), "integer");
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(children, label, executorWithArguments);
        }

        @Override
        public @NotNull String toString() {
            return "IntegerArgument(" + label + "[" + children + "] -> " + System.identityHashCode(executorWithArguments) + ")";
        }
    }
}