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
        name = "Plugin Version",
        description = "Specifies the plugin version to write into plugin.yml for the generated JAR.",
        examples = {
                """
                    plugin:
                        name: Example Plugin
                        version: 1.0.0
                    """
        }
)
public class EntryVersion extends PluginDescriptionEntry {
    public EntryVersion(final ModifiableLibrary provider) {
        super(provider, StandardElements.METADATA, "version: %String%");
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        if (!thing.startsWith("version: ")) return null;
        return new Pattern.Match(Pattern.fakeMatcher(thing), thing.substring("version: ".length()));
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        context.findTree(DescriptionTree.class).setVersion(match.meta());
    }

    @Override
    public boolean allowedIn(final State state, final Context context) {
        return super.allowedIn(state, context) && context.hasFlag(ByteCraftFlag.IN_PLUGIN_MEMBER);
    }
}

