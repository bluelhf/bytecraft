package blue.lhf.bytecraft.library.events;

import blue.lhf.bytecraft.runtime.events.EnableEvent;
import org.byteskript.skript.api.Event;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.EventHolder;

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
    public EventEnable(final Library provider) {
        super(provider, "on [plugin] enable");
    }

    @Override
    public Class<? extends Event> eventClass() {
        return EnableEvent.class;
    }
}

