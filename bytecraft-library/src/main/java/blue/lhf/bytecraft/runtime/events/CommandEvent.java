package blue.lhf.bytecraft.runtime.events;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.byteskript.skript.api.Event;
import org.byteskript.skript.api.note.EventValue;

public class CommandEvent extends Event {
    private final CommandContext<CommandSourceStack> context;

    public CommandEvent(final CommandContext<CommandSourceStack> context) {
        this.context = context;
    }

    @EventValue("sender")
    public CommandSender sender() {
        return context.getSource().getSender();
    }
}
