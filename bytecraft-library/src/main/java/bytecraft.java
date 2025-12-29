import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Function;

public final class bytecraft {
    private bytecraft() {}

    public static Command<CommandSourceStack> wrap_as_command(final Function<CommandContext<CommandSourceStack>, Integer> function) {
        return function::apply;
    }
}
