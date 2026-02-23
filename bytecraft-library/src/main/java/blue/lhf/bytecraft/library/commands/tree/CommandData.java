package blue.lhf.bytecraft.library.commands.tree;

import org.byteskript.skript.compiler.structure.BasicTree;
import org.byteskript.skript.compiler.structure.SectionMeta;

/**
 * Data related to a command tree, such as the command tree's root name, context variable name, arguments, and so on.
 * This is used to construct the code that registers the command with Brigadier.
 * <p>
 * These are recorded as additional data on the command member's {@link SectionMeta}. They <i>should</i> be a {@link BasicTree},
 * but are not because the trigger holder erroneously clears all trees instead of only its child trees when it exits.
 * @see MemberCommand
 * */
public class CommandData {
    private final CommandDetails details;

    public CommandData(final CommandDetails details) {
        this.details = details;
    }

    public CommandDetails getDetails() {
        return details;
    }
}
