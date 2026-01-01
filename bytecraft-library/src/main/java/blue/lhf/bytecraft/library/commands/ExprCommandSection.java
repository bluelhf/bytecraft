package blue.lhf.bytecraft.library.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import mx.kenzie.foundation.*;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.ExtractedSection;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.compiler.structure.PreVariable;
import org.byteskript.skript.error.ScriptCompileError;
import org.byteskript.skript.lang.element.StandardElements;
import org.objectweb.asm.Handle;

import java.lang.invoke.*;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;

import static java.lang.reflect.Modifier.*;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

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
public class ExprCommandSection extends ExtractedSection {
    private static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern.compile("a? new command ?\\((?<param>" + SkriptLangSpec.IDENTIFIER.pattern() + ")\\)");

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
    public void preCompile(final Context context, final Pattern.Match match) {
        if (!context.isSectionHeader()) throw new ScriptCompileError(context.lineNumber(), "Command section isn't section header");
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) throws Throwable {
        // steps to compile a command section:
        // - ExtractedSection inserts a new tree into the context, parented to the trigger (and its corresponding SectionMeta)
        // - we add a MethodHandles$Lookup as an inner class to open up full-privilege access to the metafactory
        // - incrementing the context's lambda index, we build a new lambda method in the current builder
        //   - the lambda method collects all local variables from the context as parameters
        //   - prior to invoking the lambda, we load all the local variables onto the stack
        // - we write an invokedynamic call to LambdaMetafactory#metafactory
        //   - the first three parameters are loaded by the JVM, the rest is on us
        //     - the parameters are:
        //       1. interface method type - the signature of the method we are implementing
        //       2. implementation - a handle to our implementation of the method
        //       3. dynamic method type - the run-time signature of the method we are implementing. since we can't
        //                                return actual primitive ints as required by Command, this is otherwise
        //                                the same except having a return type of java/lang/Integer.
        // - we give the context a new skip instruction:
        //   - skip instructions are tasks that are run after the current statement
        //   - in our case, we use a skip instruction to set the context's current method builder to be our lambda
        //     so that the code that is within our command section is placed inside the lambda
        // - when the ExtractedSection exits, the default onSectionExit implementation handles resetting the context's
        //   current method to its previous value (set by super.compile()), so we don't need to worry about restoring
        //   the state after our section

        // in our case, we are implementing com/mojang/brigadier/Command run(com/mojang/brigadier/context/CommandContext)I

        super.compile(context, match);
        context.addInnerClass(new Type(MethodHandles.Lookup.class), PUBLIC | STATIC | FINAL);
        final int lambdaIndex = context.getLambdaIndex();
        context.increaseLambdaIndex();

        final String name = "lambda$L" + lambdaIndex;
        final MethodBuilder lambdaBody = context.getBuilder().addMethod(name)
                .setModifiers(Modifier.PUBLIC | Modifier.STATIC)
                .setReturnType(CommonTypes.INTEGER);

        int localIndex = 0;
        for (final PreVariable local : context.getVariables()) {
            // We don't want to ignore internal variables here because that will mess up the
            // local variable slots in the lambda body. ExprSupplierSection does this as of 1.0.41... Maybe
            // that's broken as well?
            lambdaBody.addParameter(Object.class);
            context.getMethod().writeCode(local.load(localIndex));
            localIndex++;
        }

        final Type[] capturedParameters = lambdaBody.getErasure().parameterTypes();

        lambdaBody.addParameter(CommandContext.class);
        final String sourceStackName = match.meta();
        context.getVariable(sourceStackName).parameter = true;

        final MethodErasure lambdaSignature = lambdaBody.getErasure();
        final MethodErasure metafactorySignature = new MethodErasure(findMethod(LambdaMetafactory.class, "metafactory",
                MethodHandles.Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class));

        context.getMethod().writeCode(WriteInstruction.invokeDynamic(
                getReturnType(), "run", capturedParameters,
                new Handle(H_INVOKESTATIC,
                        "java/lang/invoke/LambdaMetafactory",
                        metafactorySignature.name(),
                        metafactorySignature.getDescriptor(),
                        false
                ),
                org.objectweb.asm.Type.getType("(Lcom/mojang/brigadier/context/CommandContext;)I"),
                new Handle(H_INVOKESTATIC, lambdaBody.finish().getInternalName(), lambdaSignature.name(), lambdaSignature.getDescriptor(), false),
                org.objectweb.asm.Type.getType("(Lcom/mojang/brigadier/context/CommandContext;)Ljava/lang/Integer;")
                ));

        this.addSkipInstruction(context, c -> c.setMethod(lambdaBody));
    }

    @Override
    public boolean allowAsInputFor(final Type type) {
        return CommonTypes.OBJECT.equals(type) || CommonTypes.EXECUTABLE.equals(type) || getReturnType().equals(type);
    }
}
