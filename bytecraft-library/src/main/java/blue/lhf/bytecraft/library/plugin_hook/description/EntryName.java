package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.ByteCraftFlag;
import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import blue.lhf.bytecraft.library.plugin_hook.PluginDescriptionEntry;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.compiler.Context;
import org.byteskript.skript.compiler.Pattern;
import org.byteskript.skript.lang.element.StandardElements;
import mx.kenzie.foundation.compiler.State;

@Documentation(
    name = "Plugin Name",
    description = "Specifies the plugin name to write into plugin.yml for the generated JAR.",
    examples = {
        """
            plugin:
                name: Example Plugin
            """
    }
)
public class EntryName extends PluginDescriptionEntry {
    public EntryName(final ModifiableLibrary provider) {
        super(provider, StandardElements.METADATA, "name: %String%");
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        if (!thing.startsWith("name: ")) return null;
        return new Pattern.Match(Pattern.fakeMatcher(thing), thing.substring("name: ".length()));
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        context.findTree(DescriptionTree.class).setName(match.meta());
    }

    @Override
    public boolean allowedIn(final State state, final Context context) {
        return super.allowedIn(state, context) && context.hasFlag(ByteCraftFlag.IN_PLUGIN_MEMBER);
    }
}

