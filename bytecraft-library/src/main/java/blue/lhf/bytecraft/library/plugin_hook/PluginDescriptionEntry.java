package blue.lhf.bytecraft.library.plugin_hook;

import blue.lhf.bytecraft.ByteCraftFlag;
import blue.lhf.bytecraft.library.plugin_hook.description.MemberPlugin;
import mx.kenzie.foundation.compiler.State;
import org.byteskript.skript.api.LanguageElement;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.syntax.SimpleEntry;
import org.byteskript.skript.compiler.Context;
import org.byteskript.skript.compiler.Pattern;
import org.byteskript.skript.lang.element.StandardElements;

/**
 * Represents an entry that may appear as part of a plugin description in {@link MemberPlugin}.
 * */
public abstract class PluginDescriptionEntry extends SimpleEntry {
    private final String name;

    public PluginDescriptionEntry(final Library provider, final String name) {
        super(provider, StandardElements.METADATA, name + ": %String%");
        this.name = name;
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        if (!thing.startsWith(name + ": ")) return null;
        return new Pattern.Match(Pattern.fakeMatcher(thing), thing.substring((name + ": ").length()));
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        update(context.findTree(DescriptionTree.class), match.meta());
    }

    @Override
    public boolean allowedIn(final State state, final Context context) {
        return super.allowedIn(state, context) && context.hasFlag(ByteCraftFlag.IN_PLUGIN_MEMBER);
    }

    public abstract void update(final DescriptionTree tree, final String newValue);
}
