package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import blue.lhf.bytecraft.library.plugin_hook.PluginDescriptionEntry;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.Literal;
import org.byteskript.skript.lang.syntax.literal.StringLiteral;

@Documentation(
        name = "Plugin API Version",
        description = "Specifies the required API version to write into plugin.yml for the generated JAR.",
        examples = MemberPlugin.EXAMPLE
)
public class EntryApiVersion extends PluginDescriptionEntry<String> {
    public EntryApiVersion(final ModifiableLibrary provider) {
        super(provider, "api-version");
    }

    @Override
    public void update(final DescriptionTree tree, final String newValue) {
        tree.setApiVersion(newValue);
    }

    @Override
    public Literal<String> createLiteral() {
        return new StringLiteral();
    }
}

