package blue.lhf.bytecraft.library.plugin_hook.description;

import blue.lhf.bytecraft.ByteCraftFlag;
import blue.lhf.bytecraft.library.plugin_hook.DescriptionTree;
import mx.kenzie.foundation.Type;
import org.byteskript.skript.api.ModifiableLibrary;
import org.byteskript.skript.api.note.Documentation;
import org.byteskript.skript.api.resource.Resource;
import org.byteskript.skript.api.syntax.Member;
import org.byteskript.skript.compiler.*;
import org.byteskript.skript.compiler.structure.SectionMeta;
import org.byteskript.skript.error.ScriptCompileError;
import org.byteskript.skript.lang.element.StandardElements;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.util.List;

@Documentation(
        name = "Plugin Declaration",
        description = """
            Declares a plugin configuration section. This member is only valid in the root script named 'index.bsk'.
            Within this section, plugin metadata such as name and version may be declared.
            """,
        examples = MemberPlugin.EXAMPLE
)
public class MemberPlugin extends Member {
    public static final String EXAMPLE =
            """
            plugin:
                name: "Example Plugin"
                version: "1.0.0"
                api-version: "1.21"
            """;

    public MemberPlugin(final ModifiableLibrary provider) {
        super(provider, StandardElements.MEMBER, "plugin");
    }

    @Override
    public Pattern.Match match(final String thing, final Context context) {
        if (!"plugin".equals(thing)) return null;
        if (context.hasFlag(AreaFlag.IN_TYPE)) {
            context.getError().addHint(this, "The plugin member must be a root-level element.");
            return null;
        }

        if (context.hasFlag(ByteCraftFlag.HAS_PLUGIN_MEMBER)) {
            context.getError().addHint(this, "The plugin member may only be used once.");
            return null;
        }

        if (!isIndexType(context.getType())) {
            context.getError().addHint(this, "The plugin member must be used in a script in <something>/index, but we found it in " + context.getType().internalName());
            return null;
        }
        return super.match(thing, context);
    }

    private boolean isIndexType(final Type type) {
        final int slash = type.internalName().indexOf('/');
        if (slash == -1) return true;
        final int sub = type.internalName().indexOf('/', slash + 1);
        return sub == -1 && type.internalName().substring(slash + 1).equals("index");
    }

    @Override
    public void compile(final Context context, final Pattern.Match match) {
        context.addFlag(ByteCraftFlag.IN_PLUGIN_MEMBER);
        context.addFlag(ByteCraftFlag.HAS_PLUGIN_MEMBER);
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

        context.addResource(new Resource() {
            @Override
            public InputStream open() throws IOException {
                final PipedInputStream result = new PipedInputStream();
                try (final PipedOutputStream stream = new PipedOutputStream(result)) {
                    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                    mapper.writeValue(stream, description);
                }
                return result;
            }

            @Override
            public String getEntryName() {
                return "plugin.yml";
            }
        });
        context.closeAllTrees();
        context.removeTree(description);
        super.onSectionExit(context, meta);
    }
}
