package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.ByteCraftFlag;
import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import mx.kenzie.foundation.Type;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.syntax.Member;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.compiler.structure.SectionMeta;
import org.byteskript.skript.error.ScriptCompileError;
import org.byteskript.skript.lang.element.StandardElements;

import java.util.List;

@Documentation(
        name = "Plugin Declaration",
        description = """
            Declares a plugin configuration section. This member is only valid in the root script named 'index.bsk'.
            Within this section, plugin metadata such as name and version may be declared.
            """,
        examples = {
                """
                    plugin:
                        name: "Example Plugin"
                        version: "1.0.0"
                        api-version: "1.21"
                    """
        }
)
public class MemberPlugin extends Member {
    public static final Type INDEX_TYPE = new Type("skript.index");

    public MemberPlugin(final ModifiableLibrary provider) {
        super(provider, StandardElements.MEMBER, "plugin");
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        if (true) {
            context.getError().addHint(this, "The plugin member is not supported yet and does nothing");
            return null;
        }
        if (!"plugin".equals(thing)) return null;
        if (context.hasFlag(AreaFlag.IN_TYPE)) {
            context.getError().addHint(this, "The plugin member must be a root-level element.");
            return null;
        }

        if (!context.getType().equals(INDEX_TYPE)) {
            context.getError().addHint(this, "The plugin member must be used in " + INDEX_TYPE.internalName() + ", but we found it in " + context.getType().internalName());
            return null;
        }
        return super.match(thing, context);
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        context.addFlag(ByteCraftFlag.IN_PLUGIN_MEMBER);
        context.setState(CompileState.MEMBER_BODY);
        context.createTree(new DescriptionTree(context.getSection()));
    }

    @Override
    public void onSectionExit(final Context context, final SectionMeta meta) {
        context.removeFlag(ByteCraftFlag.IN_PLUGIN_MEMBER);
        final DescriptionTree description = context.findTree(DescriptionTree.class);
        if (description == null) throw new IllegalStateException("Could not find plugin description tree");
        final List<String> missingMandatoryFields = description.getMissingMandatoryFields();
        if (!missingMandatoryFields.isEmpty()) {
            throw new ScriptCompileError(context.lineNumber(), "The 'plugin' section is missing the following mandatory entries: " + String.join(", ", missingMandatoryFields));
        }
        context.closeAllTrees();
        context.removeTree(description);
        super.onSectionExit(context, meta);
    }
}
