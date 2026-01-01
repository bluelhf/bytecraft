package blue.lhf.bytecraft.library.directions;

import mx.kenzie.foundation.Type;
import mx.kenzie.foundation.WriteInstruction;
import org.bukkit.block.BlockFace;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.Literal;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.lang.element.StandardElements;

import static java.util.Locale.ENGLISH;
import static org.bukkit.block.BlockFace.*;

@Documentation(
    name = "Cardinal Literal",
    description = """
        A literal representing a cardinal direction. Valid values are north, east, south, west, up, and down.
        """,
    examples = {
        """
        set {var} to north
        if {var} is north
        """
    }
)
public class CardinalLiteral extends Literal<BlockFace> {
    private static final BlockFace[] FACES = new BlockFace[]{NORTH, EAST, SOUTH, WEST, UP, DOWN};
    private static final String PATTERN;

    static {
        final StringBuilder patternBuilder = new StringBuilder();
        patternBuilder.append("(");
        final String[] names = new String[FACES.length];
        for (int i = 0, facesLength = FACES.length; i < facesLength; i++) {
            final BlockFace face = FACES[i];
            names[i] = face.name().toLowerCase(ENGLISH);
        }

        patternBuilder.append(String.join("|", names));
        patternBuilder.append(")");
        PATTERN = patternBuilder.toString();
    }

    public CardinalLiteral() {
        super(SkriptLangSpec.LIBRARY, StandardElements.EXPRESSION, PATTERN);
    }

    @Override
    public BlockFace parse(final String input) {
        return BlockFace.valueOf(input.toUpperCase(ENGLISH));
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        final String matchedName = match.matcher().group();
        context.getMethod().writeCode(WriteInstruction.getStaticField(getReturnType(), getReturnType(), matchedName.toUpperCase(ENGLISH)));
    }

    @Override
    public Type getReturnType() {
        return new Type(BlockFace.class);
    }
}
