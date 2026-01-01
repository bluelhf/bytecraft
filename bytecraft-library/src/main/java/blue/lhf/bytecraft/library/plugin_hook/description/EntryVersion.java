package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import blue.lhf.bytecraft.library.plugin_hook.PluginDescriptionEntry;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.Literal;
import org.byteskript.skript.lang.syntax.literal.StringLiteral;

@Documentation(
        name = "Plugin Version",
        description = "Specifies the plugin version to write into plugin.yml for the generated JAR.",
        examples = MemberPlugin.EXAMPLE
)
public class EntryVersion extends PluginDescriptionEntry<String> {
    public EntryVersion(final ModifiableLibrary provider) {
        super(provider, "version");
    }

    @Override
    public void update(final DescriptionTree tree, final String newValue) {
        tree.setVersion(newValue);
    }

    @Override
    public Literal<String> createLiteral() {
        return new StringLiteral();
    }
}

