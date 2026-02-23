package blue.lhf.bytecraft.library.util;

import mx.kenzie.foundation.*;
import org.byteskript.skript.api.LanguageElement;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.syntax.ExtractedSection;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.compiler.structure.PreVariable;
import org.byteskript.skript.error.ScriptCompileError;
import org.objectweb.asm.Handle;

import java.lang.invoke.*;
import java.lang.reflect.Modifier;

import static java.lang.reflect.Modifier.*;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

/**
 * Represents a section that compiles to a lambda, i.e., becomes a 'method object' that implements
 * some {@link FunctionalInterface}. The return value of <code>getReturnType()</code> dictates the interface
 * to be implemented, and the signature returned by <code>getMethodSignature()</code>
 * represents the method signature. That is to say, the return type of this expression should be an interface with
 * a method matching <code>getMethodSignature()</code>
 * <p>
 * Lambda sections compile to an <code>invokedynamic</code> instruction that invokes {@link LambdaMetafactory#metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)},
 * as well as two auxiliaries, an inner class {@link MethodHandles.Lookup} to grant the lambda metafactory full-privilege access, as well as, crucially, a synthetic method with the name <code>lambda$L&lt;some integer&gt;</code>.
 * <p>
 * The lambda metafactory receives three parameters from the JVM, our desired method signatures, as well as a reference to this synthetic method. It returns a {@link CallSite} which builds method objects implementing
 * our <code>lambda$L</code> method. The <code>invokedynamic</code> instruction invokes the call site, which results in a method object being pushed onto the operand stack.
 * <p>
 * A lambda section is an {@link ExtractedSection}, which means the code in the section is extracted into an auxiliary method (our <code>lambda$L</code> method from earlier.)
 * */
public abstract class LambdaSection extends ExtractedSection {
    public LambdaSection(final Library provider, final LanguageElement type, final String... patterns) {
        super(provider, type, patterns);
    }

    @Override
    public void preCompile(final Context context, final Pattern.Match match) throws Throwable {
        super.preCompile(context, match);
        if (!context.isSectionHeader()) throw new ScriptCompileError(context.lineNumber(), "The syntax '" + match.matcher().group() + "' expected to start a section, but did not.");
    }

    public abstract MethodErasure getMethodSignature(final Context context, final Pattern.Match match);

    public MethodErasure getRuntimeMethodSignature(final Context context, final Pattern.Match match) {
        return getMethodSignature(context, match);
    }

    public abstract String getVariableNameForParameter(final Context context, final Pattern.Match match, final int parameterIndex);

    @Override
    public void compile(final Context context, final Pattern.Match match) throws Throwable {

        super.compile(context, match);
        context.addInnerClass(new Type(MethodHandles.Lookup.class), PUBLIC | STATIC | FINAL);
        final int lambdaIndex = context.getLambdaIndex();
        context.increaseLambdaIndex();

        final String lambdaName = "lambda$L" + lambdaIndex;
        final MethodBuilder lambdaBody = context.getBuilder().addMethod(lambdaName)
                .setModifiers(Modifier.PUBLIC | Modifier.STATIC)
                .setReturnType(CommonTypes.INTEGER);

        final Type[] capturedLocals = new Type[context.getVariableCount()];
        for (int i = 0; i < capturedLocals.length; i++) {
            lambdaBody.addParameter(Object.class);
            capturedLocals[i] = context.getVariable(i).getType();
            context.getMethod().writeCode(context.getVariable(i).load(i));
        }

        final MethodErasure factorySignature = new MethodErasure(getReturnType(), lambdaName, capturedLocals);

        final MethodErasure methodSignature = getMethodSignature(context, match);
        final MethodErasure runtimeMethodSignature = getRuntimeMethodSignature(context, match);

        final Type[] methodParameters = methodSignature.parameterTypes();
        for (int i = 0; i < methodParameters.length; i++) {
            lambdaBody.addParameter(methodParameters[i]);
            final String desiredName = getVariableNameForParameter(context, match, i);
            final PreVariable associatedVariable = new PreVariable(desiredName);
            associatedVariable.parameter = true;
            if (context.hasVariable(desiredName))
                throw new ScriptCompileError(context.lineNumber(), "Attempting to use a duplicate variable name '" + desiredName + "'. Shadowing is not currently supported.");
            context.forceUnspecVariable(associatedVariable);
        }

        final MethodErasure lambdaSignature = lambdaBody.getErasure();
        final MethodErasure metafactorySignature = new MethodErasure(findMethod(LambdaMetafactory.class, "metafactory",
                MethodHandles.Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class));

        final String containingClassName = lambdaBody.finish().getInternalName();
        context.getMethod().writeCode((method, visitor) -> visitor.visitInvokeDynamicInsn(
                factorySignature.name(), factorySignature.getDescriptor(),
                new Handle(H_INVOKESTATIC,
                        new Type(LambdaMetafactory.class).internalName(),
                        metafactorySignature.name(),
                        metafactorySignature.getDescriptor(),
                        false
                ),
                org.objectweb.asm.Type.getMethodType(methodSignature.getDescriptor()),
                new Handle(H_INVOKESTATIC, containingClassName, lambdaSignature.name(), lambdaSignature.getDescriptor(), false),
                org.objectweb.asm.Type.getMethodType(runtimeMethodSignature.getDescriptor())
        ));

        this.addSkipInstruction(context, c -> c.setMethod(lambdaBody));
    }

    @Override
    public boolean allowAsInputFor(final Type type) {
        return CommonTypes.OBJECT.equals(type) || CommonTypes.EXECUTABLE.equals(type) || getReturnType().equals(type);
    }
}
