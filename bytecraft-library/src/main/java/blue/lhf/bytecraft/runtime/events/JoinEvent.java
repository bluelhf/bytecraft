package blue.lhf.bytecraft.runtime.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.byteskript.skript.api.Event;
import org.byteskript.skript.api.note.EventValue;

/**
 * Event triggered when a player joins the server.
 * This event is automatically bridged from Bukkit's PlayerJoinEvent.
 */
@EventBridge.BukkitEvent(PlayerJoinEvent.class)
public class JoinEvent extends Event {
    private final PlayerJoinEvent bukkitEvent;

    public JoinEvent(PlayerJoinEvent bukkitEvent) {
        this.bukkitEvent = bukkitEvent;
    }

    @EventValue("player")
    public Player getPlayer() {
        return bukkitEvent.getPlayer();
    }
}

