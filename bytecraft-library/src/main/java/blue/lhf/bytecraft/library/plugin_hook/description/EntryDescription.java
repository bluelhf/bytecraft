package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import blue.lhf.bytecraft.library.plugin_hook.PluginDescriptionEntry;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;

@Documentation(
    name = "Plugin Description",
    description = "Specifies the plugin description to write into plugin.yml for the generated JAR.",
    examples = {
        """
            plugin:
                name: Example Plugin
                description: This is a cool plugin that does stuff.
            """
    }
)
public class EntryDescription extends PluginDescriptionEntry {
    public EntryDescription(final ModifiableLibrary provider) {
        super(provider, "description");
    }

    @Override
    public void update(final DescriptionTree tree, final String newValue) {
        tree.setDescription(newValue);
    }
}

