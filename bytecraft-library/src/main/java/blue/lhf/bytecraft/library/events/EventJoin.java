package blue.lhf.bytecraft.library.events;

import blue.lhf.bytecraft.runtime.events.JoinEvent;
import org.byteskript.skript.api.Event;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.EventHolder;

@Documentation(
    name = "Join",
    description = "Run when a player joins the server.",
    examples = {
        """
        on join:
            trigger:
                broadcast mini ("<green>" + (getName() of event-player) + " joined!"
        """
    }
)
public class EventJoin extends EventHolder {
    public EventJoin(final Library provider) {
        super(provider, "on [player] join");
    }

    @Override
    public Class<? extends Event> eventClass() {
        return JoinEvent.class;
    }
}
