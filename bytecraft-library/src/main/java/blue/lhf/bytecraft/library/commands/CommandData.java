package blue.lhf.bytecraft.library.commands;

import org.byteskript.skript.compiler.structure.BasicTree;
import org.byteskript.skript.compiler.structure.SectionMeta;

/**
 * Data related to a command tree, such as the command tree's root name, context variable name, arguments, and so on.
 * This is used to construct the code that registers the command with Brigadier.
 * <p>
 * These are recorded as additional data on the command member's {@link SectionMeta}. They <i>should</i> be a {@link BasicTree},
 * but are not because the trigger holder erroneously clears all trees instead of only its child trees when it exits.
 * @see MemberCommand
 * */
public class CommandData {
    /** The root literal node for this command (e.g. the node representing "/foo"). */
    private final CommandNode.Literal rootNode;

    /** The node that newly declared children should be attached to. */
    private CommandNode currentParent;
    /** The name of the command context variable provided to triggers in this command. */
    private final String contextVariable;

    /**
     * Creates a new command data carrier rooted at the given literal with an associated context variable name.
     * @param root the root literal node of the command
     * @param contextVariable the name of the context variable for this command's triggers
     */
    public CommandData(final CommandNode.Literal root, final String contextVariable) {
        this.rootNode = root;
        this.currentParent = root;
        this.contextVariable = contextVariable;
    }

    /**
     * Descends into the given node, attaching it as a child of the current parent and making it the new parent
     * for subsequent declarations.
     * @param node the node to enter
     */
    public void enterNode(final CommandNode node) {
        this.currentParent.addChild(node);
        this.currentParent = node;
    }

    /**
     * Moves back up one level in the command tree so that new nodes are attached to the previous parent.
     */
    public void exitNode() {
        this.currentParent = this.currentParent.parent();
    }

    /**
     * Returns the root literal node for this command.
     * @return the root node
     */
    public CommandNode.Literal getRoot() {
        return rootNode;
    }

    /**
     * Returns the name of the context variable associated with this command.
     * @return the context variable name
     */
    public String getContextVariable() {
        return contextVariable;
    }

    /**
     * Returns the node currently considered as the parent for newly declared children.
     * @return the current parent node
     */
    public CommandNode currentNode() {
        return this.currentParent;
    }
}
