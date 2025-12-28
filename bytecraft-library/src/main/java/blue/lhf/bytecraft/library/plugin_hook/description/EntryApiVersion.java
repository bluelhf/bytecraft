package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import blue.lhf.bytecraft.library.plugin_hook.PluginDescriptionEntry;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;

@Documentation(
        name = "Plugin API Version",
        description = "Specifies the required API version to write into plugin.yml for the generated JAR.",
        examples = {
                """
                plugin:
                    name: Example Plugin
                    version: 1.0.0
                    api-version: 1.21
                """
        }
)
public class EntryApiVersion extends PluginDescriptionEntry {
    public EntryApiVersion(final ModifiableLibrary provider) {
        super(provider, "api-version");
    }

    @Override
    public void update(final DescriptionTree tree, final String newValue) {
        tree.setApiVersion(newValue);
    }
}

