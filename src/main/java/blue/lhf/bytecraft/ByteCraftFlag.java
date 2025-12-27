package blue.lhf.bytecraft;

import blue.lhf.bytecraft.library.plugin_hook.description.EntryName;
import blue.lhf.bytecraft.library.plugin_hook.description.MemberPlugin;
import org.byteskript.skript.api.Flag;

/**
 * ByteSkript compiler context flags that are set by ByteCraft syntaxes.
 * */
public enum ByteCraftFlag implements Flag {
    /**
     * Flag that is set when inside a {@link MemberPlugin} section. Allows related entries, e.g. {@link EntryName} to be used.
     * */
    IN_PLUGIN_MEMBER
    ;
}
