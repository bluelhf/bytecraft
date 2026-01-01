package blue.lhf.bytecraft.library.directions;

import blue.lhf.bytecraft.runtime.Egocentric;
import mx.kenzie.foundation.Type;
import mx.kenzie.foundation.WriteInstruction;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.Literal;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.lang.element.StandardElements;

import static java.util.Locale.ENGLISH;

@Documentation(
    name = "Egocentric Literal",
    description = """
        A literal representing an egocentric direction. Egocentric directions are determined by the facing of the location
        that they apply to, for example, 'block 1 metre left of getLocation() of {player}' is a different block based on
        which way the player is looking.
        
        Instead of 'above' or 'below', you may mean to use the cardinal directions 'up' and 'down' respectively.
        """,
    examples = {
        """
        set {foo} to above
        """
    }
)
public class EgocentricLiteral extends Literal<Egocentric> {
    private static final String PATTERN;

    static {
        final StringBuilder patternBuilder = new StringBuilder();
        patternBuilder.append("(");
        final Egocentric[] values = Egocentric.values();
        final String[] names = new String[values.length];
        for (int i = 0, facesLength = values.length; i < facesLength; i++) {
            final Egocentric face = values[i];
            names[i] = face.name().toLowerCase(ENGLISH);
        }

        patternBuilder.append(String.join("|", names));
        patternBuilder.append(")");
        PATTERN = patternBuilder.toString();
    }

    public EgocentricLiteral() {
        super(SkriptLangSpec.LIBRARY, StandardElements.EXPRESSION, PATTERN);
    }

    @Override
    public Egocentric parse(final String input) {
        return Egocentric.valueOf(input.toUpperCase(ENGLISH));
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        final String matchedName = match.matcher().group();
        context.getMethod().writeCode(WriteInstruction.getStaticField(getReturnType(), getReturnType(), matchedName.toUpperCase(ENGLISH)));
    }

    @Override
    public Type getReturnType() {
        return new Type(Egocentric.class);
    }
}
