package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import blue.lhf.bytecraft.library.plugin_hook.PluginDescriptionEntry;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;

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
        super(provider, "version");
    }

    @Override
    public void update(final DescriptionTree tree, final String newValue) {
        tree.setVersion(newValue);
    }
}

