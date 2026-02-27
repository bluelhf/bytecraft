package blue.lhf.bytecraft.library.events;

import blue.lhf.bytecraft.runtime.events.CommandEvent;
import org.byteskript.skript.api.Event;
import org.byteskript.skript.api.Library;
import org.byteskript.skript.api.syntax.EventHolder;

/**
 * Pseudo-event that is automatically injected as the current event in a command trigger.
 * */
public class EventCommand extends EventHolder {
    public EventCommand(final Library provider) {
        super(provider);
    }

    @Override
    public Class<? extends Event> eventClass() {
        return CommandEvent.class;
    }
}
