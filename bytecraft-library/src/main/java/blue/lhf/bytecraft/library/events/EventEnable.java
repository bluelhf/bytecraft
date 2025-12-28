package blue.lhf.bytecraft.library.events;

import blue.lhf.bytecraft.runtime.Enable;
import org.byteskript.skript.api.Event;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.EventHolder;
import org.byteskript.skript.compiler.SkriptLangSpec;

@Documentation(
        name = "Enable",
        description = "Run when the plugin is enabled.",
        examples = {
                """
                on enable:
                    trigger:
                        print "Hello from " + getName() of plugin
                """
        }
)
public class EventEnable extends EventHolder {
    public EventEnable() {
        super(SkriptLangSpec.LIBRARY, "on [plugin] enable");
    }

    @Override
    public Class<? extends Event> eventClass() {
        return Enable.class;
    }
}

