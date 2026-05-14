package blue.lhf.bytecraft.runtime.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.byteskript.skript.runtime.Skript;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.logging.Level;

/**
 * Bridge system that automatically hooks Bytecraft events to corresponding Bukkit events.
 * <p>
 * Usage:
 * 1. Annotate your Bytecraft event class with {@code @BukkitEvent(PlayerJoinEvent.class)}
 * 2. Call {@link EventBridge#registerBridge(Class, Plugin, Skript)} to automatically register the Bukkit listener
 * 3. The bridge will convert Bukkit events to Bytecraft events and trigger them in Skript
 * <p>
 * The bridge uses reflection to:
 * - Instantiate the Bytecraft event with the Bukkit event context
 * - Register a generic Bukkit event listener that forwards events to Bytecraft
 */
public abstract class EventBridge implements Listener {
    
    /**
     * Annotation to mark a Bytecraft event class as bridged to a specific Bukkit event.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface BukkitEvent {
        Class<? extends org.bukkit.event.Event> value();
    }
    
    /**
     * Registers a bridge for the given Bytecraft event class.
     * 
     * @param eventClass The Bytecraft event class
     * @throws Exception if the class cannot be loaded or is not properly annotated
     */
    public static <E extends org.byteskript.skript.api.Event> void registerBridge(Class<E> eventClass, org.bukkit.plugin.Plugin plugin, Skript skript) throws Exception {
        final BukkitEvent bridge = eventClass.getAnnotation(BukkitEvent.class);
        final Class<? extends org.bukkit.event.Event> bukkitEvent = bridge.value();

        final Constructor<E> constructor = eventClass.getDeclaredConstructor(bukkitEvent);
        Bukkit.getPluginManager().registerEvent(bukkitEvent, new Listener() {}, EventPriority.NORMAL, (listener, event) -> {
            if (bukkitEvent.isInstance(event)) {
                try {
                    org.byteskript.skript.api.Event bytecraftEvent = constructor.newInstance(event);

                    skript.runEvent(bytecraftEvent).all().join();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error triggering Bytecraft event: " + eventClass.getName(), e);
                }
            }
        }, plugin);

        plugin.getLogger().info("Registered Bytecraft event bridge for: " + eventClass.getName() +
                " -> " + bukkitEvent.getSimpleName());
    }
}
