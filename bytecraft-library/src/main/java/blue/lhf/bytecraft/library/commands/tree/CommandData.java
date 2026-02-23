package blue.lhf.bytecraft.library.commands.tree;

public class CommandData {
    private final CommandDetails details;

    public CommandData(final CommandDetails details) {
        this.details = details;
    }

    public CommandDetails getDetails() {
        return details;
    }
}
