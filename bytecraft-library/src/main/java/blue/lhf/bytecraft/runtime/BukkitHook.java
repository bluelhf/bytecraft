package blue.lhf.bytecraft.runtime;

import blue.lhf.bytecraft.runtime.events.EnableEvent;
import mx.kenzie.foundation.Type;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.byteskript.skript.api.resource.ClassResource;
import org.byteskript.skript.api.resource.Resource;
import org.byteskript.skript.runtime.Skript;
import org.byteskript.skript.runtime.internal.Instruction;
import org.byteskript.skript.runtime.threading.AirlockQueue;
import org.byteskript.skript.runtime.threading.OperationController;

import java.util.ListIterator;
import java.util.logging.Level;

/**
 * Utility class for building a Bukkit plugin entrypoint.
 **/
public class BukkitHook {
    public static final Type COMPILED_HOOK_TYPE = new Type(BootstrapPlugin.class);

    public static class BootstrapPlugin extends JavaPlugin {
        private final Skript skript = new Skript(null);
        @Override
        public void onLoad() {
            try {
                Bootstrap.run(skript);
            } catch (final Exception e) {
                getLogger().log(Level.SEVERE, "Failed to run script", e);
            }
        }

        @Override
        public void onEnable() {
            Bukkit.getScheduler().runTaskTimer(JavaPlugin.getProvidingPlugin(Bootstrap.class), () -> {
                for (final OperationController controller : skript.getProcesses()) {
                    final AirlockQueue queue;
                    synchronized (queue = controller.getQueue()) {
                        final ListIterator<Instruction<?>> iterator = queue.listIterator();
                        while (iterator.hasNext()) {
                            iterator.next().runSafely();
                            iterator.remove();
                        }
                    }

                    //noinspection SynchronizationOnLocalVariableOrMethodParameter - Controller is referenced by other threads
                    synchronized (controller) {
                        controller.notifyAll();
                    }
                }
            }, 1, 1);

            skript.runEvent(new EnableEvent()).all().join();
        }
    }

    private BukkitHook() {}

    public static class Bootstrap {
        public static void run(final Skript skript) throws Exception {
            for (final Resource resource : RuntimeCollector.collectRuntime(Bootstrap.class.getProtectionDomain(), "skript")) {
                if (!(resource instanceof final ClassResource classResource)) continue;
                skript.loadScript(classResource.open(), classResource.source().name());
            }
        }
    }
}
