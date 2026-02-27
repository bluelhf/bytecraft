package blue.lhf.bytecraft.runtime;

import blue.lhf.bytecraft.runtime.events.CommandEvent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.byteskript.skript.runtime.Skript;
import org.byteskript.skript.runtime.internal.CompiledScript;
import org.byteskript.skript.runtime.threading.ScriptRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CommandWrapper {
    private final Skript skript;

    public CommandWrapper(final Skript skript) {
        this.skript = skript;
    }

    /**
     * Wraps a {@link Command} from a compiled script so that the command's code is executed using the Skript instance's airlock queue.
     * Used at run-time to ensure commands run on the right thread. A race condition decides whether the right integer result is returned,
     * and if the command loses, the integer result will be <code>0</code>.
     * @param source The compiled source of the command, marked as the owner of the {@link ScriptRunner}
     * @param command The command to wrap
     * @return A {@link Command} that ex
     * */
    public Command<CommandSourceStack> wrapInAirlock(final Class<CompiledScript> source, final Command<CommandSourceStack> command) {
        return context -> {
            final Future<?> future = skript.runScript(new ScriptRunner() {
                int result;
                @Override
                public void start() {
                    try {
                        result = command.run(context);
                    } catch (final CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public Object result() {
                    return result;
                }

                @Override
                public Class<? extends CompiledScript> owner() {
                    return source;
                }
            }, new CommandEvent(context));
            try {
                if (future.isDone() && future.get() instanceof final Integer number) return number;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (final ExecutionException e) {
                context.getSource().getSender().sendRichMessage("<red>An error occurred while executing this command.</red>");
                throw new RuntimeException(e);
            }
            return 0;
        };
    }
}
