package blue.lhf.bytecraft.library.commands;

import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.syntax.Member;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.lang.element.StandardElements;

public class MemberCommand extends Member {
    public MemberCommand(final ModifiableLibrary provider) {
        super(provider, StandardElements.MEMBER, "command");
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        if (!"command".equals(thing)) return null;
        if (context.hasFlag(AreaFlag.IN_TYPE)) {
            context.getError().addHint(this, "Commands must be root-level elements.");
            return null;
        }

        return super.match(thing, context);
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
    }
}
