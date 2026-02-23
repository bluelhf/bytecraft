package blue.lhf.bytecraft.library.commands.tree;

/**
 * Details about the registration of a command, such as its name and the variable associated with its context.
 * @param name The name of the command, e.g. "foo" for <code>/foo</code>.
 * @param contextVariable The name of the command's context variable, e.g. "my_context" for a command defined as <code>command foo (my_context):</code>
 * */
public record CommandDetails(String name, String contextVariable) {
}
