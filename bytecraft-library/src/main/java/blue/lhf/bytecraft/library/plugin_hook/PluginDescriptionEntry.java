package blue.lhf.bytecraft.library.plugin_hook;

import blue.lhf.bytecraft.library.plugin_hook.description.MemberPlugin;
import org.byteskript.skript.api.LanguageElement;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.syntax.SimpleEntry;

/**
 * Represents an entry that may appear as part of a plugin description in {@link MemberPlugin}.
 * */
public abstract class PluginDescriptionEntry extends SimpleEntry {
    public PluginDescriptionEntry(final Library provider, final LanguageElement type, final String... patterns) {
        super(provider, type, patterns);
    }
}
